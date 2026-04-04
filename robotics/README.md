# Robotics

ROS 2 packages and Isaac Sim simulation assets for Aisleon. This layer is **simulation-only** — no physical hardware is required or assumed.

## Scope

The robotics layer demonstrates how a trust-aware retail assistant could extend into physical store navigation. It includes:

- ROS 2 nodes for store mapping, navigation, task planning, and human handoff
- An HTTP bridge for frontend integration
- Isaac Sim configuration files and scenario definitions

All navigation is simulated with fixed timing. There is no real path planning or obstacle avoidance — the robot linearly interpolates to its destination over 3 seconds.

## ROS 2 Workspace Overview

```
ros2_ws/src/
├── retail_robot_interfaces/   # Custom msgs, srvs, actions (ament_cmake)
├── store_map_service/         # In-memory product-to-shelf location lookup
├── navigation_service/        # Simulated navigation with pose publishing
├── task_planner/              # GuideCustomer action server orchestrating the flow
├── human_handoff_service/     # Staff escalation when robot cannot complete a task
├── simulation_bridge/         # HTTP server (port 8765) exposing robot state to frontend
├── retail_robot_bringup/      # Launch file that starts all nodes
├── retail_robot_description/  # Robot URDF/mesh placeholders
└── perception_service/        # Perception node placeholder
```

## Isaac Sim Asset Overview

```
simulation/
├── isaac_sim/
│   ├── worlds/          # Store world YAML config (20x15m, lighting, physics)
│   ├── robots/          # Differential drive robot config (lidar + camera)
│   ├── scenarios/       # Runnable scenario definitions with ROS 2 trigger commands
│   ├── nav/             # Navigation map placeholders
│   ├── props/           # Store prop placeholders
│   └── sensors/         # Sensor config placeholders
└── store_world/
    ├── layouts/         # Store layout JSON (4 aisles, 12 shelves, zones)
    ├── metadata/        # Store metadata placeholders
    ├── products/        # Product placement placeholders
    ├── shelves/         # Shelf asset placeholders
    └── signage/         # Signage asset placeholders
```

## How to Build

Requires ROS 2 Humble (or later) installed.

```bash
cd ros2_ws
colcon build
source install/setup.bash
```

## How to Run (without Isaac Sim)

```bash
# Start all nodes with one command:
ros2 launch retail_robot_bringup retail_robot.launch.py

# In another terminal, send a navigation task:
ros2 action send_goal /guide_customer \
  retail_robot_interfaces/action/GuideCustomer \
  "{product_id: 'PROD_001', customer_id: 'USER_001'}"

# Or use the HTTP bridge:
curl http://localhost:8765/state
curl -X POST http://localhost:8765/task \
  -H "Content-Type: application/json" \
  -d '{"product_id": "PROD_001", "customer_id": "USER_001"}'
```

The simulation bridge on port 8765 provides robot state and task submission for the frontend dashboard at `/robotics-simulation`.

## Hardware Requirements for Isaac Sim

Isaac Sim is **optional**. The ROS 2 nodes run independently without it. If you want visual simulation:

- NVIDIA GPU with 8 GB+ VRAM (RTX 2070 or higher recommended)
- NVIDIA driver 525.60 or later
- Isaac Sim 2023.1.0 or later
- 32 GB system RAM recommended
- Ubuntu 22.04 (native) or Windows with Isaac Sim container

See [simulation/isaac_sim/worlds/README.md](simulation/isaac_sim/worlds/README.md) for setup details.
