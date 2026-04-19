"""POST /intent/parse — advisory structured extraction of basket intent."""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException

from app.intent_parsing import IntentParser
from app.schemas.intent import IntentParseRequest, IntentParseResponse

log = logging.getLogger(__name__)

router = APIRouter(prefix="/intent", tags=["intent"])


def _parser() -> IntentParser:
    return IntentParser()


@router.post("/parse", response_model=IntentParseResponse)
async def parse_intent(request: IntentParseRequest) -> IntentParseResponse:
    try:
        intent, warnings = _parser().parse(request.text)
    except RuntimeError as exc:
        log.warning("intent parse runtime error: %s", exc)
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    return IntentParseResponse(intent=intent, warnings=warnings)
