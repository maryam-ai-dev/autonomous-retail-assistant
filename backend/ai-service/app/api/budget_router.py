"""POST /budget/insights — advisory budget insights, hallucination-guarded."""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException

from app.budget_insights import BudgetInsightsGenerator
from app.schemas.budget import BudgetInsightsRequest, BudgetInsightsResponse

log = logging.getLogger(__name__)

router = APIRouter(prefix="/budget", tags=["budget"])


def _generator() -> BudgetInsightsGenerator:
    return BudgetInsightsGenerator()


@router.post("/insights", response_model=BudgetInsightsResponse)
async def budget_insights(request: BudgetInsightsRequest) -> BudgetInsightsResponse:
    try:
        return _generator().insights(request.summary)
    except RuntimeError as exc:
        log.warning("budget insights runtime error: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc)) from exc
