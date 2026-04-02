"""Similarity-oriented ranking strategy — prioritises brand match."""

from typing import Dict, List

from app.ranking.ranker import ProductRanker
from app.ranking_strategies.base_strategy import RankingStrategy
from app.schemas.product import NormalizedProduct
from app.schemas.ranking import RankedProduct

WEIGHTS = {"price": 0.2, "brand": 0.6, "availability": 0.0, "merchant": 0.2}


class SimilarityRanker(RankingStrategy):

    def __init__(self) -> None:
        self._ranker = ProductRanker(weights=WEIGHTS)

    def rank(
        self,
        products: List[NormalizedProduct],
        preference_vector: Dict,
    ) -> List[RankedProduct]:
        return self._ranker.rank(products, preference_vector)

    @property
    def name(self) -> str:
        return "similarity"
