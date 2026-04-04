# Frontend

Next.js 15 dashboard for the Aisleon trust-aware retail assistant.

## App Router Structure

The frontend uses the Next.js App Router with a `(dashboard)` route group for authenticated pages. The route group does not appear in URLs.

```
src/app/
├── (dashboard)/
│   ├── layout.tsx            # Sidebar + main content layout
│   ├── search/               # /search — product discovery
│   ├── cart/                  # /cart — cart management
│   ├── approvals/            # /approvals — approval queue
│   ├── trust/                # /trust — trust center (merchants, budget, substitution)
│   ├── audit-log/            # /audit-log — event timeline
│   ├── preferences/          # /preferences — retail preferences
│   ├── profile/              # /profile — user profile
│   ├── recommendations/      # /recommendations — product recommendations
│   └── robotics-simulation/  # /robotics-simulation — ROS 2 robot dashboard
├── login/                    # /login — authentication
├── signup/                   # /signup — registration
└── page.tsx                  # / — landing page
```

## Feature-First Organisation

Components, hooks, services, and types are grouped by feature under `src/features/`:

```
src/features/
├── auth/                     # Login, signup, auth service
├── product-search/           # Search form, results grid, product cards
├── cart/                     # Cart items, summary, checkout
├── approvals/                # Approval queue, detail modal
├── trust-center/             # Merchant list, budget, substitution settings
├── audit-log/                # Event timeline, type filters
├── profile/                  # Profile display and edit
├── retail-preferences/       # Preferences form
├── recommendations/          # Recommendation panel
└── robotics-simulation/      # Store map, navigation tracker, task controls
```

Shared UI primitives live in `src/shared/ui/`. API client and endpoint definitions live in `src/core/api/`.

## Running Locally

```bash
npm install
npm run dev
```

The app starts at `http://localhost:3000`. Requires the Spring Boot backend at the URL specified in `NEXT_PUBLIC_API_URL`.

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Spring Boot backend base URL | `http://localhost:8080` |

Auth tokens are stored in cookies (`auth_token`), not localStorage. The middleware reads cookies server-side for route protection.

## Key Conventions

- **TypeScript strict mode** — no `any` types unless commented
- **Functional components only** — no class components
- **Tailwind CSS only** — no inline styles
- **API calls through `src/core/api/client.ts`** — never call `fetch` directly in components (the robotics simulation service is an exception as it connects to the ROS 2 bridge, not the Spring Boot API)
- **Endpoints in `src/core/api/endpoints.ts`** — no hardcoded URLs in components
