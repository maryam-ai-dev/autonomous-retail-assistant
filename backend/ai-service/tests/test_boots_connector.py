from __future__ import annotations

from typing import Any, Optional

import pytest

from app.scrapers.boots_connector import BootsConnector


class _StubBoots(BootsConnector):
    def __init__(self, items: Optional[list[dict[str, Any]]]) -> None:
        super().__init__()
        self._items = items

    async def _fetch_items(self, query, max_results):  # type: ignore[override]
        return self._items


@pytest.mark.asyncio
async def test_parses_boots_items() -> None:
    connector = _StubBoots([
        {
            "id": "b1",
            "name": "Sensodyne toothpaste 100ml",
            "brand": "Sensodyne",
            "price": "4.50",
            "imageUrl": "https://img.example/b1.jpg",
            "productUrl": "https://www.boots.com/p/b1",
            "subcategory": "DENTAL",
        }
    ])
    products = await connector.safe_search("toothpaste", 5)
    assert len(products) == 1
    assert products[0].external_id == "b1"
    assert products[0].subcategory == "DENTAL"


@pytest.mark.asyncio
async def test_bot_detection_marks_connector_unhealthy() -> None:
    connector = _StubBoots(None)
    products = await connector.safe_search("anything", 10)
    assert products == []
    status = connector.get_status()
    assert status["botDetected"] is True
    assert status["lastFailureReason"] == "bot detection"


def test_search_url_is_encoded() -> None:
    url = BootsConnector().build_search_url("face cream")
    assert "face+cream" in url
