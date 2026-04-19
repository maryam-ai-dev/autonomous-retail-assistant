"""Budget-insights generator.

Spec from SPRINT_PLAN_BACKEND.md B10.2:
    * Claude Sonnet, max_tokens 300
    * Prompt lives at prompts/budget_insights.txt (not hardcoded).
    * Insights must reference values present in input — no hallucinated figures.
    * Validation: strip any insight that cites a figure not present in the
      BudgetSummary.
"""

from __future__ import annotations

import json
import logging
import re
from decimal import Decimal, InvalidOperation
from pathlib import Path

from app.core.anthropic_client import AnthropicClient, AnthropicClientProtocol
from app.schemas.budget import BudgetInsightsResponse, BudgetSummary

log = logging.getLogger(__name__)

MAX_TOKENS = 300
PROMPT_PATH = Path(__file__).resolve().parents[2] / "prompts" / "budget_insights.txt"

_FIGURE_RE = re.compile(r"£?\s?(\d+(?:\.\d{1,2})?)")


class BudgetInsightsGenerator:
    def __init__(
        self,
        client: AnthropicClientProtocol | None = None,
        prompt_path: Path | None = None,
    ) -> None:
        self._client = client or AnthropicClient()
        self._prompt_path = prompt_path or PROMPT_PATH

    def insights(self, summary: BudgetSummary) -> BudgetInsightsResponse:
        warnings: list[str] = []
        prompt = self._prompt_path.read_text().replace(
            "{SUMMARY_JSON}", _summary_to_prompt_json(summary)
        )
        raw = self._client.complete(prompt=prompt, max_tokens=MAX_TOKENS)
        payload = _extract_json(raw)
        if payload is None:
            warnings.append("LLM output was not parseable JSON; returning empty insights")
            return BudgetInsightsResponse(insights=[], warnings=warnings)

        candidate_insights = payload.get("insights", []) if isinstance(payload, dict) else []
        if not isinstance(candidate_insights, list):
            warnings.append("LLM returned non-list insights; returning empty")
            return BudgetInsightsResponse(insights=[], warnings=warnings)

        allowed = _allowed_figures(summary)
        cleaned: list[str] = []
        for raw_text in candidate_insights:
            if not isinstance(raw_text, str):
                continue
            text = raw_text.strip()
            if not text:
                continue
            cited = _figures_in(text)
            unsupported = [f for f in cited if f not in allowed]
            if unsupported:
                warnings.append(
                    f"dropped insight citing unsupported figures {unsupported}: {text!r}"
                )
                continue
            cleaned.append(text)
        return BudgetInsightsResponse(insights=cleaned, warnings=warnings)


def _summary_to_prompt_json(summary: BudgetSummary) -> str:
    return json.dumps(
        {
            "month": summary.month,
            "spent": float(summary.spent),
            "budget": float(summary.budget),
            "savedVsFullPrice": float(summary.saved_vs_full_price),
            "byRetailer": {k: float(v) for k, v in summary.by_retailer.items()},
            "basketCount": summary.basket_count,
        },
        ensure_ascii=False,
    )


def _allowed_figures(summary: BudgetSummary) -> set[Decimal]:
    out: set[Decimal] = set()
    out.add(_q(summary.spent))
    out.add(_q(summary.budget))
    out.add(_q(summary.saved_vs_full_price))
    for v in summary.by_retailer.values():
        out.add(_q(v))
    # Allow the implied difference between budget and spent — a common framing
    # ("£X under budget"). This is derivable from cited figures, not hallucinated.
    out.add(_q(summary.budget - summary.spent))
    out.discard(Decimal("0.00"))
    return out


def _figures_in(text: str) -> list[Decimal]:
    out: list[Decimal] = []
    for match in _FIGURE_RE.finditer(text):
        try:
            out.append(_q(Decimal(match.group(1))))
        except InvalidOperation:
            continue
    return out


def _q(value: Decimal) -> Decimal:
    return value.quantize(Decimal("0.01"))


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
