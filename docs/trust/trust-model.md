# Trust Model

## Core Philosophy

Aisleon treats every autonomous purchase as a trust decision. The system is designed around the principle that an AI assistant spending money on behalf of a user must earn and demonstrate trust at every step. No purchase proceeds without the user understanding why it was recommended, what the risks are, and having the ability to intervene.

Trust is not binary. It is a continuous score composed of multiple dimensions, and the system's response varies based on how confident it is in each dimension. High-trust actions proceed automatically. Low-trust actions pause for human input. Uncertain situations are flagged transparently.

## Trust Rules

| # | Rule | Plain Language | Enforced By |
|---|------|---------------|-------------|
| 1 | Budget enforcement | Products exceeding the user's budget cap are filtered out before ranking | Intelligence layer (ConstraintFilter) |
| 2 | Blocked brand exclusion | Products from brands the user has blocked are removed entirely | Intelligence layer (ConstraintFilter) |
| 3 | Merchant approval check | Items from merchants not on the approved list generate policy warnings during checkout | Authority layer (PolicyEvaluationService) |
| 4 | Approval threshold gate | If the cart total exceeds the user's approval threshold, checkout is blocked until explicitly approved | Authority layer (CartService, ApprovalService) |
| 5 | Substitution safety analysis | Substitutes with brand changes or price increases > 10% are flagged as high risk and require approval | Intelligence layer (SubstitutionAnalyser) |
| 6 | Trust score transparency | Every ranked product shows its trust score breakdown so users can see why it was recommended | Intelligence layer (TrustScorer) |
| 7 | Uncertainty disclosure | When the system is not confident in its ranking, it says so and recommends user review | Intelligence layer (UncertaintyAssessor) |
| 8 | Full audit trail | Every action (search, cart change, approval, checkout) is recorded as a timestamped event | Authority layer (AuditEventListener) |

## Approval Triggers

A purchase requires explicit user approval when any of these conditions are met:

- **Budget threshold exceeded** — the cart total is above the user's configured approval threshold
- **Unapproved merchant** — one or more cart items come from a merchant not on the approved merchant list
- **Substitution disallowed** — the cart contains a substituted product and the user's preferences do not allow substitutions
- **Substitution price delta** — a substituted product's price exceeds the user's maximum substitution price delta

When an approval is required, the system creates an `ApprovalRequest` with a `triggerReason` explaining why. The cart remains active but cannot proceed to checkout until the request is approved or rejected.

## Trust Scoring

Each ranked product receives a `TrustScore` with six components:

| Component | What It Measures | How It's Calculated |
|-----------|-----------------|-------------------|
| recommendation_confidence | How well the product matches the query and preferences | Derived from the weighted ranking score (price, brand, availability, merchant) |
| constraint_satisfaction | Whether the product meets hard constraints | Starts at 1.0, penalized for budget exceeded (-0.4), near budget (-0.1), blocked brand (-0.5), non-preferred brand (-0.1) |
| substitution_risk | Risk level if this product is a substitute | none/low/medium/high based on brand affinity match and substitution tolerance |
| merchant_trust | Reliability of the seller | high (rating >= 0.75), medium (>= 0.45), low (< 0.45) |
| actionability | What the user should do | safe_to_proceed / needs_user_input / needs_approval / block |
| overall_trust_score | Weighted composite | 0.4 x confidence + 0.35 x constraint_satisfaction + 0.25 x merchant_trust |

The frontend uses the overall trust score to display confidence badges:
- **High** — overall_trust_score >= 0.75 (green)
- **Medium** — overall_trust_score >= 0.45 (yellow)
- **Low** — overall_trust_score < 0.45 (red)

## Uncertainty Handling

The `UncertaintyAssessor` evaluates confidence in the overall ranking result, not just individual products. It checks for:

- **Low top score** — the best-ranked product scored poorly
- **Close top scores** — the top two products are nearly tied, making the recommendation ambiguous
- **Single merchant dominance** — all top results come from the same merchant
- **No preferred brands** — none of the user's preferred brands appear in results

Based on severity, it recommends one of:
- `proceed` — results are reliable
- `ask_user` — present results but flag uncertainty
- `require_approval` — do not auto-proceed
- `escalate` — results are too unreliable to act on

## Audit Trail

Every trust-relevant action generates a domain event captured in the audit log:

| Event | What It Records |
|-------|----------------|
| PRODUCTS_RANKED | Search query, top product titles, confidence, strategy, sources used |
| CART_CHECKOUT_INITIATED | Cart ID, user, total amount, item count |
| APPROVAL_REQUIRED | Cart ID, total amount, trigger reason, policy warnings |
| APPROVAL_REQUESTED | Approval ID, cart ID, trigger reason, total amount |
| PURCHASE_AUTHORIZED | Approval ID, user, cart ID, total amount |
| PURCHASE_REJECTED | Approval ID, user, cart ID, total amount |
| CHECKOUT_COMPLETED | Order ID, merchant order ref, total amount |
| CHECKOUT_FAILED | Cart ID, failure reason |

Audit events are persisted asynchronously via `@Async("auditExecutor")` so they never block the main transaction. The audit log is append-only and queryable by user, event type, and time range through the `GET /api/audit` endpoint.
