"""Pydantic schemas for basket-intent parsing."""

from __future__ import annotations

from decimal import Decimal
from enum import Enum

from pydantic import BaseModel, Field


class IntentCategory(str, Enum):
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
        default=IntentCategory.HEALTH_BEAUTY,
        description=(
            "Top-level category. Defaults to HEALTH_BEAUTY when the LLM cannot"
            " determine one. GROCERY is out of scope — see out_of_scope."
        ),
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
    out_of_scope: bool = Field(
        default=False,
        description=(
            "True when the user's intent is outside Aisleon's coverage (e.g. grocery"
            " — handled by NourishOS). When set, Spring returns 422 OUT_OF_SCOPE."
        ),
    )
    out_of_scope_reason: str | None = Field(
        default=None,
        description="Short reason string when out_of_scope is true (e.g. 'grocery').",
    )


class IntentParseRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=1000)


class IntentParseResponse(BaseModel):
    intent: ParsedIntent
    warnings: list[str] = Field(default_factory=list)
