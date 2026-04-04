# Approval-Required Purchase

A purchase exceeds policy thresholds and requires explicit human approval before checkout proceeds.

## Trigger Conditions

An approval is required when any of these conditions are detected during checkout:

- The cart total exceeds the user's configured approval threshold
- One or more items come from a merchant not on the approved merchant list
- The cart contains a substituted product and substitutions are disallowed in user preferences
- A substituted product's price delta exceeds the user's maximum substitution price delta

## Flow

1. The user adds items to their cart on the `/cart` page
2. The user clicks "Checkout" which calls `POST /api/cart/checkout`
3. `CartService.checkoutCart()` loads the user's retail preferences and builds a `PurchasePolicy`
4. `PolicyEvaluationService.evaluate()` checks the cart against the policy
5. The policy detects a violation (e.g. total of $150 exceeds the $100 approval threshold)
6. `CartService` calls `ApprovalService.createApprovalRequest()` with the trigger reason
7. `ApprovalRequestedEvent` is published and recorded in the audit log
8. `ApprovalRequiredEvent` is published with the policy warnings
9. The frontend receives an `APPROVAL_REQUIRED` response with the approval ID and reasons
10. The user navigates to `/approvals` and sees the pending request with the trigger reason
11. The user clicks "Approve" which calls `POST /api/approvals/{id}/approve`
12. `ApprovalService.approve()` transitions the request to APPROVED and publishes `PurchaseAuthorizedEvent`
13. `CheckoutService` listens for the event and auto-triggers `executeCheckout()`
14. The mock marketplace executor runs, saves a `CheckoutOrder`, and publishes `CheckoutCompletedEvent`
15. The cart status is updated to CHECKED_OUT

## What the User Sees

- **Cart page**: checkout button returns a warning with the trigger reason and a link to the approvals page
- **Approvals page**: a queue showing pending requests with the trigger reason, total amount, and approve/reject buttons
- **Audit log**: the full chain of events — APPROVAL_REQUIRED, APPROVAL_REQUESTED, PURCHASE_AUTHORIZED, CHECKOUT_COMPLETED

## Audit Events

| Order | Event | Details |
|-------|-------|---------|
| 1 | APPROVAL_REQUIRED | Cart ID, total amount, trigger reason, policy warnings |
| 2 | APPROVAL_REQUESTED | Approval ID, user, cart, reason, amount |
| 3 | PURCHASE_AUTHORIZED | Approval ID, user, cart, amount |
| 4 | CHECKOUT_COMPLETED | Order ID, merchant order ref, amount |
