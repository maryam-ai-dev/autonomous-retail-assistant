# 🛒 Aisleon

> Trust-aware autonomous retail assistant. Every purchase decision is a trust decision - products are scored, purchases are policy-gated, and every action is audit-logged.

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Next.js](https://img.shields.io/badge/Next.js-15-black?logo=next.js)](https://nextjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)

![Demo](DEMO_GIF_PLACEHOLDER)

## What it does

Aisleon autonomously discovers, evaluates, and purchases retail products on behalf of users - while enforcing trust constraints at every step. Products are ranked by trust scores before they reach the user. Purchases above budget thresholds require explicit approval. Every search, cart action, and checkout is recorded as a domain event. Unlike conventional shopping assistants, Leon treats every recommendation as a trust decision.

## Demo

![Demo](DEMO_GIF_PLACEHOLDER)

- Search for a product and get trust-scored results with reasoning
- Review policy warnings before anything hits your cart
- Approve or reject purchases above your budget threshold
- Inspect the full audit trail of every action taken on your behalf

## Architecture

```
Next.js (frontend)
    |
Spring Boot (authority layer) - DDD modules, REST API, domain events, policy enforcement
    |                    |
FastAPI              PostgreSQL 16
(intelligence layer) - ranking, trust scoring, substitution analysis
```

Key decisions:

- Spring Boot owns all canonical IDs and domain logic - FastAPI never makes authority decisions
- Every cross-module communication happens through domain events - no direct service coupling
- Policy rules live in Spring Boot - FastAPI scores and explains, but never approves
- Trust score is computed before any product reaches the UI - no unscored recommendations
- Every action is audit-logged as an immutable domain event

## Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 15, TypeScript, Tailwind CSS |
| Authority service | Spring Boot 3.3, Java 17, PostgreSQL 16 |
| Intelligence service | FastAPI, Python 3.11+, Pydantic |
| Infrastructure | Docker Compose, Flyway, JWT |

## Features

- Trust-scored product discovery - merchant reliability, constraint satisfaction, and substitution risk combined into a single score
- Policy-enforced approvals - purchases above budget threshold are blocked until explicitly approved
- Dual-connector discovery - eBay API with Playwright browser fallback for resilient sourcing
- Unsafe substitution detection - brand changes or price increases above 10% flagged for approval
- Full audit trail - every action recorded as a timestamped domain event
- Explainability layer - trust scores surfaced with reasoning, not just a number

## Trust model

Five core rules enforced at the authority layer:

1. Every product gets a trust score before it reaches the user
2. Purchases above the user's approval threshold are blocked until explicitly approved
3. Unapproved merchants generate policy warnings
4. Substitutions that change brand or increase price by more than 10% require approval
5. Every action - search, cart, approval, checkout - is recorded as an immutable event

## Domain events

| Event | Publisher | Consumer |
|---|---|---|
| `ProductCandidatesRankedEvent` | discovery | audit |
| `CartItemAddedEvent` | cart | audit |
| `ApprovalRequiredEvent` | cart | audit |
| `CartCheckedOutEvent` | cart | audit |
| `PurchaseAuthorizedEvent` | approval | audit, checkout |
| `PurchaseRejectedEvent` | approval | audit |
| `CheckoutCompletedEvent` | checkout | audit |

## Running locally

```bash
cp .env.example .env
docker-compose up -d
```

This starts PostgreSQL, Spring Boot, the AI service, and the Next.js frontend.

## Roadmap

- [ ] In-store robotics extension - ROS 2 navigation stack with Isaac Sim for physical customer guidance (planned future layer)
- [ ] Expanded connector coverage beyond eBay
- [ ] User preference learning over time
- [ ] Mobile app

## License

Apache 2.0

---

*Built by [Maryam Yousuf](https://github.com/maryam-ai-dev)*
