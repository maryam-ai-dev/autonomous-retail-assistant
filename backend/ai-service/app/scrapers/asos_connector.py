"""ASOS fashion connector (Playwright).

Spec from sprint B12.2. ASOS is the primary UK fashion retailer for Aisleon
v1.1. Listing-page data only — we do not follow PDP links. Halal tags are
never set on FASHION products; a "modest fashion" collection membership adds a
normalizationWarning but no halal inference.
"""

from __future__ import annotations

import re
from datetime import datetime, timezone
from typing import Any, Optional
from urllib.parse import quote_plus

from app.scrapers.playwright_connector import PlaywrightConnector, ScrapeOutcome
from app.scrapers.raw_product import RawScraperProduct

SEARCH_URL_TEMPLATE = "https://www.asos.com/search/?q={query}"

# ASOS department/section → Aisleon ProductSubcategory. Match is performed on
# lowercase substring against the raw department string so variants like
# "women's dresses" and "mens dresses" both resolve.
SUBCATEGORY_MAP: list[tuple[str, str]] = [
    ("footwear", "FOOTWEAR"),
    ("shoes", "FOOTWEAR"),
    ("trainers", "FOOTWEAR"),
    ("boots", "FOOTWEAR"),
    ("sandals", "FOOTWEAR"),
    ("dresses", "DRESSES"),
    ("coats", "OUTERWEAR"),
    ("jackets", "OUTERWEAR"),
    ("outerwear", "OUTERWEAR"),
    ("sportswear", "SPORTSWEAR"),
    ("activewear", "SPORTSWEAR"),
    ("tops", "TOPS"),
    ("t-shirts", "TOPS"),
    ("tees", "TOPS"),
    ("shirts", "TOPS"),
    ("blouses", "TOPS"),
    ("knitwear", "TOPS"),
    ("bottoms", "BOTTOMS"),
    ("trousers", "BOTTOMS"),
    ("jeans", "BOTTOMS"),
    ("shorts", "BOTTOMS"),
    ("skirts", "BOTTOMS"),
    ("underwear", "UNDERWEAR"),
    ("lingerie", "UNDERWEAR"),
    ("accessories", "ACCESSORIES"),
    ("bags", "ACCESSORIES"),
    ("scarves", "ACCESSORIES"),
    ("sunglasses", "ACCESSORIES"),
    ("jewellery", "ACCESSORIES"),
    ("hats", "ACCESSORIES"),
    ("belts", "ACCESSORIES"),
]


class AsosConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("ASOS")

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
        # Live Playwright fetch is not wired in this environment. Returning []
        # keeps the bridge contract intact (empty list is a valid response).
        return []

    def _parse_item(self, item: dict[str, Any]) -> Optional[RawScraperProduct]:
        name = item.get("name")
        price = _as_float(item.get("price"))
        if not name or price is None:
            return None

        subcategory = _map_subcategory(item.get("department"))

        offer_flags: list[str] = []
        original_price = _as_float(item.get("originalPrice"))
        if original_price is not None and original_price > price:
            offer_flags.append("REDUCED_TO_CLEAR")

        size_text = _as_optional_str(item.get("sizes"))

        warnings: list[str] = []
        if _is_modest_collection(item):
            warnings.append(
                "modest fashion collection — verify fabric composition if relevant"
            )

        return RawScraperProduct(
            external_id=str(item.get("id") or _slugify(name)),
            display_name=name,
            brand=_as_optional_str(item.get("brand")),
            category="FASHION",
            subcategory=subcategory,
            price=price,
            price_from_text=False,
            size_text=size_text,
            image_url=_as_optional_str(item.get("imageUrl")),
            product_url=_strip_tracking(_as_optional_str(item.get("productUrl"))),
            certification_tags=[],
            offer_flags=offer_flags,
            source_fetched_at=datetime.now(timezone.utc).isoformat(),
            normalization_warnings=warnings,
        )


def _map_subcategory(department: Any) -> str:
    if not department:
        return "UNKNOWN"
    lower = str(department).lower()
    for needle, subcat in SUBCATEGORY_MAP:
        if needle in lower:
            return subcat
    return "UNKNOWN"


def _is_modest_collection(item: dict[str, Any]) -> bool:
    tags = item.get("collections") or item.get("tags") or []
    if not isinstance(tags, list):
        return False
    return any("modest" in str(t).lower() for t in tags)


def _as_float(value: Any) -> Optional[float]:
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _as_optional_str(value: Any) -> Optional[str]:
    if value is None:
        return None
    s = str(value).strip()
    return s or None


def _strip_tracking(url: Optional[str]) -> Optional[str]:
    if not url:
        return url
    # Drop query/fragment — ASOS listing links carry ctaref= / affid= /
    # utm_source= params that should never persist in stored product URLs.
    return url.split("?")[0].split("#")[0]


def _slugify(value: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
