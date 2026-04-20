"""FastAPI scraper bridge — called by Spring Boot."""

from __future__ import annotations

from typing import Any

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.scrapers.argos_connector import ArgosConnector
from app.scrapers.asos_connector import AsosConnector
from app.scrapers.boots_connector import BootsConnector
from app.scrapers.playwright_connector import PlaywrightConnector

router = APIRouter(prefix="/scrapers", tags=["scrapers"])


class ScraperSearchRequest(BaseModel):
    query: str
    retailer: str
    maxResults: int = Field(default=20, ge=1, le=100)


_CONNECTORS: dict[str, type[PlaywrightConnector]] = {
    "BOOTS": BootsConnector,
    "ARGOS": ArgosConnector,
    "ASOS": AsosConnector,
}


@router.post("/search")
async def search(request: ScraperSearchRequest) -> list[dict[str, Any]]:
    connector_cls = _CONNECTORS.get(request.retailer.upper())
    if connector_cls is None:
        raise HTTPException(status_code=400, detail=f"Unknown retailer: {request.retailer}")
    connector = connector_cls()
    products = await connector.safe_search(request.query, request.maxResults)
    return [product.model_dump() for product in products]


@router.get("/status")
async def status() -> list[dict[str, Any]]:
    statuses: list[dict[str, Any]] = []
    for retailer, cls in _CONNECTORS.items():
        connector = cls()
        snap = connector.get_status()
        snap["retailer"] = retailer
        statuses.append(snap)
    return statuses
