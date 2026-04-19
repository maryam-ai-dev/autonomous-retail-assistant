"""POST /basket/generate — advisory basket construction."""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException

from app.basket_generation import BasketGenerator
from app.schemas.basket import BasketGenerateRequest, GeneratedDraft

log = logging.getLogger(__name__)

router = APIRouter(prefix="/basket", tags=["basket"])


def _generator() -> BasketGenerator:
    return BasketGenerator()


@router.post("/generate", response_model=GeneratedDraft)
async def generate_basket(request: BasketGenerateRequest) -> GeneratedDraft:
    try:
        return _generator().generate(request)
    except RuntimeError as exc:
        log.warning("basket generation runtime error: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc)) from exc
