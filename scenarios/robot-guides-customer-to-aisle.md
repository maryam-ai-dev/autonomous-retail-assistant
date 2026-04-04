# Robot Guides Customer to Aisle

A simulated robot navigates the store to guide a customer to the correct aisle for their selected product.

## ROS 2 Trigger

The scenario starts when a `GuideCustomer` action goal is sent to the task planner node, either via:

- The frontend robotics simulation page ("Guide Customer" button)
- The simulation bridge HTTP API (`POST http://localhost:8765/task`)
- Direct ROS 2 CLI command

```bash
ros2 action send_goal /guide_customer \
  retail_robot_interfaces/action/GuideCustomer \
  "{product_id: 'PROD_001', customer_id: 'USER_001'}"
```

## Navigation Flow

1. **Task planner** receives the `GuideCustomer` goal
2. **Shelf lookup** — the task planner calls the `/shelf_lookup` service to find the product's aisle and coordinates
3. If the product is not found, the action aborts with `PRODUCT_NOT_FOUND`
4. **Navigation task** — the task planner publishes a `NavigationTask` to `/navigation/task`
5. **Navigation node** receives the task and begins simulated movement:
   - Publishes `ASSIGNED` to `/navigation/status` immediately
   - Publishes `IN_PROGRESS` after 1 second
   - Publishes simulated pose updates to `/robot/pose` every 0.5 seconds (linear interpolation)
   - Publishes `COMPLETED` after 3 seconds
6. **Task planner** monitors `/navigation/status` and sends feedback updates (progress 0 to 1.0)
7. On `COMPLETED`, the action returns `success: true, final_status: COMPLETED`

## Handoff Fallback

If navigation fails or times out:

1. The task planner publishes a `HandoffRequest` to `/handoff/request`
2. The **handoff node** receives the request, logs it, and publishes `RECEIVED` to `/handoff/status`
3. After 1 second, the handoff node publishes `STAFF_NOTIFIED` and sets `/robot/status` to `WAITING_FOR_STAFF`
4. The action returns `success: false, final_status: NAVIGATION_FAILED_HANDOFF_TRIGGERED`
5. Staff can resolve the handoff via the `/handoff/resolve` ROS 2 service, which resets robot status to `IDLE`

This can be triggered by requesting an unknown product:

```bash
ros2 action send_goal /guide_customer \
  retail_robot_interfaces/action/GuideCustomer \
  "{product_id: 'PROD_UNKNOWN_999', customer_id: 'USER_001'}"
```

## Dashboard View

With the simulation bridge running on port 8765, the frontend robotics simulation page at `/robotics-simulation` shows:

- **Store map** — SVG top-down view with the robot's position updating in real time, target shelf highlighted during navigation
- **Task status** — current robot state (IDLE, NAVIGATING, WAITING_FOR_STAFF)
- **Navigation tracker** — active task ID, destination, status badge, progress bar
- **Handoff alert** — prominent warning when a handoff is active, with reason and "Mark as Resolved" button
- **Send task form** — dropdown of PROD_001 through PROD_010, customer ID input, "Guide Customer" button

## Modules Exercised

| Layer | Module |
|-------|--------|
| Robotics | StoreMapNode, NavigationNode, TaskPlannerNode, HandoffNode, SimulationBridge |
| Client | StoreMap, NavigationTracker, TaskStatus, HandoffAlert, simulation-service |
| Interfaces | NavigationTask.msg, HandoffRequest.msg, ShelfLookup.srv, GuideCustomer.action |
