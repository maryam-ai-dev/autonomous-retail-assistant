from __future__ import annotations

from typing import Any, Optional

import pytest

from app.scrapers.argos_connector import ArgosConnector


class _StubArgos(ArgosConnector):
    def __init__(self, items: Optional[list[dict[str, Any]]]) -> None:
        super().__init__()
        self._items = items

    async def _fetch_items(self, query, max_results):  # type: ignore[override]
        return self._items


@pytest.mark.asyncio
async def test_parses_argos_items_with_category_mapping() -> None:
    connector = _StubArgos([
        {
            "id": "a1",
            "name": "Breville 1.7L kettle",
            "brand": "Breville",
            "price": "29.99",
            "imageUrl": "https://img.example/a1.jpg",
            "productUrl": "https://www.argos.co.uk/p/a1",
            "category": "kitchen",
        }
    ])
    products = await connector.safe_search("kettle", 10)
    assert len(products) == 1
    assert products[0].subcategory == "KITCHEN"


@pytest.mark.asyncio
async def test_zero_results_returns_empty_not_error() -> None:
    connector = _StubArgos([])
    products = await connector.safe_search("nothing", 10)
    assert products == []
    # No bot-detection flag, no failure reason.
    assert connector.get_status()["botDetected"] is False


def test_url_is_encoded() -> None:
    url = ArgosConnector().build_search_url("electric kettle")
    assert "electric+kettle" in url
