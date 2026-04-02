"""Assesses uncertainty in ranking results and recommends next action."""

from typing import Dict, List

from app.schemas.ranking import RankedProduct, UncertaintyAssessment


class UncertaintyAssessor:
    """Evaluates how confident the system should be in the ranking output."""

    def assess(
        self,
        ranked_products: List[RankedProduct],
        preference_vector: Dict,
    ) -> UncertaintyAssessment:
        """Return an uncertainty assessment for the ranked results."""
        reasons: List[str] = []

        if not ranked_products:
            return UncertaintyAssessment(
                confidence=0.0,
                is_uncertain=True,
                reasons=["No products available to rank"],
                recommendation="escalate",
            )

        top_score_low = self._top_score_low(ranked_products)
        if top_score_low:
            reasons.append("Top-ranked product has a low confidence score")

        scores_close = self._top_scores_close(ranked_products)
        if scores_close:
            reasons.append(
                "Top two products are very close in score — hard to pick a clear winner"
            )

        same_merchant = self._all_same_merchant(ranked_products)
        if same_merchant:
            reasons.append("All results come from the same seller")

        no_preferred = self._no_preferred_brand(ranked_products, preference_vector)
        if no_preferred:
            reasons.append("None of the results match your preferred brands")

        is_uncertain = len(reasons) > 0
        confidence = self._compute_confidence(
            ranked_products, top_score_low, scores_close, same_merchant, no_preferred
        )
        recommendation = self._recommend(is_uncertain, confidence)

        return UncertaintyAssessment(
            confidence=round(confidence, 4),
            is_uncertain=is_uncertain,
            reasons=reasons,
            recommendation=recommendation,
        )

    def _top_score_low(self, ranked: List[RankedProduct]) -> bool:
        return ranked[0].score < 0.4

    def _top_scores_close(self, ranked: List[RankedProduct]) -> bool:
        if len(ranked) < 2:
            return False
        return abs(ranked[0].score - ranked[1].score) < 0.05

    def _all_same_merchant(self, ranked: List[RankedProduct]) -> bool:
        if len(ranked) < 2:
            return False
        first = ranked[0].product.merchant_name
        return all(rp.product.merchant_name == first for rp in ranked)

    def _no_preferred_brand(
        self, ranked: List[RankedProduct], pv: Dict
    ) -> bool:
        brand_affinity: List[str] = pv.get("brand_affinity", [])
        if not brand_affinity:
            return False
        for rp in ranked:
            brand = (rp.product.brand or "").lower()
            if brand in brand_affinity:
                return False
        return True

    def _compute_confidence(
        self,
        ranked: List[RankedProduct],
        top_low: bool,
        close: bool,
        same_merchant: bool,
        no_preferred: bool,
    ) -> float:
        base = ranked[0].score
        if top_low:
            base -= 0.2
        if close:
            base -= 0.15
        if same_merchant:
            base -= 0.1
        if no_preferred:
            base -= 0.05
        return max(0.0, min(1.0, base))

    def _recommend(self, is_uncertain: bool, confidence: float) -> str:
        if not is_uncertain:
            return "proceed"
        if confidence >= 0.5:
            return "ask_user"
        if confidence >= 0.25:
            return "require_approval"
        return "escalate"
