"""Argos Playwright connector."""

from __future__ import annotations

import re
from datetime import datetime, timezone
from typing import Any, Optional
from urllib.parse import quote_plus

from app.scrapers.playwright_connector import PlaywrightConnector, ScrapeOutcome
from app.scrapers.raw_product import RawScraperProduct

SEARCH_URL_TEMPLATE = "https://www.argos.co.uk/search/{query}/"

CATEGORY_MAP: dict[str, str] = {
    "kitchen": "KITCHEN",
    "bedding": "BEDDING",
    "furniture": "FURNITURE",
    "toys": "TOYS",
    "garden": "GARDEN",
    "diy": "DIY",
    "appliances": "ELECTRICAL_APPLIANCES",
    "laptops": "LAPTOPS",
    "phones": "PHONES",
    "audio": "AUDIO",
    "tv": "TV",
    "gaming": "GAMING",
    "cameras": "CAMERAS",
    "smart home": "SMART_HOME",
}


class ArgosConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("ARGOS")

    def build_search_url(self, query: str) -> str:
        return SEARCH_URL_TEMPLATE.format(query=quote_plus(query))

    async def _search(self, query: str, max_results: int) -> ScrapeOutcome:
        items = await self._fetch_items(query, max_results)
        if items is None:
            return ScrapeOutcome(bot_detected=True)
        products = [self._parse_item(item) for item in items[:max_results]]
        products = [p for p in products if p is not None]
        return ScrapeOutcome(products=products)

    async def _fetch_items(
        self, query: str, max_results: int
    ) -> Optional[list[dict[str, Any]]]:
        return []

    def _parse_item(self, item: dict[str, Any]) -> Optional[RawScraperProduct]:
        name = item.get("name")
        price = _as_float(item.get("price"))
        if not name or price is None:
            return None
        argos_category = (item.get("category") or "").lower()
        subcategory = CATEGORY_MAP.get(argos_category, "UNKNOWN")
        return RawScraperProduct(
            external_id=str(item.get("id") or _slugify(name)),
            display_name=name,
            brand=item.get("brand"),
            category="GENERAL_MERCHANDISE",
            subcategory=subcategory,
            price=price,
            price_from_text=False,
            image_url=item.get("imageUrl"),
            product_url=item.get("productUrl"),
            source_fetched_at=datetime.now(timezone.utc).isoformat(),
        )


def _as_float(value: Any) -> Optional[float]:
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _slugify(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
