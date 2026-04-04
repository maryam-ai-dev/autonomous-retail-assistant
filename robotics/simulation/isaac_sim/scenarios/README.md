# Scenarios

## Guide Customer to Product

Demonstrates the full happy-path: a customer requests help finding a product, the robot looks up the shelf location, navigates there, and reports success.

### Prerequisites

Start all nodes:

```bash
ros2 launch retail_robot_bringup retail_robot.launch.py
```

### Run

```bash
ros2 action send_goal /guide_customer \
  retail_robot_interfaces/action/GuideCustomer \
  "{product_id: 'PROD_001', customer_id: 'USER_001'}"
```

### What to observe

- `/navigation/status` transitions: ASSIGNED -> IN_PROGRESS -> COMPLETED
- `/robot/pose` updates as the robot moves toward the shelf
- Action result returns `success: true, final_status: COMPLETED`
- `GET http://localhost:8765/state` reflects the navigation progress in real time

---

## Human Handoff on Low Confidence

Tests the failure path: the robot is asked to find a product that does not exist in the store map. ShelfLookup returns not found, and the action aborts.

### Run

```bash
ros2 action send_goal /guide_customer \
  retail_robot_interfaces/action/GuideCustomer \
  "{product_id: 'PROD_UNKNOWN_999', customer_id: 'USER_001'}"
```

### What to observe

- ShelfLookup returns `found: false` for the unknown product
- Action result returns `success: false, final_status: PRODUCT_NOT_FOUND`
- No navigation task is published (the robot stays in place)
- If a navigation failure were to occur instead, a `HandoffRequest` would be published and `/robot/status` would show `WAITING_FOR_STAFF`
