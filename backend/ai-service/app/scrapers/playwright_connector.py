"""Base class for Playwright-based retailer scrapers."""

from __future__ import annotations

import asyncio
import logging
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Optional

from app.scrapers.raw_product import RawScraperProduct

logger = logging.getLogger(__name__)

STEALTH_HEADERS: dict[str, str] = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/122.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "en-GB,en;q=0.9",
    "Accept": (
        "text/html,application/xhtml+xml,application/xml;q=0.9,"
        "image/avif,image/webp,*/*;q=0.8"
    ),
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "none",
    "Upgrade-Insecure-Requests": "1",
}

REQUEST_TIMEOUT_SECONDS = 15
MAX_RETRIES = 1


@dataclass
class ConnectorStatusSnapshot:
    """Status fields mirrored in the Spring bridge."""

    last_success_at: Optional[datetime] = None
    last_failure_at: Optional[datetime] = None
    last_failure_reason: Optional[str] = None
    recent_result_count: int = 0
    bot_detected: bool = False


@dataclass
class ScrapeOutcome:
    products: list[RawScraperProduct] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)
    bot_detected: bool = False


class PlaywrightConnector(ABC):
    """Abstract connector: every retailer-specific scraper extends this."""

    def __init__(self, retailer_key: str) -> None:
        self.retailer_key = retailer_key
        self._status = ConnectorStatusSnapshot()

    async def safe_search(
        self, query: str, max_results: int
    ) -> list[RawScraperProduct]:
        """Run a search with timeout, retry, and bot-detection fallback."""
        attempts_left = MAX_RETRIES + 1
        last_error: Optional[str] = None
        while attempts_left > 0:
            attempts_left -= 1
            try:
                outcome = await asyncio.wait_for(
                    self._search(query, max_results),
                    timeout=REQUEST_TIMEOUT_SECONDS,
                )
            except asyncio.TimeoutError:
                last_error = "timeout"
                logger.warning(
                    "Playwright search timed out for %s (query=%r)",
                    self.retailer_key,
                    query,
                )
                continue
            except Exception as exc:  # noqa: BLE001
                last_error = f"{type(exc).__name__}: {exc}"
                logger.warning(
                    "Playwright search error for %s: %s",
                    self.retailer_key,
                    last_error,
                )
                break

            if outcome.bot_detected:
                self._record_failure("bot detection", bot_detected=True)
                return []

            if outcome.products:
                self._record_success(outcome.products)
                return outcome.products
            # Empty partial parse — treat as warning but return what we have.
            self._record_success(outcome.products)
            return outcome.products

        self._record_failure(last_error or "unknown error")
        return []

    def get_status(self) -> dict[str, Any]:
        snap = self._status
        return {
            "retailer": self.retailer_key,
            "lastSuccessAt": _iso(snap.last_success_at),
            "lastFailureAt": _iso(snap.last_failure_at),
            "lastFailureReason": snap.last_failure_reason,
            "recentResultCount": snap.recent_result_count,
            "botDetected": snap.bot_detected,
        }

    @abstractmethod
    async def _search(
        self, query: str, max_results: int
    ) -> ScrapeOutcome:  # pragma: no cover - abstract
        ...

    # Internal helpers -------------------------------------------------------

    def _record_success(self, products: list[RawScraperProduct]) -> None:
        self._status.last_success_at = datetime.now(timezone.utc)
        self._status.recent_result_count = len(products)
        self._status.bot_detected = False
        self._status.last_failure_reason = None

    def _record_failure(self, reason: str, *, bot_detected: bool = False) -> None:
        self._status.last_failure_at = datetime.now(timezone.utc)
        self._status.last_failure_reason = reason
        self._status.bot_detected = bot_detected


def _iso(value: Optional[datetime]) -> Optional[str]:
    return value.isoformat() if value else None
