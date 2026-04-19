"""Pydantic schemas for basket-intent parsing."""

from __future__ import annotations

from decimal import Decimal
from enum import Enum

from pydantic import BaseModel, Field


class IntentCategory(str, Enum):
    GROCERY = "GROCERY"
    HEALTH_BEAUTY = "HEALTH_BEAUTY"
    GENERAL_MERCHANDISE = "GENERAL_MERCHANDISE"
    FASHION = "FASHION"
    ELECTRONICS = "ELECTRONICS"
    MIXED = "MIXED"


class ParsedIntent(BaseModel):
    raw_text: str = Field(..., description="The raw user input that was parsed.")
    budget: Decimal | None = Field(
        default=None,
        description="Total budget in GBP. Null if the user did not state a budget — never guessed.",
    )
    currency: str = Field(default="GBP")
    category: IntentCategory = Field(
        default=IntentCategory.GROCERY,
        description="Top-level category. Defaults to GROCERY when the LLM cannot determine one.",
    )
    subcategories: list[str] = Field(default_factory=list)
    dietary_requirements: list[str] = Field(
        default_factory=list,
        description="Uppercase dietary tags extracted from the input (e.g. HALAL, VEGAN).",
    )
    retailer_hints: list[str] = Field(default_factory=list)
    item_hints: list[str] = Field(default_factory=list)
    timing: str | None = None
    notes: str | None = None


class IntentParseRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=1000)


class IntentParseResponse(BaseModel):
    intent: ParsedIntent
    warnings: list[str] = Field(default_factory=list)
