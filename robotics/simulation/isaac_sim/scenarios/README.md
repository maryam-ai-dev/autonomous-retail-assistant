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
