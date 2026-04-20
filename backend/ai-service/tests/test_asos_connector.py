from __future__ import annotations

from typing import Any, Optional

import pytest

from app.scrapers.asos_connector import AsosConnector


class _StubAsos(AsosConnector):
    def __init__(self, items: Optional[list[dict[str, Any]]]) -> None:
        super().__init__()
        self._items = items

    async def _fetch_items(self, query, max_results):  # type: ignore[override]
        return self._items


@pytest.mark.asyncio
async def test_parses_dress_as_fashion_dresses_subcategory() -> None:
    connector = _StubAsos([
        {
            "id": "a1",
            "name": "Black midi dress",
            "brand": "ASOS DESIGN",
            "price": "45.00",
            "department": "Women's Dresses",
            "sizes": "XS, S, M, L",
            "imageUrl": "https://img.example/a1.jpg",
            "productUrl": "https://www.asos.com/p/a1?ctaref=search",
        }
    ])
    products = await connector.safe_search("black midi dress", 10)
    assert len(products) == 1
    product = products[0]
    assert product.external_id == "a1"
    assert product.category == "FASHION"
    assert product.subcategory == "DRESSES"
    assert product.brand == "ASOS DESIGN"
    assert product.size_text == "XS, S, M, L"
    assert product.product_url == "https://www.asos.com/p/a1"


@pytest.mark.asyncio
async def test_footwear_maps_trainers_to_footwear_subcategory() -> None:
    connector = _StubAsos([
        {
            "id": "a2",
            "name": "White trainers size 6",
            "brand": "Nike",
            "price": "60.00",
            "department": "Men's Trainers",
            "sizes": "6, 7, 8, 9",
            "imageUrl": "https://img.example/a2.jpg",
            "productUrl": "https://www.asos.com/p/a2",
        }
    ])
    products = await connector.safe_search("trainers", 5)
    assert products[0].subcategory == "FOOTWEAR"
    assert products[0].size_text == "6, 7, 8, 9"


@pytest.mark.asyncio
async def test_reduced_price_populates_offer_flag() -> None:
    connector = _StubAsos([
        {
            "id": "a3",
            "name": "Leather jacket",
            "brand": "Topshop",
            "price": "70.00",
            "originalPrice": "120.00",
            "department": "Coats & Jackets",
            "imageUrl": "https://img.example/a3.jpg",
            "productUrl": "https://www.asos.com/p/a3",
        }
    ])
    products = await connector.safe_search("leather jacket", 5)
    assert products[0].subcategory == "OUTERWEAR"
    assert "REDUCED_TO_CLEAR" in products[0].offer_flags


@pytest.mark.asyncio
async def test_modest_collection_adds_warning_no_halal_tag() -> None:
    connector = _StubAsos([
        {
            "id": "a4",
            "name": "Long-sleeve maxi dress",
            "brand": "ASOS DESIGN",
            "price": "50.00",
            "department": "Women's Dresses",
            "collections": ["Modest Fashion"],
            "imageUrl": "https://img.example/a4.jpg",
            "productUrl": "https://www.asos.com/p/a4",
        }
    ])
    products = await connector.safe_search("modest dress", 5)
    product = products[0]
    assert any("modest" in w.lower() for w in product.normalization_warnings)
    # FASHION products never carry halal tags, even via modest collection.
    assert product.certification_tags == []


@pytest.mark.asyncio
async def test_unknown_department_falls_back_to_unknown_subcategory() -> None:
    connector = _StubAsos([
        {
            "id": "a5",
            "name": "Mystery item",
            "price": "10.00",
            "department": "Homeware",  # ASOS does carry some homeware but we don't map it
            "imageUrl": "https://img.example/a5.jpg",
            "productUrl": "https://www.asos.com/p/a5",
        }
    ])
    products = await connector.safe_search("thing", 5)
    assert products[0].subcategory == "UNKNOWN"


@pytest.mark.asyncio
async def test_bot_detection_marks_connector_unhealthy() -> None:
    connector = _StubAsos(None)
    products = await connector.safe_search("anything", 10)
    assert products == []
    status = connector.get_status()
    assert status["botDetected"] is True
    assert status["lastFailureReason"] == "bot detection"


def test_search_url_is_encoded() -> None:
    url = AsosConnector().build_search_url("black midi dress")
    assert "black+midi+dress" in url
