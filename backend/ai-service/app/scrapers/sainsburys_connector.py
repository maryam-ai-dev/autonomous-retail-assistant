"""Sainsbury's Playwright connector."""

from __future__ import annotations

import re
from datetime import datetime, timezone
from typing import Any, Optional
from urllib.parse import quote_plus

from app.scrapers.playwright_connector import PlaywrightConnector, ScrapeOutcome
from app.scrapers.raw_product import RawScraperProduct

SEARCH_URL_TEMPLATE = (
    "https://www.sainsburys.co.uk/gol-ui/SearchResults/{query}"
)


class SainsburysConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("SAINSBURYS")

    def build_search_url(self, query: str) -> str:
        return SEARCH_URL_TEMPLATE.format(query=quote_plus(query))

    async def _search(self, query: str, max_results: int) -> ScrapeOutcome:
        # Live browser fetch is not available in this environment.
        # Subclasses (including tests) override _fetch_items to provide data.
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
        """Hook — return raw item dicts, or None to signal bot detection."""
        # Live fetch is not implemented without Playwright browsers installed.
        return []

    def _parse_item(self, item: dict[str, Any]) -> Optional[RawScraperProduct]:
        name = item.get("name")
        price = _as_float(item.get("price"))
        if not name or price is None:
            return None
        offer_flags: list[str] = []
        if item.get("nectarPrice"):
            offer_flags.append("NECTAR_PRICE")
        return RawScraperProduct(
            external_id=str(item.get("id") or _slugify(name)),
            display_name=name,
            brand=item.get("brand"),
            category="GROCERY",
            subcategory="UNKNOWN",
            price=price,
            price_from_text=False,
            unit_price=_as_float(item.get("unitPrice")),
            unit_basis=item.get("unitOfMeasure"),
            size_text=item.get("size"),
            image_url=item.get("imageUrl"),
            product_url=item.get("productUrl"),
            offer_flags=offer_flags,
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
