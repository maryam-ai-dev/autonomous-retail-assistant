# Aisleon

A budget-first AI shopping agent with a social layer for UK shoppers.

## Overview

Aisleon turns a free-text basket intent ("weekly groceries under £70, halal") into a DRAFT basket assembled across UK retailers - Tesco, Sainsbury's, Boots, and Argos - matched to the user's taste profile and budget. Spring Boot owns the final truth on basket validity (budget, dietary, size, retailer rules); FastAPI proposes candidates and wording via Claude Sonnet but never enforces constraints. A social feed lets users share approved baskets, fork them into their own, and follow shoppers with similar taste.

## Key Highlights

- **Budget authority is explicit** - Spring re-validates every FastAPI-proposed basket, trims lowest-priority items until within budget, and rejects untrimmable intents with 422
- **Deterministic constraints, not LLM guesswork** - dietary (halal/vegan/vegetarian), size, retailer allow/deny, and substitution detection are enforced by `BasketConstraintEngine`, not by the model
- **Resilient connectors** - Tesco via Apify, Sainsbury's / Boots / Argos via Playwright, per-retailer retry + timeout + Resilience4j circuit breakers, 6h Redis cache with stale fallback
- **Inline basket approval** - `POST /api/baskets/{id}/approve` with substitution-flag gating; no separate approval queue (the old `/api/approvals/*` routes now return 410 Gone)
- **Social + shared baskets** - posts with reactions (`TRIED_THIS`, `BETTER_ALT`, `WOULDNT_RECOMMEND`), cursor-paginated feed, follows, polymorphic comments, public share links, and fork-to-your-own-basket
- **Budget history + insights** - monthly aggregation from BASKET_APPROVED audit events, plus Claude-generated insights with a hallucination guard that drops any figure not present in the summary

## System Overview

```
┌──────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                         │
│        Next.js 16 · App Router · React 19 · Tailwind 4   │
│  Search │ Feed │ Basket │ Budget │ Profile │ Settings    │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────┐
│                  AUTHORITY LAYER                          │
│          Spring Boot 3.3 · Java 17 · DDD · Events         │
│  basket-intent │ catalogue │ scraping │ policy │ approval │
│  cart │ checkout │ merchant │ social │ shared_baskets     │
│  budget │ preferences │ identity │ audit                  │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────┐
│                INTELLIGENCE LAYER                         │
│           FastAPI · Python 3.11 · Claude Sonnet           │
│  intent_parsing │ basket_generation │ budget_insights     │
│  scrapers (Playwright) │ ranking │ substitution_analysis  │
└──────────────────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Client | Next.js 16, React 19, TypeScript strict, Tailwind 4 | App shell, basket flow, social feed, budget views |
| Authority | Spring Boot 3.3, Java 17, PostgreSQL 16, Flyway, JWT | Domain logic, REST API, final basket validation, event publishing, audit log |
| Intelligence | FastAPI, Python 3.11, Pydantic, Claude Sonnet (`claude-sonnet-4-20250514`), Playwright | Intent parsing, basket generation, budget insights, Playwright scrapers |
| Scraping | Apify (Tesco), Playwright (Sainsbury's / Boots / Argos) | UK retailer product discovery |
| Cache | Redis (6h TTL, stale fallback < 24h) | Catalogue result cache |
| Infrastructure | Docker Compose, Flyway, Resilience4j | Orchestration, migrations, circuit breakers |

## Repo Structure

```
aisleon/
├── backend/
│   ├── spring-boot/      # Authority — DDD modules, REST API, domain events
│   │   └── src/main/java/com/aisleon/
│   │       ├── basket/          # intents, candidates, generation, approval
│   │       ├── catalogue/       # NormalizedProduct, dedup, dietary tagging
│   │       ├── scraping/        # connector registry, cache, circuit breakers
│   │       ├── policy/          # basket constraint engine
│   │       ├── cart/ checkout/ merchant/ approval/
│   │       ├── social/          # posts, feed, follows, comments, reactions
│   │       ├── shared_baskets/  # share + public view + fork
│   │       ├── budget/          # monthly summary aggregation
│   │       ├── preferences/     # taste + clothing profiles
│   │       ├── identity/        # auth (JWT), profile
│   │       └── audit/           # append-only event log
│   ├── ai-service/       # Intelligence — FastAPI + Claude Sonnet
│   │   └── app/
│   │       ├── intent_parsing/  basket_generation/  budget_insights/
│   │       ├── scrapers/        # Playwright connectors
│   │       └── ranking/         substitution_analysis/
│   └── contracts/        # OpenAPI spec + shared JSON schemas
├── frontend/             # Next.js App Router client
├── scenarios/            # End-to-end scenario descriptions
├── docs/                 # Architecture + ADRs
├── _archived/            # Robotics prototype (removed per sprint B11.1)
├── SPRINT_PLAN_BACKEND.md  SPRINT_PLAN_FRONTEND.md
├── CLAUDE.md             # Source of truth for agent + conventions
├── docker-compose.yml
```

## Quick Start

### Full stack (Docker)

```bash
cp .env.example .env
# Fill in JWT_SECRET, POSTGRES_*, ANTHROPIC_API_KEY, APIFY_API_KEY
docker compose up
```

This starts PostgreSQL, Spring Boot, FastAPI, and the Next.js frontend.

### Services individually

```bash
# Spring Boot (requires Postgres + .env)
cd backend/spring-boot && ./mvnw spring-boot:run

