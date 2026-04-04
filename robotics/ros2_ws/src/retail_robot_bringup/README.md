# retail_robot_bringup

Main bringup package for the Aisleon retail robot ROS 2 system.

## Build

```bash
cd robotics/ros2_ws
colcon build
source install/setup.bash
```

## Launch

```bash
ros2 launch retail_robot_bringup retail_robot.launch.py
```

This starts all nodes: store_map_service, navigation_service, task_planner, human_handoff_service, and simulation_bridge.
