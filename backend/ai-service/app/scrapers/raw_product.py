"""Raw scraper output schema — mirrors Spring's RawScraperProduct."""

from __future__ import annotations

from typing import Optional

from pydantic import BaseModel, Field


class RawScraperProduct(BaseModel):
    external_id: str
    display_name: str
    brand: Optional[str] = None
    category: str = "GROCERY"
    subcategory: str = "UNKNOWN"
    price: Optional[float] = None
    price_from_text: bool = False
    unit_price: Optional[float] = None
    unit_basis: Optional[str] = None
    size_text: Optional[str] = None
    image_url: Optional[str] = None
    product_url: Optional[str] = None
    is_available: bool = True
    is_basketable: bool = True
    certification_tags: list[str] = Field(default_factory=list)
    offer_flags: list[str] = Field(default_factory=list)
    source_fetched_at: str
    normalization_warnings: list[str] = Field(default_factory=list)
