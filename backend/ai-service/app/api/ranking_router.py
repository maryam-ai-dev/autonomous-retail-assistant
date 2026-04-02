"""Main ranking API route — orchestrates the full ranking pipeline."""

from fastapi import APIRouter, HTTPException

from app.constraint_filtering.filter import ConstraintFilter
from app.explainability.explainer import Explainer
from app.preference_modeling.modeler import PreferenceModeler
from app.ranking_strategies.strategy_selector import select_strategy
from app.schemas.ranking import RankedProduct, RankingRequest, RankingResponse
from app.trust_scoring.scorer import TrustScorer
from app.uncertainty_assessment.assessor import UncertaintyAssessor

router = APIRouter(prefix="/ranking", tags=["Ranking"])

constraint_filter = ConstraintFilter()
preference_modeler = PreferenceModeler()
explainer = Explainer()
trust_scorer = TrustScorer()
uncertainty_assessor = UncertaintyAssessor()


@router.post("/rank", response_model=RankingResponse)
async def rank_products(request: RankingRequest) -> RankingResponse:
    """Run the full ranking pipeline and return scored, explained results."""
    if not request.products:
        raise HTTPException(status_code=400, detail="No products provided for ranking")

    # 1. Constraint filtering
    filtered, filtered_count = constraint_filter.filter(
        request.products, request.user_preferences
    )

    if not filtered:
        return RankingResponse(
            ranked_products=[],
            strategy_used="none",
            confidence=0.0,
            uncertainty=uncertainty_assessor.assess([], {}),
            filtered_count=filtered_count,
            sources_used=[],
        )

    # 2. Preference modeling
    preference_vector = preference_modeler.model(request.user_preferences)
    # Carry through raw prefs for scoring modules
    preference_vector["budget_cap"] = request.user_preferences.get("budget_cap")
    preference_vector["blocked_brands"] = request.user_preferences.get("blocked_brands", [])

    # 3. Strategy selection
    strategy = select_strategy(request.query)

    # 4. Rank with chosen strategy
    ranked = strategy.rank(filtered, preference_vector)

    # 5. Explainer on top 5
    top_n = min(5, len(ranked))
    enriched: list[RankedProduct] = []
    for i, rp in enumerate(ranked):
        if i < top_n:
            explanation = explainer.explain(rp.product, rp.score, preference_vector)
            trust_score = trust_scorer.score(
                rp.product, rp.score, preference_vector
            )
            enriched.append(
                RankedProduct(
                    product=rp.product,
                    score=rp.score,
                    rank=rp.rank,
                    explanation=explanation,
                    trust_score=trust_score,
                )
            )
        else:
            enriched.append(rp)

    # 6. Uncertainty assessment on full list
    uncertainty = uncertainty_assessor.assess(enriched, preference_vector)

    # 7. Collect sources
    sources = list({p.source_name for p in request.products})

    return RankingResponse(
        ranked_products=enriched,
        strategy_used=strategy.name,
        confidence=uncertainty.confidence,
        uncertainty=uncertainty,
        filtered_count=filtered_count,
        sources_used=sources,
    )
