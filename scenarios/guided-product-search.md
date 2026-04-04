# Guided Product Search

A customer searches for a product and the assistant guides them through ranked, trust-scored results.

## User Intent

The user wants to find a specific type of product (e.g. "wireless headphones") and see options ranked by how well they match their preferences, with clear trust indicators to help them decide.

## System Flow

1. The user types a query into the search form on the `/search` page
2. The frontend sends `POST /api/discovery/search` with the query and the user's ID
3. Spring Boot loads the user's retail preferences (budget cap, preferred brands, blocked brands, approval threshold)
4. The `ConnectorSelectionService` calls the eBay API connector to search for products
5. If eBay returns fewer than 3 results, the Playwright browser connector runs as fallback against Google Shopping
6. Results from both connectors are deduplicated and normalized into `NormalizedProduct` records
7. Spring Boot calls the Python service at `POST /ranking/rank` with the normalized products, query, and user preferences
8. The Python service runs the full pipeline: constraint filtering, preference modelling, strategy selection, ranking, explainability, trust scoring, and uncertainty assessment
9. Each of the top 5 products receives a trust score (overall, constraint satisfaction, merchant trust, substitution risk) and a human-readable explanation
10. The ranked results are returned to the frontend and displayed as product cards with confidence badges

## Outcome

The user sees products ranked by relevance to their preferences, each with:
- A confidence badge (high/medium/low) based on the overall trust score
- An explanation of why the product was ranked where it is (e.g. "within budget", "preferred brand", "high seller rating")
- Tradeoffs flagged transparently (e.g. "brand not in your preferred list", "slightly above budget")

## Modules Exercised

| Layer | Module |
|-------|--------|
| Authority | Discovery (ConnectorSelectionService, EbayApiConnector, PlaywrightBrowserConnector, ProductNormalizationService) |
| Intelligence | ConstraintFilter, PreferenceModeler, StrategySelector, ProductRanker, Explainer, TrustScorer, UncertaintyAssessor |
| Client | SearchForm, SearchResultsGrid, ProductCard, RecommendationPanel |
| Audit | ProductCandidatesRankedEvent logged |

## How to Trigger

```bash
# With backend and AI service running:
curl -X POST http://localhost:8080/api/discovery/search \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"query": "wireless headphones"}'
```

Or use the search form on the frontend at `http://localhost:3000/search`.
