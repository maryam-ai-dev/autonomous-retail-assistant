"""Advisory basket generation — Claude Sonnet picks candidates to propose.

Spring Boot is the final authority on budget and dietary constraints; this
module is explicitly a proposal layer. See Phase B6 in SPRINT_PLAN_BACKEND.md.
"""

from app.basket_generation.generator import BasketGenerator

__all__ = ["BasketGenerator"]
