# Unsafe Substitution Blocked

The assistant detects an unsafe product substitution and blocks it before it reaches the cart.

## Detection

When a substitute product is proposed (e.g. the original product is out of stock and an alternative is suggested), the Python intelligence layer analyses the substitution for safety:

1. The frontend or backend calls `POST /substitution/analyse` with the original and substitute `NormalizedProduct`
2. `SubstitutionAnalyser.analyse()` compares the two products across multiple dimensions:
   - **Price delta** — absolute and percentage difference
   - **Brand change** — whether the brand is different
   - **Risk level** — computed from price delta and brand change

## Risk Classification

| Risk Level | Conditions |
|-----------|------------|
| Low | Price delta <= 5%, same brand |
| Medium | Price delta 5-10% or brand changed |
| High | Price delta > 10% (requires approval) |

## Policy Engine Response

The `SubstitutionAnalysis` result includes:

- `is_safe_substitution` — false if risk level is not low or approval is required
- `requires_consent` — true if price increased or brand changed (user should confirm)
- `requires_approval` — true if price delta exceeds 10% (must go through approval flow)
- `reasons` — plain English explanations (e.g. "Price increased by $15.00 (15.0%)", "Price change exceeds 10% threshold — approval required")

## What the User Sees

- The product card for a substitute shows the risk level badge and the price delta
- If the substitution requires approval, the user is informed that adding this item will trigger an approval step at checkout
- The reasons list explains in plain language why the substitution was flagged

## Domain Events

When a cart containing an unsafe substitution proceeds to checkout:

| Event | When |
|-------|------|
| APPROVAL_REQUIRED | Checkout detects the substitution violates the user's substitution policy |
| APPROVAL_REQUESTED | An approval request is created with the substitution as the trigger reason |

## How to Trigger

```bash
# Call the substitution analysis endpoint directly:
curl -X POST http://localhost:8000/substitution/analyse \
  -H "Content-Type: application/json" \
  -d '{
    "original_product": {
      "source_type": "api", "source_name": "ebay",
      "title": "Widget A", "price": 100.0,
      "merchant_name": "Store1", "brand": "BrandX"
    },
    "substitute_product": {
      "source_type": "api", "source_name": "ebay",
      "title": "Widget B", "price": 115.0,
      "merchant_name": "Store2", "brand": "BrandX"
    }
  }'
```

Expected response: `risk_level: "high"`, `requires_approval: true`.
