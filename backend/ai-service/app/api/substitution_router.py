"""Substitution analysis API route."""

from fastapi import APIRouter
from pydantic import BaseModel

from app.schemas.product import NormalizedProduct
from app.substitution_analysis.analyser import SubstitutionAnalyser, SubstitutionAnalysis

router = APIRouter(prefix="/substitution", tags=["Substitution"])

analyser = SubstitutionAnalyser()


class SubstitutionRequest(BaseModel):
    original_product: NormalizedProduct
    substitute_product: NormalizedProduct


@router.post("/analyse", response_model=SubstitutionAnalysis)
async def analyse_substitution(request: SubstitutionRequest) -> SubstitutionAnalysis:
    """Analyse risk of substituting one product for another."""
    return analyser.analyse(request.original_product, request.substitute_product)
