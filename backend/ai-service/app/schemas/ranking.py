"""Pydantic models for the ranking pipeline request and response."""

from typing import Dict, List, Optional

from pydantic import BaseModel

from app.schemas.product import NormalizedProduct


class RankingRequest(BaseModel):
    query: str
    products: List[NormalizedProduct]
    user_preferences: Dict


class TrustScore(BaseModel):
    recommendation_confidence: float
    constraint_satisfaction: float
    substitution_risk: str
    merchant_trust: str
    actionability: str
    overall_trust_score: float


class Explanation(BaseModel):
    top_reasons: List[str]
    tradeoffs: List[str]
    budget_match: bool
    brand_match: str
    merchant_trust: str


class UncertaintyAssessment(BaseModel):
    confidence: float
    is_uncertain: bool
    reasons: List[str]
    recommendation: str


class RankedProduct(BaseModel):
    product: NormalizedProduct
    score: float
    rank: int
    explanation: Explanation
    trust_score: TrustScore


class RankingResponse(BaseModel):
    ranked_products: List[RankedProduct]
    strategy_used: str
    confidence: float
    uncertainty: UncertaintyAssessment
    filtered_count: int
    sources_used: List[str]
