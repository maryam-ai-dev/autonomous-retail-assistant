"""FastAPI entry point for the Aisleon AI service."""

from fastapi import FastAPI

app = FastAPI(
    title="Aisleon AI Service",
    description="Intelligence layer — ranking, explainability, and trust scoring",
    version="0.1.0",
)


@app.get("/health")
async def health_check() -> dict[str, str]:
    """Return service health status."""
    return {"status": "ok"}
