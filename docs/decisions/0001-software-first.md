# ADR 0001: Software-First Architecture

## Status

Accepted

## Context

Aisleon includes a robotics layer for in-store customer guidance. Building and testing against real robotic hardware introduces significant cost, lead time, and environment constraints. Most of the system's value — product discovery, trust scoring, policy enforcement, approval workflows — is entirely software-based and does not depend on physical robots.

We needed an architecture that lets the full software stack be developed, tested, and demonstrated independently of any robotics hardware, while still supporting a realistic robotics integration path.

## Decision

Adopt a software-first architecture where the robotics layer is a simulation extension, not a prerequisite:

- The authority layer (Spring Boot), intelligence layer (FastAPI), and client layer (Next.js) are fully functional without any robotics components
- The ROS 2 workspace runs independently with simulated navigation timing (no physics engine required)
- NVIDIA Isaac Sim provides optional visual simulation for those with compatible hardware
- A lightweight HTTP bridge (port 8765) connects the ROS 2 nodes to the frontend dashboard for development and demo purposes

## Consequences

**Benefits:**
- The core product (trust-aware purchasing) can be developed and shipped without robotics hardware
- Frontend, backend, and AI service can be tested with standard CI — no GPU or ROS 2 install needed
- The robotics layer can evolve independently without blocking software releases
- Demo and portfolio presentation work on any machine

**Trade-offs:**
- The simulation bridge on port 8765 is a development convenience, not production transport — it would need to be replaced with a proper ROS 2-to-cloud bridge for a real deployment
- Isaac Sim integration is configuration-only (YAML and scenario files) — there is no working `.usd` world file, and building one requires an NVIDIA GPU with 8 GB+ VRAM and Isaac Sim installed
- Simulated navigation uses fixed timing (3-second completion) rather than physics-based path planning, so the demo does not reflect real-world navigation complexity
