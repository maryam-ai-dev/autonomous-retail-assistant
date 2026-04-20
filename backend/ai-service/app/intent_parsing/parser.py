"""Intent parser — loads prompt from disk, calls Claude Sonnet, validates output.

Rules from CLAUDE.md:
    * Max tokens: 500.
    * Prompt lives on disk (prompts/intent_parse.txt), not hardcoded.
    * If the LLM cannot extract a budget, budget stays None. We never guess.
"""

from __future__ import annotations

import json
import logging
import re
from decimal import Decimal, InvalidOperation
from pathlib import Path

from app.core.anthropic_client import AnthropicClient, AnthropicClientProtocol
from app.schemas.intent import IntentCategory, ParsedIntent

log = logging.getLogger(__name__)

MAX_TOKENS = 500
PROMPT_PATH = Path(__file__).resolve().parents[2] / "prompts" / "intent_parse.txt"


class IntentParser:
    def __init__(
        self,
        client: AnthropicClientProtocol | None = None,
        prompt_path: Path | None = None,
    ) -> None:
        self._client = client or AnthropicClient()
        self._prompt_path = prompt_path or PROMPT_PATH

    def parse(self, text: str) -> tuple[ParsedIntent, list[str]]:
        warnings: list[str] = []
        prompt = self._prompt_path.read_text().replace("{USER_INPUT}", text)
        raw = self._client.complete(prompt=prompt, max_tokens=MAX_TOKENS)
        payload = _extract_json(raw)
        if payload is None:
            warnings.append("LLM output was not parseable JSON; returning default intent")
            return _default_intent(text), warnings
        intent = _coerce_intent(text, payload, warnings)
        return intent, warnings


def _extract_json(raw: str) -> dict | None:
    raw = raw.strip()
    fence = re.match(r"```(?:json)?\s*(.*?)```", raw, flags=re.DOTALL)
    if fence:
        raw = fence.group(1).strip()
    try:
        parsed = json.loads(raw)
    except json.JSONDecodeError:
        match = re.search(r"\{.*\}", raw, flags=re.DOTALL)
        if not match:
            return None
        try:
            parsed = json.loads(match.group(0))
        except json.JSONDecodeError:
            return None
    return parsed if isinstance(parsed, dict) else None


def _coerce_intent(text: str, data: dict, warnings: list[str]) -> ParsedIntent:
    budget_raw = data.get("budget")
    budget: Decimal | None
    if budget_raw is None or budget_raw == "":
        budget = None
    else:
        try:
            budget = Decimal(str(budget_raw))
            if budget <= 0:
                budget = None
                warnings.append("budget was non-positive; coerced to null")
        except (InvalidOperation, TypeError):
            budget = None
            warnings.append("budget was not numeric; coerced to null")

    out_of_scope_raw = data.get("outOfScope", data.get("out_of_scope"))
    out_of_scope = bool(out_of_scope_raw) if out_of_scope_raw is not None else False
    out_of_scope_reason = _as_optional_str(data.get("reason") or data.get("out_of_scope_reason"))

    category_raw = str(data.get("category", "HEALTH_BEAUTY")).upper()
    if category_raw == "GROCERY":
        # LLM slipped back to GROCERY despite the prompt — coerce to out-of-scope
        # so Spring can 422 it instead of surfacing grocery results.
        warnings.append("LLM returned GROCERY category; coerced to out_of_scope=true")
        out_of_scope = True
        if out_of_scope_reason is None:
            out_of_scope_reason = "grocery"
        category = IntentCategory.HEALTH_BEAUTY
    else:
        try:
            category = IntentCategory(category_raw)
        except ValueError:
            warnings.append(f"unknown category {category_raw!r}; defaulting to HEALTH_BEAUTY")
            category = IntentCategory.HEALTH_BEAUTY

    return ParsedIntent(
        raw_text=text,
        budget=budget,
        currency=str(data.get("currency") or "GBP"),
        category=category,
        subcategories=_as_str_list(data.get("subcategories")),
        dietary_requirements=[s.upper() for s in _as_str_list(data.get("dietary_requirements"))],
        retailer_hints=[s.upper() for s in _as_str_list(data.get("retailer_hints"))],
        item_hints=_as_str_list(data.get("item_hints")),
        timing=_as_optional_str(data.get("timing")),
        notes=_as_optional_str(data.get("notes")),
        out_of_scope=out_of_scope,
        out_of_scope_reason=out_of_scope_reason,
    )


def _default_intent(text: str) -> ParsedIntent:
    return ParsedIntent(raw_text=text, budget=None, category=IntentCategory.HEALTH_BEAUTY)


def _as_str_list(value: object) -> list[str]:
    if not isinstance(value, list):
        return []
    return [str(v).strip() for v in value if isinstance(v, (str, int, float)) and str(v).strip()]


def _as_optional_str(value: object) -> str | None:
    if value is None:
        return None
    s = str(value).strip()
    return s or None
