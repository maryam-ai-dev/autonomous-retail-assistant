"""Boots Playwright connector."""

from __future__ import annotations

import re
from datetime import datetime, timezone
from typing import Any, Optional
from urllib.parse import quote_plus

from app.scrapers.playwright_connector import PlaywrightConnector, ScrapeOutcome
from app.scrapers.raw_product import RawScraperProduct

SEARCH_URL_TEMPLATE = "https://www.boots.com/search?q={query}"


class BootsConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("BOOTS")

    def build_search_url(self, query: str) -> str:
        return SEARCH_URL_TEMPLATE.format(query=quote_plus(query))

    async def _search(self, query: str, max_results: int) -> ScrapeOutcome:
        items = await self._fetch_items(query, max_results)
        if items is None:
            return ScrapeOutcome(bot_detected=True)
        products = [self._parse_item(item) for item in items[:max_results]]
        products = [p for p in products if p is not None]
        warnings: list[str] = []
        if not products and items:
            warnings.append("partial parse")
        return ScrapeOutcome(products=products, warnings=warnings)

    async def _fetch_items(
        self, query: str, max_results: int
    ) -> Optional[list[dict[str, Any]]]:
        # Live fetch is not implemented without Playwright browsers.
        return []

    def _parse_item(self, item: dict[str, Any]) -> Optional[RawScraperProduct]:
        name = item.get("name")
        price = _as_float(item.get("price"))
        if not name or price is None:
            return None
        return RawScraperProduct(
            external_id=str(item.get("id") or _slugify(name)),
            display_name=name,
            brand=item.get("brand"),
            category="HEALTH_BEAUTY",
            subcategory=item.get("subcategory", "UNKNOWN"),
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
