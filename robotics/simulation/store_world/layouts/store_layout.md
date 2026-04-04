# Store Layout

The demo retail store is a 20m x 15m single-floor space.

## Zones

- **Entrance** (x: 8-12, y: 0-1) — customer entry point at the bottom-center of the store.
- **Checkout area** (x: 0-20, y: 0-2) — spans the full width at the front of the store, directly behind the entrance.
- **Staff station** (x: 18-20, y: 12-15) — located at the back-right corner, where staff are stationed for handoff requests.

## Aisles

Four aisles (A, B, C, D) run north-south from y=3 to y=12, spaced evenly across the store width. Each aisle has three shelf sections.

| Aisle | X center | Shelves |
|-------|----------|---------|
| A     | 2.0      | A1 (left, y: 3-6), A2 (right, y: 3-6), A3 (left, y: 7-10) |
| B     | 7.0      | B1 (left, y: 3-6), B2 (right, y: 3-6), B3 (left, y: 7-10) |
| C     | 12.0     | C1 (left, y: 3-6), C2 (right, y: 3-6), C3 (left, y: 7-10) |
| D     | 17.0     | D1 (left, y: 3-6), D2 (right, y: 3-6), D3 (left, y: 7-10) |

## Coordinate system

- Origin (0, 0) is the front-left corner of the store.
- X increases to the right (0 to 20).
- Y increases toward the back (0 to 15).
- The robot starts at the entrance area and navigates through the aisles to reach shelf locations.
