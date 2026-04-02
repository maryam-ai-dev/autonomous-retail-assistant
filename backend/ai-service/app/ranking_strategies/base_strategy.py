"""Abstract base class for ranking strategies."""

from abc import ABC, abstractmethod
from typing import Dict, List

from app.schemas.product import NormalizedProduct
from app.schemas.ranking import RankedProduct


class RankingStrategy(ABC):
    """Each strategy defines its own weight distribution for scoring."""

    @abstractmethod
    def rank(
        self,
        products: List[NormalizedProduct],
        preference_vector: Dict,
    ) -> List[RankedProduct]:
        """Score and rank products using this strategy's weights."""

    @property
    @abstractmethod
    def name(self) -> str:
        """Strategy name for reporting."""
