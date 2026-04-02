"""Computes per-product trust scores based on ranking and user preferences."""

from typing import Dict

from app.schemas.product import NormalizedProduct
from app.schemas.ranking import TrustScore


class TrustScorer:
    """Produces a TrustScore for a single ranked product in context of user preferences."""

    def score(
        self,
        product: NormalizedProduct,
        ranking_score: float,
        preference_vector: Dict,
    ) -> TrustScore:
        recommendation_confidence = ranking_score
        constraint_satisfaction = self._constraint_satisfaction(product, preference_vector)
        substitution_risk = self._substitution_risk(product, preference_vector)
        merchant_trust = self._merchant_trust(product)
        actionability = self._actionability(
            recommendation_confidence, constraint_satisfaction, substitution_risk, preference_vector
        )
        overall = self._overall(
            recommendation_confidence, constraint_satisfaction, merchant_trust
        )

        return TrustScore(
            recommendation_confidence=round(recommendation_confidence, 4),
            constraint_satisfaction=round(constraint_satisfaction, 4),
            substitution_risk=substitution_risk,
            merchant_trust=merchant_trust,
            actionability=actionability,
            overall_trust_score=round(overall, 4),
        )

    def _constraint_satisfaction(
        self, product: NormalizedProduct, pv: Dict
    ) -> float:
        score = 1.0

        budget_cap = pv.get("budget_cap")
        if budget_cap is not None and budget_cap > 0:
            if product.price > float(budget_cap):
                score -= 0.4
            elif product.price > float(budget_cap) * 0.9:
                score -= 0.1

        brand = (product.brand or "").lower()
        blocked = [b.lower() for b in pv.get("blocked_brands", [])]
        if brand in blocked:
            score -= 0.5

        affinity = pv.get("brand_affinity", [])
        if affinity and brand not in affinity:
            score -= 0.1

        return max(0.0, min(1.0, score))

    def _substitution_risk(
        self, product: NormalizedProduct, pv: Dict
    ) -> str:
        tolerance = pv.get("substitution_tolerance", 0.5)
        brand = (product.brand or "").lower()
        affinity = pv.get("brand_affinity", [])

        if not affinity:
            return "none"

        if brand in affinity:
            return "none"

        if tolerance >= 0.7:
            return "low"
        if tolerance >= 0.4:
            return "medium"
        return "high"

    def _merchant_trust(self, product: NormalizedProduct) -> str:
        rating = product.merchant_rating
        if rating is None:
            return "medium"
        if rating >= 0.75:
            return "high"
        if rating >= 0.45:
            return "medium"
        return "low"

    def _actionability(
        self,
        confidence: float,
        constraint_sat: float,
        sub_risk: str,
        pv: Dict,
    ) -> str:
        if constraint_sat < 0.3:
            return "block"

        if sub_risk == "high":
            return "needs_user_input"

        approval_strictness = pv.get("approval_strictness", 0.0)
        if approval_strictness > 0.7 and confidence < 0.6:
            return "needs_approval"

        if confidence < 0.4 or constraint_sat < 0.6:
            return "needs_user_input"

        return "safe_to_proceed"

    def _overall(
        self,
        confidence: float,
        constraint_sat: float,
        merchant_trust_label: str,
    ) -> float:
        merchant_val = {"high": 1.0, "medium": 0.6, "low": 0.3}.get(
            merchant_trust_label, 0.5
        )
        overall = (confidence * 0.4) + (constraint_sat * 0.35) + (merchant_val * 0.25)
        return max(0.0, min(1.0, overall))
