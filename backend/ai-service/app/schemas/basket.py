"""Pydantic schemas for the advisory basket generator."""

from __future__ import annotations

from decimal import Decimal

from pydantic import BaseModel, Field


class CandidateProduct(BaseModel):
    candidate_id: str = Field(..., min_length=1)
    name: str
    brand: str | None = None
    retailer: str
    price: Decimal = Field(..., ge=0)
    unit_price: Decimal | None = None
    unit_basis: str | None = None
    subcategory: str | None = None
    dietary_tags: list[str] = Field(default_factory=list)


class BasketGenerateRequest(BaseModel):
    raw_text: str = Field(..., min_length=1)
    budget: Decimal | None = None
    category: str = "GROCERY"
    dietary_requirements: list[str] = Field(default_factory=list)
    retailer_hints: list[str] = Field(default_factory=list)
    item_hints: list[str] = Field(default_factory=list)
    candidates: list[CandidateProduct] = Field(default_factory=list)


class GeneratedBasketItem(BaseModel):
    candidate_id: str
    quantity: int = Field(default=1, ge=1)
    reasoning: str = ""


class GeneratedDraft(BaseModel):
    items: list[GeneratedBasketItem] = Field(default_factory=list)
    total_cost: Decimal = Field(default=Decimal("0"))
    retry_count: int = 0
    warnings: list[str] = Field(default_factory=list)
