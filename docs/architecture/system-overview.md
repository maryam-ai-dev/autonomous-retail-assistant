# System Overview

## Four-Layer Architecture

Aisleon is structured as four independent layers, each with a distinct responsibility and runtime:

**Client Layer** (Next.js) — the user-facing dashboard. Renders search, cart, approvals, trust center, audit log, and robotics simulation pages. Communicates with the authority layer over REST and with the robotics bridge over HTTP polling.

**Authority Layer** (Spring Boot) — the single source of truth. Owns all domain logic, persistence, authentication, and event publishing. Enforces business rules through DDD aggregates and policy evaluation. Exposes a REST API consumed by the frontend.

**Intelligence Layer** (FastAPI) — stateless analytical service. Receives normalized products and user preferences, returns ranked results with trust scores, explanations, and uncertainty assessments. Also provides substitution risk analysis. Called synchronously by the authority layer during discovery.

**Robotics Layer** (ROS 2) — simulated in-store navigation. A set of ROS 2 nodes handle store mapping, path planning, navigation execution, and human handoff. A lightweight HTTP bridge on port 8765 exposes robot state to the frontend.

## Main Request Flow

A typical product discovery and purchase follows this path:

1. **User searches** — the frontend sends a query to `POST /api/discovery/search`
2. **Connector selection** — Spring Boot calls the eBay API connector; if fewer than 3 results, the Playwright browser connector runs as fallback
3. **Normalization** — raw connector results are cleaned and mapped to `NormalizedProduct`
4. **Ranking** — Spring Boot calls the Python service at `POST /ranking/rank` with products and user preferences
5. **Trust scoring** — the Python service scores each top product for recommendation confidence, constraint satisfaction, substitution risk, and merchant trust
6. **Explainability** — the Python service generates human-readable explanations and tradeoffs for each ranked product
7. **Response** — ranked products with trust scores and explanations are returned to the frontend
8. **Cart** — the user adds products to their cart via `POST /api/cart/items`
9. **Checkout** — `POST /api/cart/checkout` evaluates policies against the cart
10. **Policy evaluation** — if the cart triggers policy violations (budget exceeded, unapproved merchant), an approval request is created
11. **Approval** — the user reviews and approves or rejects the purchase via `POST /api/approvals/{id}/approve`
12. **Checkout execution** — on approval, `PurchaseAuthorizedEvent` triggers `CheckoutService`, which runs the mock marketplace executor
13. **Audit** — every step publishes domain events consumed by async audit listeners

## Approval Flow

```
Cart Checkout
    │
    ├── Policy passes ──────────────► Cart marked CHECKED_OUT
    │
    └── Policy violations found
            │
            ├── Blocked (hard constraint) ──► Checkout rejected
            │
            └── Approval required ──────────► ApprovalRequest created
                                                     │
                                    ┌────────────────┼────────────────┐
                                    │                                 │
                              User approves                    User rejects
                                    │                                 │
                        PurchaseAuthorizedEvent            PurchaseRejectedEvent
                                    │                                 │
                        CheckoutService executes              Cart remains active
                                    │
                            ┌───────┴───────┐
                            │               │
                      Success           Failure
                            │               │
                CheckoutCompletedEvent  CheckoutFailedEvent
```

## Service Boundaries

| Service | Port | Responsibility |
|---------|------|---------------|
| PostgreSQL | 5432 | Persistence for all authority layer data |
| Spring Boot | 8080 | REST API, domain logic, auth, events, connectors |
| FastAPI | 8000 | Ranking, trust scoring, explainability, substitution analysis |
| Next.js | 3000 | Dashboard UI, server-side rendering |
| ROS 2 Bridge | 8765 | Robot state HTTP API (development/demo only) |

## Domain Modelling

### DDD Modules

These modules have rich business logic that justifies a full domain layer:

| Module | Why DDD |
|--------|---------|
| Cart | Aggregate with item management, total calculation, status transitions |
| Approval | State machine (PENDING → APPROVED/REJECTED/EXPIRED) with invariants |
| Checkout | Status transitions (INITIATED → PROCESSING → COMPLETED/FAILED) |
| Policy | Evaluation engine with multiple constraint types |
| Discovery | Connector orchestration, normalization, ranking integration |
| Merchant | Approval status, source type classification |
| Preferences | User-specific constraints that drive policy evaluation |

Each DDD module follows: `domain/` (pure Java) → `application/` (orchestration) → `infrastructure/` (JPA, mappers) → `interfaces/` (REST controllers).

### CRUD Modules

These modules are thin persistence layers without complex business rules:

| Module | Why CRUD |
|--------|----------|
| Identity/Auth | Standard JWT registration and login |
| Identity/Profile | Simple user profile reads and updates |
| Audit | Append-only event log — no domain logic, just writes and queries |

### Event-Driven Boundaries

Modules communicate through Spring `ApplicationEventPublisher`. Events are:
- Published only from application services
- Consumed asynchronously by audit listeners via `@Async("auditExecutor")`
- Never published from domain objects or controllers

This decouples the audit module from all other modules — it only knows about event types, not about the services that publish them.

### Bounded Contexts

| Context | Modules | Owns |
|---------|---------|------|
| Shopping | discovery, cart, policy | Product search, cart management, policy evaluation |
| Governance | approval, checkout | Purchase authorization, execution |
| Identity | auth, profile, preferences | Users, credentials, retail preferences |
| Catalog | merchant | Merchant registry, approval status |
| Observability | audit | Event logging, timeline queries |
| Robotics | ROS 2 nodes | Navigation, task planning, handoff |

### Key Invariants

- A cart item cannot be added if the same `externalProductId` already exists in the cart
- An approval request can only transition from PENDING — approve/reject on a non-PENDING request throws
- A checkout order can only complete or fail from PROCESSING state
- Trust scores below 0.45 are classified as "low confidence" and surfaced prominently
- Substitutions with price increase > 10% require explicit approval

### Domain Events

| Event | Published By | Consumed By |
|-------|-------------|-------------|
| ProductCandidatesRankedEvent | discovery | audit |
| CartItemAddedEvent | cart | audit |
| ApprovalRequiredEvent | cart | audit |
| CartCheckedOutEvent | cart | audit |
| ApprovalRequestedEvent | approval | audit |
| PurchaseAuthorizedEvent | approval | audit, checkout |
| PurchaseRejectedEvent | approval | audit |
| CheckoutCompletedEvent | checkout | audit |
| CheckoutFailedEvent | checkout | audit |

## Connector Architecture

Product discovery uses a two-tier connector strategy:

**Tier 1 — API connectors** (preferred): The eBay API connector authenticates via OAuth2 client credentials and searches the eBay Browse API. Results are mapped to `NormalizedProduct` with `sourceType=API`.

**Tier 2 — Browser connectors** (fallback): If the API returns fewer than 3 results or fails entirely, the Playwright browser connector scrapes Google Shopping results. Products are mapped with `sourceType=BROWSER` and `sourceName="browser_generic"`.

**Normalization**: All products from both tiers pass through `ProductNormalizationService`, which validates required fields, cleans strings, and ensures consistent pricing. The normalized list is then sent to the Python ranking service.

**Deduplication**: When both connectors contribute results, products are deduplicated by `externalProductId` and by title similarity (case-insensitive containment) before normalization.
