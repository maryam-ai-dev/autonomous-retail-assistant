"""Core product ranker that scores and sorts products by preference vector."""

from typing import Dict, List, Optional

from app.schemas.product import NormalizedProduct
from app.schemas.ranking import Explanation, RankedProduct, TrustScore

# Default weights for the weighted average
DEFAULT_WEIGHTS = {
    "price": 0.35,
    "brand": 0.25,
    "availability": 0.15,
    "merchant": 0.25,
}


class ProductRanker:
    """Scores and ranks products against a preference vector."""

    def __init__(self, weights: Optional[Dict[str, float]] = None) -> None:
        self.weights = weights or DEFAULT_WEIGHTS

    def rank(
        self,
        products: List[NormalizedProduct],
        preference_vector: Dict,
    ) -> List[RankedProduct]:
        """Score each product, sort descending, return ranked list."""
        scored = []
        for product in products:
            scores = self._score_product(product, preference_vector)
            total = self._weighted_total(scores)
            scored.append((product, scores, total))

        scored.sort(key=lambda x: x[2], reverse=True)

        ranked: List[RankedProduct] = []
        for i, (product, scores, total) in enumerate(scored):
            ranked.append(
                RankedProduct(
                    product=product,
                    score=round(total, 4),
                    rank=i + 1,
                    explanation=self._placeholder_explanation(),
                    trust_score=self._placeholder_trust_score(total),
                )
            )
        return ranked

    def _score_product(
        self, product: NormalizedProduct, pv: Dict
    ) -> Dict[str, float]:
        return {
            "price": self._price_score(product, pv),
            "brand": self._brand_score(product, pv),
            "availability": self._availability_score(product),
            "merchant": self._merchant_score(product),
        }

    def _price_score(self, product: NormalizedProduct, pv: Dict) -> float:
        budget_cap = pv.get("budget_cap")
        if budget_cap is None or budget_cap <= 0:
            return 0.5
        score = 1.0 - (product.price / float(budget_cap))
        return max(0.0, min(1.0, score))

    def _brand_score(self, product: NormalizedProduct, pv: Dict) -> float:
        brand_affinity: List[str] = pv.get("brand_affinity", [])
        blocked_brands: List[str] = [
            b.lower() for b in pv.get("blocked_brands", [])
        ]
        brand = (product.brand or "").lower()

        if brand in blocked_brands:
            return 0.0
        if brand in brand_affinity:
            return 1.0
        return 0.5

    def _availability_score(self, product: NormalizedProduct) -> float:
        avail = (product.availability or "").upper().strip()
        if avail in ("IN_STOCK", "AVAILABLE"):
            return 1.0
        return 0.5

    def _merchant_score(self, product: NormalizedProduct) -> float:
        if product.merchant_rating is None:
            return 0.5
        return max(0.0, min(1.0, product.merchant_rating))

    def _weighted_total(self, scores: Dict[str, float]) -> float:
        total = 0.0
        for key, weight in self.weights.items():
            total += scores.get(key, 0.5) * weight
        return total

    def _placeholder_explanation(self) -> Explanation:
        """Placeholder — replaced by Explainer in step 5.6."""
        return Explanation(
            top_reasons=[],
            tradeoffs=[],
            budget_match=False,
            brand_match="neutral",
            merchant_trust="medium",
        )

    def _placeholder_trust_score(self, total: float) -> TrustScore:
        """Placeholder — replaced by TrustScorer in step 5.8."""
        return TrustScore(
            recommendation_confidence=total,
            constraint_satisfaction=1.0,
            substitution_risk="none",
            merchant_trust="medium",
            actionability="buy",
            overall_trust_score=total,
        )
