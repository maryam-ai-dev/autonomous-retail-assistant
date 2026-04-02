"""Generates plain-language explanations for ranked products."""

from typing import Dict, List

from app.schemas.product import NormalizedProduct
from app.schemas.ranking import Explanation


class Explainer:
    """Produces human-readable explanations for why a product was ranked where it is."""

    def explain(
        self,
        product: NormalizedProduct,
        score: float,
        preference_vector: Dict,
    ) -> Explanation:
        """Return an Explanation for the given product and preference context."""
        reasons = self._build_reasons(product, preference_vector)
        tradeoffs = self._build_tradeoffs(product, preference_vector)
        budget_match = self._is_budget_match(product, preference_vector)
        brand_match = self._classify_brand(product, preference_vector)
        merchant_trust = self._classify_merchant(product)

        return Explanation(
            top_reasons=reasons[:3],
            tradeoffs=tradeoffs,
            budget_match=budget_match,
            brand_match=brand_match,
            merchant_trust=merchant_trust,
        )

    def _build_reasons(
        self, product: NormalizedProduct, pv: Dict
    ) -> List[str]:
        reasons: List[str] = []

        if self._is_budget_match(product, pv):
            reasons.append("Price fits within your budget")

        brand_class = self._classify_brand(product, pv)
        if brand_class == "preferred":
            reasons.append("From a preferred brand")

        rating = product.merchant_rating
        if rating is not None and rating >= 0.8:
            reasons.append("Highly rated seller")

        avail = (product.availability or "").upper().strip()
        if avail in ("IN_STOCK", "AVAILABLE"):
            reasons.append("Currently in stock")

        if product.shipping_cost is not None and product.shipping_cost == 0:
            reasons.append("Free shipping")

        if not reasons:
            reasons.append("Matches your search query")

        return reasons

    def _build_tradeoffs(
        self, product: NormalizedProduct, pv: Dict
    ) -> List[str]:
        tradeoffs: List[str] = []

        if not self._is_budget_match(product, pv):
            tradeoffs.append("Price exceeds your budget")

        brand_class = self._classify_brand(product, pv)
        if brand_class == "neutral":
            tradeoffs.append("Brand not in your preferences")

        rating = product.merchant_rating
        if rating is not None and rating < 0.5:
            tradeoffs.append("Seller has a lower rating")

        avail = (product.availability or "").upper().strip()
        if avail not in ("IN_STOCK", "AVAILABLE", ""):
            tradeoffs.append("May not be immediately available")

        return tradeoffs

    def _is_budget_match(self, product: NormalizedProduct, pv: Dict) -> bool:
        budget_cap = pv.get("budget_cap")
        if budget_cap is None or budget_cap <= 0:
            return True
        return product.price <= float(budget_cap)

    def _classify_brand(self, product: NormalizedProduct, pv: Dict) -> str:
        brand = (product.brand or "").lower()
        brand_affinity: List[str] = pv.get("brand_affinity", [])
        blocked: List[str] = [b.lower() for b in pv.get("blocked_brands", [])]

        if brand in blocked:
            return "blocked"
        if brand in brand_affinity:
            return "preferred"
        return "neutral"

    def _classify_merchant(self, product: NormalizedProduct) -> str:
        rating = product.merchant_rating
        if rating is None:
            return "medium"
        if rating >= 0.75:
            return "high"
        if rating >= 0.45:
            return "medium"
        return "low"
