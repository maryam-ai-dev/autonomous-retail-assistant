# Backend

The backend consists of two services and a shared contracts folder.

## Structure

| Folder | Service | Purpose |
|--------|---------|---------|
| `spring-boot/` | Authority layer | Domain logic, REST API, authentication, persistence, domain events |
| `ai-service/` | Intelligence layer | Product ranking, trust scoring, explainability, substitution analysis |
| `contracts/` | Shared schemas | Normalized product JSON schema used as the contract between services |

The Spring Boot service calls the Python AI service synchronously during product discovery (`POST /ranking/rank` and `POST /substitution/analyse`). Both services must be running for search to work.

## Running Locally (without Docker)

### Spring Boot

Requires Java 17+ and a PostgreSQL 16 instance.

```bash
# Start PostgreSQL (or use Docker)
docker run -d --name aisleon-db \
  -e POSTGRES_DB=aisleon \
  -e POSTGRES_USER=aisleon \
  -e POSTGRES_PASSWORD=aisleon \
  -p 5432:5432 postgres:16-alpine

# Copy environment variables
cp ../.env.example ../.env

# Run Spring Boot
cd spring-boot
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`. Flyway migrations run automatically on startup.

### AI Service

Requires Python 3.11+.

```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

The service starts at `http://localhost:8000`. Health check at `GET /health`.

## Module Ownership

### Spring Boot — DDD Modules

| Module | Responsibility |
|--------|---------------|
| discovery | Connector orchestration, normalization, Python service integration |
| cart | Cart aggregate, item management, checkout initiation |
| policy | Policy evaluation against user preferences and merchant list |
| approval | Approval request state machine (PENDING/APPROVED/REJECTED) |
| checkout | Checkout execution with mock marketplace executor |
| merchant | Merchant registry with approval status |
| preferences | User retail preferences (budget, brands, substitution rules) |

### Spring Boot — CRUD Modules

| Module | Responsibility |
|--------|---------------|
| identity/auth | JWT registration and login |
| identity/profile | User profile management |
| audit | Append-only event log with async listeners |

### AI Service Modules

| Module | Responsibility |
|--------|---------------|
| constraint_filtering | Remove products violating hard constraints |
| preference_modeling | Normalize user preferences into a preference vector |
| ranking_strategies | Strategy selection (value, similarity, replenishment) |
| ranking | Score and sort products by weighted criteria |
| explainability | Generate human-readable explanations per product |
| trust_scoring | Compute per-product trust scores |
| uncertainty_assessment | Evaluate confidence in ranking results |
| substitution_analysis | Analyse safety of product substitutions |
