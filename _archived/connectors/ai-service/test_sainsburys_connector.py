from __future__ import annotations

from typing import Any, Optional

import pytest

from app.scrapers.sainsburys_connector import SainsburysConnector


class _StubConnector(SainsburysConnector):
    def __init__(self, items: Optional[list[dict[str, Any]]]) -> None:
        super().__init__()
        self._items = items

    async def _fetch_items(self, query, max_results):  # type: ignore[override]
        return self._items


@pytest.mark.asyncio
async def test_parses_items_into_raw_products() -> None:
    connector = _StubConnector([
        {
            "id": "s1",
            "name": "British oat milk 1L",
            "brand": "Sainsbury's",
            "price": "1.75",
            "unitPrice": "1.75",
            "unitOfMeasure": "per litre",
            "size": "1L",
            "imageUrl": "https://img.example/s1.jpg",
            "productUrl": "https://www.sainsburys.co.uk/gol-ui/product/s1",
            "nectarPrice": True,
        }
    ])

    products = await connector.safe_search("oat milk", 10)

    assert len(products) == 1
    assert products[0].offer_flags == ["NECTAR_PRICE"]
    assert products[0].price == 1.75


@pytest.mark.asyncio
async def test_bot_detection_returns_empty() -> None:
    connector = _StubConnector(None)
    products = await connector.safe_search("anything", 10)
    assert products == []
    assert connector.get_status()["botDetected"] is True


def test_search_url_is_properly_encoded() -> None:
    connector = SainsburysConnector()
    url = connector.build_search_url("oat milk & honey")
    assert "oat+milk+%26+honey" in url
