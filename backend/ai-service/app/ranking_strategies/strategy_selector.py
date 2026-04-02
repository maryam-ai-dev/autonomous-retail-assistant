"""Selects a ranking strategy based on query keywords."""

from app.ranking_strategies.base_strategy import RankingStrategy
from app.ranking_strategies.replenishment_ranker import ReplenishmentRanker
from app.ranking_strategies.similarity_ranker import SimilarityRanker
from app.ranking_strategies.value_ranker import ValueRanker

VALUE_KEYWORDS = {"cheapest", "budget", "cheap"}
SIMILARITY_KEYWORDS = {"same brand", "usual", "regular"}
REPLENISHMENT_KEYWORDS = {"urgent", "need now", "restock"}


def select_strategy(query: str) -> RankingStrategy:
    """Match query keywords to a ranking strategy. Defaults to value ranker."""
    lower = query.lower()

    for keyword in REPLENISHMENT_KEYWORDS:
        if keyword in lower:
            return ReplenishmentRanker()

    for keyword in SIMILARITY_KEYWORDS:
        if keyword in lower:
            return SimilarityRanker()

    for keyword in VALUE_KEYWORDS:
        if keyword in lower:
            return ValueRanker()

    return ValueRanker()
