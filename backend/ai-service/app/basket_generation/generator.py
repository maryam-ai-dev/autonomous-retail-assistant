"""Basket generator — two-pass Claude Sonnet with budget retry.

Rules from CLAUDE.md and Phase B6 of SPRINT_PLAN_BACKEND.md:
    * max_tokens = 2000
    * Prompt lives on disk (prompts/basket_generate.txt), not hardcoded.
    * One retry if totalCost > budget, with a "reduce cost" nudge.
    * Hallucinated candidate_ids are dropped — never pass through.
    * We are ADVISORY — Spring Boot will re-validate and trim.
"""

from __future__ import annotations

import json
import logging
import re
from decimal import Decimal, InvalidOperation
from pathlib import Path

from app.core.anthropic_client import AnthropicClient, AnthropicClientProtocol
from app.schemas.basket import (
    BasketGenerateRequest,
    CandidateProduct,
    GeneratedBasketItem,
    GeneratedDraft,
)

log = logging.getLogger(__name__)

MAX_TOKENS = 2000
PROMPT_PATH = Path(__file__).resolve().parents[2] / "prompts" / "basket_generate.txt"

_REDUCE_NUDGE = (
    "IMPORTANT: your previous attempt exceeded the budget. Reduce the total"
    " cost — pick fewer or cheaper items so the totalCost is well below the"
    " budget."
)


class BasketGenerator:
    def __init__(
        self,
        client: AnthropicClientProtocol | None = None,
        prompt_path: Path | None = None,
    ) -> None:
        self._client = client or AnthropicClient()
        self._prompt_path = prompt_path or PROMPT_PATH

    def generate(self, request: BasketGenerateRequest) -> GeneratedDraft:
        by_id = {c.candidate_id: c for c in request.candidates}
        if not by_id:
            return GeneratedDraft(
                items=[], total_cost=Decimal("0"), retry_count=0, warnings=["no candidates supplied"]
            )

        warnings: list[str] = []

        items = self._call_llm(request, by_id, warnings, extra_nudge="")
        total = _compute_total(items, by_id)

        retry_count = 0
        if request.budget is not None and total > request.budget:
            warnings.append(
                f"first pass exceeded budget ({total} > {request.budget}); retrying with nudge"
            )
            retry_items = self._call_llm(
                request, by_id, warnings, extra_nudge=_REDUCE_NUDGE
            )
            retry_total = _compute_total(retry_items, by_id)
            retry_count = 1
            if retry_total <= total:
                items = retry_items
                total = retry_total
            else:
                warnings.append(
                    "retry produced a more expensive basket; keeping first pass"
                )

        if request.budget is not None and total > request.budget:
            warnings.append(
                f"final total {total} still exceeds budget {request.budget} — Spring will trim"
            )

        return GeneratedDraft(
            items=items, total_cost=total, retry_count=retry_count, warnings=warnings
        )

    def _call_llm(
        self,
        request: BasketGenerateRequest,
        by_id: dict[str, CandidateProduct],
        warnings: list[str],
        *,
        extra_nudge: str,
    ) -> list[GeneratedBasketItem]:
        prompt = self._render_prompt(request, extra_nudge)
        raw = self._client.complete(prompt=prompt, max_tokens=MAX_TOKENS)
        payload = _extract_json(raw)
        if payload is None:
            warnings.append("LLM output was not parseable JSON; returning empty basket")
            return []
        return _coerce_items(payload, by_id, warnings)

    def _render_prompt(self, request: BasketGenerateRequest, extra_nudge: str) -> str:
        template = self._prompt_path.read_text()
        intent_json = {
            "raw_text": request.raw_text,
            "budget": (
                float(request.budget) if request.budget is not None else None
            ),
            "category": request.category,
            "dietary_requirements": request.dietary_requirements,
            "retailer_hints": request.retailer_hints,
            "item_hints": request.item_hints,
        }
        candidates_json = [_candidate_to_prompt_row(c) for c in request.candidates]
        return (
            template.replace("{EXTRA_NUDGE}", extra_nudge)
            .replace("{INTENT_JSON}", json.dumps(intent_json, ensure_ascii=False))
            .replace("{CANDIDATES_JSON}", json.dumps(candidates_json, ensure_ascii=False))
        )


def _candidate_to_prompt_row(c: CandidateProduct) -> dict:
    return {
        "candidateId": c.candidate_id,
        "name": c.name,
        "brand": c.brand,
        "retailer": c.retailer,
        "price": float(c.price),
        "unit_price": float(c.unit_price) if c.unit_price is not None else None,
        "unit_basis": c.unit_basis,
        "subcategory": c.subcategory,
        "dietary_tags": c.dietary_tags,
    }


def _extract_json(raw: str) -> dict | None:
    raw = raw.strip()
    fence = re.match(r"```(?:json)?\s*(.*?)```", raw, flags=re.DOTALL)
    if fence:
        raw = fence.group(1).strip()
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        match = re.search(r"\{.*\}", raw, flags=re.DOTALL)
        if not match:
            return None
        try:
            return json.loads(match.group(0))
        except json.JSONDecodeError:
            return None


def _coerce_items(
    payload: dict,
    by_id: dict[str, CandidateProduct],
    warnings: list[str],
) -> list[GeneratedBasketItem]:
    raw_items = payload.get("items", []) if isinstance(payload, dict) else []
    if not isinstance(raw_items, list):
        return []
    items: list[GeneratedBasketItem] = []
    for entry in raw_items:
        if not isinstance(entry, dict):
            continue
        cid = str(entry.get("candidateId") or entry.get("candidate_id") or "").strip()
        if not cid:
            continue
        if cid not in by_id:
            warnings.append(f"dropped hallucinated candidateId {cid!r}")
            continue
        qty = entry.get("quantity", 1)
        try:
            quantity = max(1, int(qty))
        except (TypeError, ValueError):
            quantity = 1
        reasoning = str(entry.get("reasoning") or "").strip()
        items.append(
            GeneratedBasketItem(
                candidate_id=cid, quantity=quantity, reasoning=reasoning
            )
        )
    return items


def _compute_total(
    items: list[GeneratedBasketItem], by_id: dict[str, CandidateProduct]
) -> Decimal:
    total = Decimal("0")
    for it in items:
        candidate = by_id.get(it.candidate_id)
        if candidate is None:
            continue
        try:
            total += candidate.price * Decimal(it.quantity)
        except InvalidOperation:
            continue
    return total
