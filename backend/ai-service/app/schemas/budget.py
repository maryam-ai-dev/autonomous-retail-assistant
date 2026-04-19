"""Schemas for the /budget/insights endpoint."""

from __future__ import annotations

from decimal import Decimal

from pydantic import BaseModel, Field


class BudgetSummary(BaseModel):
    """Mirror of the Spring `BudgetSummary` DTO — what we receive as input."""

    month: str = Field(..., description="ISO month, e.g. '2026-04'.")
    spent: Decimal = Decimal("0")
    budget: Decimal = Decimal("0")
    saved_vs_full_price: Decimal = Field(default=Decimal("0"), alias="savedVsFullPrice")
    by_retailer: dict[str, Decimal] = Field(default_factory=dict, alias="byRetailer")
    basket_count: int = Field(default=0, alias="basketCount")

    model_config = {"populate_by_name": True}


class BudgetInsightsRequest(BaseModel):
    summary: BudgetSummary


class BudgetInsightsResponse(BaseModel):
    insights: list[str]
    warnings: list[str] = Field(default_factory=list)
