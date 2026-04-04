# Aisleon

A trust-aware autonomous retail assistant with a software-first architecture and simulated robotics extension.

## Overview

Aisleon is a multi-service system that autonomously discovers, evaluates, and purchases retail products on behalf of users while enforcing trust constraints at every step. Unlike conventional shopping assistants, Aisleon treats every purchase decision as a trust decision - products are ranked by trust scores, purchases require policy-based approval, and every action is audit-logged. The system extends into a simulated robotics layer where a ROS 2 robot navigates a virtual store to guide customers to products.

## Key Highlights

- **Trust-first purchasing** - every product is scored for trust (merchant reliability, constraint satisfaction, substitution risk) before it reaches the user
- **Policy-enforced approvals** - purchases that exceed budget thresholds or involve unapproved merchants are held for explicit user approval
- **Full audit trail** - every search, cart action, approval decision, and checkout is recorded as a domain event
- **Dual-connector discovery** - eBay API connector with Playwright browser fallback for resilient product sourcing
- **Simulated robotics** - ROS 2 navigation stack with Isaac Sim integration for in-store customer guidance scenarios

## System Overview

```
┌──────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                         │
│              Next.js · App Router · Tailwind             │
│   Search │ Cart │ Approvals │ Trust │ Audit │ Robotics   │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────┐
│                  AUTHORITY LAYER                          │
│           Spring Boot · Java 17 · DDD · Events           │
│  Discovery │ Cart │ Policy │ Approval │ Checkout │ Audit  │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────┐
│                INTELLIGENCE LAYER                         │
│              FastAPI · Python 3.11+ · Pydantic            │
│    Ranking │ Explainability │ Trust Scoring │ Substitution │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                  ROBOTICS LAYER                           │
│            ROS 2 · Isaac Sim · WebSocket Bridge           │
│  Navigation │ Task Planner │ Handoff │ Store Map │ Bridge │
└──────────────────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Client | Next.js 15, TypeScript, Tailwind CSS | Dashboard, search, cart, approvals, robotics UI |
| Authority | Spring Boot 3.3, Java 17, PostgreSQL 16 | Domain logic, API, auth, persistence, events |
| Intelligence | FastAPI, Python 3.11+, Pydantic | Ranking, trust scoring, explainability, substitution analysis |
| Robotics | ROS 2, Isaac Sim | Navigation, task planning, handoff, simulation |
| Infrastructure | Docker Compose, Flyway, JWT | Orchestration, migrations, authentication |

## Repo Structure

```
aisleon/
├── backend/
│   ├── spring-boot/       # Authority layer — DDD modules, REST API, domain events
│   ├── ai-service/        # Intelligence layer — ranking, trust scoring, substitution
│   └── contracts/         # Shared API contracts (normalized product schema)
├── frontend/              # Client layer — Next.js App Router dashboard
├── robotics/
│   ├── ros2_ws/           # ROS 2 workspace — nodes, interfaces, launch files
│   └── simulation/        # Isaac Sim worlds, robot config, scenarios
├── docs/                  # Architecture docs, trust model, ADRs
├── scenarios/             # End-to-end scenario descriptions
├── docker-compose.yml     # Local development orchestration
└── sprint-plan-final.md   # Full sprint plan (18 sprints)
```

## Quick Start

### Backend + Frontend (Docker)

```bash
cp .env.example .env
docker-compose up -d
```

This starts PostgreSQL, Spring Boot, the AI service, and the Next.js frontend.

### Backend (without Docker)

```bash
# Terminal 1 — PostgreSQL (requires local install or Docker)
docker run -d --name aisleon-db -e POSTGRES_DB=aisleon -e POSTGRES_USER=aisleon -e POSTGRES_PASSWORD=aisleon -p 5432:5432 postgres:16-alpine

# Terminal 2 — Spring Boot
cd backend/spring-boot
cp ../../.env.example ../../.env
./mvnw spring-boot:run

# Terminal 3 — AI Service
cd backend/ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000

# Terminal 4 — Frontend
cd frontend
npm install
npm run dev
```

### ROS 2 (separate)

```bash
cd robotics/ros2_ws
colcon build
source install/setup.bash
ros2 launch retail_robot_bringup retail_robot.launch.py
```

The simulation bridge serves robot state at `http://localhost:8765/state`.

## Domain Modelling

The Spring Boot backend uses two modelling approaches:

**DDD modules** (rich domain logic): discovery, cart, policy, approval, checkout, merchant, preferences
- Domain objects are pure Java with no framework annotations
- JPA entities live in `infrastructure/` with explicit mappers
- Application services orchestrate domain logic and publish events

**CRUD modules** (simple persistence): identity/auth, identity/profile, audit
- Standard controller → service → repository flow
- No domain layer needed

**Domain events** drive cross-module communication:

| Event | Publisher | Consumer |
|-------|----------|----------|
| ProductCandidatesRankedEvent | discovery | audit |
| CartItemAddedEvent | cart | audit |
| ApprovalRequiredEvent | cart | audit |
| CartCheckedOutEvent | cart | audit |
| ApprovalRequestedEvent | approval | audit |
| PurchaseAuthorizedEvent | approval | audit, checkout |
| PurchaseRejectedEvent | approval | audit |
| CheckoutCompletedEvent | checkout | audit |
| CheckoutFailedEvent | checkout | audit |

## Trust Model

The system enforces five core trust rules:

1. **Every product gets a trust score** — combining merchant reliability, constraint satisfaction, substitution risk, and recommendation confidence into an overall score
2. **Purchases above threshold require approval** — if the cart total exceeds the user's approval threshold, checkout is blocked until explicitly approved
3. **Unapproved merchants trigger warnings** — items from merchants not on the approved list generate policy warnings
4. **Unsafe substitutions are flagged** — if a substitute product changes brand or increases price by more than 10%, it requires approval
5. **Every action is auditable** — searches, cart changes, approvals, and checkouts are recorded as timestamped domain events

## Scenarios

- [Guided Product Search](scenarios/guided-product-search.md) - search, rank, trust-score, and display products
- [Approval-Required Purchase](scenarios/approval-required-purchase.md) - policy triggers, approval flow, checkout
- [Unsafe Substitution Blocked](scenarios/unsafe-substitution-blocked.md) - substitution analysis and risk detection
- [Robot Guides Customer to Aisle](scenarios/robot-guides-customer-to-aisle.md) - ROS 2 navigation and handoff

## Isaac Sim

The robotics layer includes configuration for NVIDIA Isaac Sim, but Isaac Sim is **not required** to run the system. All ROS 2 nodes run independently with simulated navigation timing. Isaac Sim adds visual simulation for those with compatible hardware:

- NVIDIA GPU with 8 GB+ VRAM (RTX 2070 or higher)
- NVIDIA driver 525.60+
- Isaac Sim 2023.1.0+
- 32 GB system RAM recommended

See [robotics/simulation/isaac_sim/worlds/README.md](robotics/simulation/isaac_sim/worlds/README.md) for setup details.

## License

[Apache License 2.0](LICENSE) - Copyright 2026 Maryam Yousuf
