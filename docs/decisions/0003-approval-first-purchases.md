# ADR 0003: Approval-First Purchases

## Status

Accepted

## Context

Aisleon is an autonomous purchasing assistant — it discovers products and can execute purchases on behalf of the user. This creates a sensitive trust boundary: an AI system spending real money without explicit human confirmation for every transaction.

Early design exploration considered two approaches:
1. **Auto-purchase with undo** — buy immediately, let the user cancel within a window
2. **Approval-first** — hold purchases for user review when trust conditions are not fully met

The auto-purchase model is faster but risks spending money the user did not intend to spend. For a system that needs to build user trust over time, the cost of an unwanted purchase far outweighs the inconvenience of an approval step.

## Decision

Adopt an approval-first purchase model:

- Every checkout evaluates the cart against the user's policies (budget threshold, merchant approval status, substitution rules)
- If all policies pass, checkout proceeds immediately — no unnecessary friction for trusted purchases
- If any policy is violated, an `ApprovalRequest` is created and the purchase is held until the user explicitly approves or rejects it
- The approval trigger reason is recorded and shown to the user so they understand why approval was needed
- Approved purchases are executed automatically via `PurchaseAuthorizedEvent`

This is not a blanket "confirm every purchase" gate. It is a risk-proportional model: low-risk purchases flow through, high-risk purchases pause.

## Consequences

**Benefits:**
- Users never have an unwanted purchase — the system errs on the side of caution
- Trust is built incrementally — as users configure preferences and approve merchants, fewer purchases require approval
- The approval trigger reason provides transparency, teaching users how the system evaluates risk
- The audit trail captures every approval decision, creating accountability

**Trade-offs:**
- Purchases that require approval are not instant — there is latency between checkout and execution
- Users with strict policies may find that most purchases require approval, which could feel cumbersome
- The current implementation uses a single approval threshold; more granular per-category or per-merchant thresholds would improve the experience but add complexity
