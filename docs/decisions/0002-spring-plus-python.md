# ADR 0002: Spring Boot + Python Split

## Status

Accepted

## Context

The system has two fundamentally different jobs:

1. **Domain logic and persistence** — managing users, carts, approvals, checkout, merchants, audit logs, and enforcing business rules. This is transactional, stateful, and benefits from a mature web framework with strong typing and ORM support.

2. **Analytical processing** — ranking products, scoring trust, generating explanations, assessing uncertainty, and analysing substitution risk. This is stateless, numerical, and benefits from Python's data science ecosystem and rapid prototyping speed.

Combining both in a single service would force one language to do a job it is not best suited for, and would couple deployment of business logic changes with analytical model changes.

## Decision

Split the backend into two services:

- **Spring Boot (Java 17)** — authority layer. Owns all persistence, authentication, domain events, and business rule enforcement. Calls the Python service synchronously during discovery.
- **FastAPI (Python 3.11+)** — intelligence layer. Stateless service that receives normalized products and user preferences, returns ranked results with trust scores and explanations.

The two services communicate over HTTP. The Spring Boot service calls `POST /ranking/rank` and `POST /substitution/analyse` on the Python service. The Python service has no database and no knowledge of users or carts.

## Consequences

**Benefits:**
- Each service uses the language best suited to its job — Java for domain logic, Python for analytical processing
- Teams (or future contributors) can work on ranking algorithms without touching business logic, and vice versa
- Services can be deployed and scaled independently
- The Python service can be replaced or supplemented with ML models without changing the authority layer

**Trade-offs:**
- Two services means a contract is needed between them — the `NormalizedProduct` schema in `backend/contracts/` serves as this contract
- Network latency is added to the discovery flow (one synchronous HTTP call per search)
- Both services must be running for product search to work — the Spring Boot service does not cache or fall back if the Python service is unavailable
