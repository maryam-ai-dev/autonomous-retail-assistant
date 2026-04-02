"""Deterministic constraint filter that removes products violating user preferences."""

from typing import Dict, List, Tuple

from app.schemas.product import NormalizedProduct

VALID_AVAILABILITY = {"IN_STOCK", "AVAILABLE", ""}


class ConstraintFilter:
    """Filters products based on hard constraints from user preferences."""

    def filter(
        self,
        products: List[NormalizedProduct],
        user_preferences: Dict,
    ) -> Tuple[List[NormalizedProduct], int]:
        """Return (filtered_products, removed_count)."""
        budget_cap = user_preferences.get("budget_cap")
        blocked_brands = {
            b.lower() for b in user_preferences.get("blocked_brands", [])
        }
        blocked_categories = {
            c.lower() for c in user_preferences.get("blocked_categories", [])
        }

        filtered: List[NormalizedProduct] = []
        removed = 0

        for product in products:
            if self._exceeds_budget(product, budget_cap):
                removed += 1
                continue

            if self._brand_blocked(product, blocked_brands):
                removed += 1
                continue

            if self._category_blocked(product, blocked_categories):
                removed += 1
                continue

            if self._unavailable(product):
                removed += 1
                continue

            filtered.append(product)

        return filtered, removed

    def _exceeds_budget(
        self, product: NormalizedProduct, budget_cap: object
    ) -> bool:
        if budget_cap is None or budget_cap <= 0:
            return False
        return product.price > float(budget_cap)

    def _brand_blocked(
        self, product: NormalizedProduct, blocked_brands: set
    ) -> bool:
        if not blocked_brands:
            return False
        brand = (product.brand or "").lower()
        return brand in blocked_brands

    def _category_blocked(
        self, product: NormalizedProduct, blocked_categories: set
    ) -> bool:
        if not blocked_categories:
            return False
        category = (product.category or "").lower()
        return category in blocked_categories

    def _unavailable(self, product: NormalizedProduct) -> bool:
        availability = (product.availability or "").upper().strip()
        if availability == "":
            return False
        return availability not in VALID_AVAILABILITY
