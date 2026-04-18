"""Unit tests for the Playwright base connector."""

from __future__ import annotations

import asyncio
from datetime import datetime, timezone

import pytest

from app.scrapers.playwright_connector import (
    PlaywrightConnector,
    ScrapeOutcome,
)
from app.scrapers.raw_product import RawScraperProduct


class _TimeoutConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("STUB")

    async def _search(self, query, max_results):  # type: ignore[override]
        await asyncio.sleep(30)
        return ScrapeOutcome()


class _BotDetectedConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("STUB")

    async def _search(self, query, max_results):  # type: ignore[override]
        return ScrapeOutcome(bot_detected=True)


class _PartialConnector(PlaywrightConnector):
    def __init__(self) -> None:
        super().__init__("STUB")

    async def _search(self, query, max_results):  # type: ignore[override]
        return ScrapeOutcome(
            products=[
                RawScraperProduct(
                    external_id="p1",
                    display_name="Example",
                    source_fetched_at=datetime.now(timezone.utc).isoformat(),
                )
            ],
            warnings=["partial parse"],
        )


@pytest.mark.asyncio
async def test_timeout_fires_and_status_updated(monkeypatch: pytest.MonkeyPatch) -> None:
    # Shorten timeout for test
    import app.scrapers.playwright_connector as pc

    monkeypatch.setattr(pc, "REQUEST_TIMEOUT_SECONDS", 0.2)
    monkeypatch.setattr(pc, "MAX_RETRIES", 0)
    connector = _TimeoutConnector()
    result = await connector.safe_search("anything", 10)
    assert result == []
    status = connector.get_status()
    assert status["lastFailureReason"] == "timeout"


@pytest.mark.asyncio
async def test_bot_detection_returns_empty_and_marks_status() -> None:
    connector = _BotDetectedConnector()
    result = await connector.safe_search("anything", 10)
    assert result == []
    status = connector.get_status()
    assert status["botDetected"] is True
    assert status["lastFailureReason"] == "bot detection"


@pytest.mark.asyncio
async def test_partial_results_are_returned() -> None:
    connector = _PartialConnector()
    result = await connector.safe_search("milk", 10)
    assert len(result) == 1
    status = connector.get_status()
    assert status["recentResultCount"] == 1
