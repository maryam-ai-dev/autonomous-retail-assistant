"""FastAPI entry point for the Aisleon AI service."""

from fastapi import FastAPI

from app.api.ranking_router import router as ranking_router
from app.api.substitution_router import router as substitution_router

app = FastAPI(
    title="Aisleon AI Service",
    description="Intelligence layer — ranking, explainability, and trust scoring",
    version="0.1.0",
)

app.include_router(ranking_router)
app.include_router(substitution_router)


@app.get("/health")
async def health_check() -> dict[str, str]:
    """Return service health status."""
    return {"status": "ok"}