# FastAPI intelligence service (requires ANTHROPIC_API_KEY for live LLM calls)
cd backend/ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000

# Frontend
cd frontend
npm install
npm run dev
# Or with mock fixtures — no backend needed:
# echo 'NEXT_PUBLIC_USE_MOCKS=true' >> .env.local && npm run dev
```

### Running Flyway migrations manually

```bash
cd backend/spring-boot && ./mvnw flyway:migrate
```

## Domain Modelling

Spring Boot uses two modelling approaches:

**DDD modules** (rich domain logic): `basket-intent`, `catalogue`, `scraping`, `cart`, `policy`, `approval`, `checkout`, `merchant`, `social`, `shared_baskets`, `budget-history`
- Domain objects are pure Java records with no framework annotations
- JPA entities live in `infrastructure/` with explicit mappers
- Application services orchestrate domain logic and publish events

**CRUD modules** (simple persistence): `identity/auth`, `identity/profile`, `preferences`, `audit`, `common`
- Standard controller → service → repository flow

**First-class basket concepts** (do not conflate):

| Concept | What it is |
|---|---|
| `BasketIntent` | Raw user text + parsed structured fields |
| `CandidatePool` | NormalizedProducts after constraint filtering |
| `GeneratedDraft` | FastAPI output — a proposal, not validated yet |
| `Basket` (DRAFT) | Persisted, Spring-validated, awaiting user review |
| `Basket` (APPROVED) | User-approved, ready for checkout handoff |
| `Basket` (CHECKED_OUT) | User confirmed purchase on retailer site |
| `SharedBasket` | Public snapshot of an APPROVED basket |

**Domain events** drive cross-module communication and feed the append-only audit log:

| Event | Publisher | Consumers |
|-------|----------|-----------|
| `BasketGeneratedEvent` | basket | audit |
| `BasketApprovedEvent` | basket | audit, budget-history |
| `SubstitutionAcceptedEvent` | basket | audit |
| `ApprovalRequiredEvent` | cart | audit |
| `PurchaseAuthorizedEvent` | approval | audit, checkout |
| `CheckoutCompletedEvent` | checkout | audit |

## Budget Authority & Constraints

Spring Boot owns the final truth on basket validity. FastAPI is advisory.

1. **`BasketConstraintEngine` (Spring)** - pre-generation filter. Removes candidates that violate hard rules (budget ceiling, halal filter on `MEAT_POULTRY`, fashion size, retailer allow/deny) before they ever reach the LLM
2. **FastAPI basket generator** - advisory. Prioritises and explains candidates; retries once with a "reduce cost" nudge if over budget
3. **Spring post-generation validation** - final authority. Re-validates against budget, trims lowest-priority items (`price / confidenceScore` ratio) until within budget, rejects with 422 `BUDGET_TOO_LOW_FOR_INTENT` if untrimmable
4. **Spring on basket approval** - final gate. `basket.totalCost ≤ intent.budget` is re-checked before `DRAFT → APPROVED` transition; unresolved substitution flags return 409

`HALAL_VERIFIED` items are never trimmed before `HALAL_LIKELY` / `HALAL_UNKNOWN` equivalents exist, and items with a resolved substitution flag are deprioritised for trimming — the user explicitly kept them.

## Scenarios

- [Guided Product Search](scenarios/guided-product-search.md) — intent parsing, candidate selection, ranking
- [Approval-Required Purchase](scenarios/approval-required-purchase.md) — inline approval, substitution-flag gating, checkout handoff
- [Unsafe Substitution Blocked](scenarios/unsafe-substitution-blocked.md) — substitution detection and user review

## License

[Apache License 2.0](LICENSE) — Copyright 2026 Maryam Yousuf
