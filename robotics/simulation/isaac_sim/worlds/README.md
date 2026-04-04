# Isaac Sim Store World

## Hardware Requirements

- NVIDIA GPU with at least 8 GB VRAM (RTX 2070 or higher recommended)
- NVIDIA driver 525.60 or later
- Isaac Sim 2023.1.0 or later
- 32 GB system RAM recommended
- Ubuntu 22.04 (for native) or Windows with Isaac Sim container

## Opening the World

1. Launch NVIDIA Isaac Sim from the Omniverse Launcher.
2. Open `store_world.yaml` as a reference for world parameters.
3. Build the store environment using the layout from `../../store_world/layouts/store_layout.json`.
4. Place shelf assets, floor materials, and lighting according to the config.

## Running Without Isaac Sim

Isaac Sim is optional. The ROS 2 nodes run independently:

```bash
cd robotics/ros2_ws
colcon build
source install/setup.bash
ros2 launch retail_robot_bringup retail_robot.launch.py
```

The simulation bridge on port 8765 provides robot state and task submission without requiring a visual simulation environment. This is sufficient for development, testing, and frontend integration.
