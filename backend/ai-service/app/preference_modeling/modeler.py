"""Converts raw user preferences into a normalised preference vector for scoring."""

from typing import Dict, List


class PreferenceModeler:
    """Produces a preference vector from the user_preferences dict. Simple scoring, no embeddings."""

    def model(self, user_preferences: Dict) -> Dict:
        """Return a preference_vector dict with normalised signals."""
        return {
            "budget_sensitivity": self._budget_sensitivity(user_preferences),
            "brand_affinity": self._brand_affinity(user_preferences),
            "substitution_tolerance": self._substitution_tolerance(user_preferences),
            "approval_strictness": self._approval_strictness(user_preferences),
        }

    def _budget_sensitivity(self, prefs: Dict) -> float:
        """0 = no budget concern, 1 = very tight budget."""
        budget_cap = prefs.get("budget_cap")
        if budget_cap is None or budget_cap <= 0:
            return 0.0
        if budget_cap <= 20:
            return 1.0
        if budget_cap <= 50:
            return 0.8
        if budget_cap <= 100:
            return 0.5
        if budget_cap <= 500:
            return 0.3
        return 0.1

    def _brand_affinity(self, prefs: Dict) -> List[str]:
        """List of preferred brands (lowercased for matching)."""
        brands = prefs.get("preferred_brands", [])
        return [b.lower() for b in brands if isinstance(b, str)]

    def _substitution_tolerance(self, prefs: Dict) -> float:
        """0 = no substitutions, 1 = fully open to substitutions."""
        allow = prefs.get("allow_substitutions", True)
        if not allow:
            return 0.0
        max_delta = prefs.get("max_substitution_price_delta")
        if max_delta is None or max_delta <= 0:
            return 0.5
        if max_delta <= 5:
            return 0.3
        if max_delta <= 20:
            return 0.6
        return 0.9

    def _approval_strictness(self, prefs: Dict) -> float:
        """0 = no approval needed, 1 = very strict approval threshold."""
        threshold = prefs.get("approval_threshold")
        if threshold is None or threshold <= 0:
            return 0.0
        if threshold <= 10:
            return 1.0
        if threshold <= 30:
            return 0.8
        if threshold <= 50:
            return 0.5
        if threshold <= 100:
            return 0.3
        return 0.1
