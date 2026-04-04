"""Substitution analysis — evaluates safety and risk of product substitutions."""

from typing import List

from pydantic import BaseModel

from app.schemas.product import NormalizedProduct


class SubstitutionAnalysis(BaseModel):
    is_safe_substitution: bool
    price_delta: float
    price_delta_percent: float
    brand_changed: bool
    requires_consent: bool
    requires_approval: bool
    risk_level: str
    reasons: List[str]


class SubstitutionAnalyser:
    """Compares an original product with a proposed substitute and returns risk analysis."""

    def analyse(
        self,
        original_product: NormalizedProduct,
        substitute_product: NormalizedProduct,
    ) -> SubstitutionAnalysis:
        """Analyse substitution risk between original and substitute products."""
        price_delta = substitute_product.price - original_product.price
        price_delta_percent = (
            (price_delta / original_product.price) * 100
            if original_product.price > 0
            else 0.0
        )

        original_brand = (original_product.brand or "Unknown").strip().lower()
        substitute_brand = (substitute_product.brand or "Unknown").strip().lower()
        brand_changed = original_brand != substitute_brand

        requires_consent = price_delta > 0 or brand_changed
        requires_approval = abs(price_delta_percent) > 10

        risk_level = self._compute_risk_level(
            price_delta_percent, brand_changed, requires_approval
        )

        reasons = self._build_reasons(
            price_delta, price_delta_percent, brand_changed,
            requires_consent, requires_approval, risk_level
        )

        is_safe = risk_level == "low" and not requires_approval

        return SubstitutionAnalysis(
            is_safe_substitution=is_safe,
            price_delta=round(price_delta, 2),
            price_delta_percent=round(price_delta_percent, 2),
            brand_changed=brand_changed,
            requires_consent=requires_consent,
            requires_approval=requires_approval,
            risk_level=risk_level,
            reasons=reasons,
        )

    def _compute_risk_level(
        self,
        price_delta_percent: float,
        brand_changed: bool,
        requires_approval: bool,
    ) -> str:
        if requires_approval or abs(price_delta_percent) > 10:
            return "high"
        if brand_changed or abs(price_delta_percent) > 5:
            return "medium"
        return "low"

    def _build_reasons(
        self,
        price_delta: float,
        price_delta_percent: float,
        brand_changed: bool,
        requires_consent: bool,
        requires_approval: bool,
        risk_level: str,
    ) -> List[str]:
        reasons: List[str] = []

        if price_delta > 0:
            reasons.append(
                f"Price increased by ${price_delta:.2f} ({price_delta_percent:.1f}%)"
            )
        elif price_delta < 0:
            reasons.append(
                f"Price decreased by ${abs(price_delta):.2f} ({abs(price_delta_percent):.1f}%)"
            )
        else:
            reasons.append("Price unchanged")

        if brand_changed:
            reasons.append("Brand changed from original product")

        if requires_approval:
            reasons.append("Price change exceeds 10% threshold — approval required")

        if requires_consent and not requires_approval:
            reasons.append("User consent recommended before proceeding")

        if risk_level == "low":
            reasons.append("Substitution is within acceptable parameters")

        return reasons
