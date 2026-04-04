# Robot Configuration

## Base Platform

The Aisleon retail robot is a differential drive wheeled platform based on the NVIDIA Isaac Sim Carter robot. Carter provides a well-tested base with:

- Differential drive kinematics
- ROS 2 navigation stack compatibility
- Isaac Sim physics integration

See: [NVIDIA Isaac Sim Carter Robot](https://docs.omniverse.nvidia.com/isaacsim/latest/features/robots/carter.html)

## Sensors

- **2D LiDAR** (RPLidar A2) — mounted on top for obstacle detection and SLAM, 12m range, 360-degree scan
- **RGB-D Camera** (Intel RealSense D435) — front-mounted for perception, product recognition, and depth sensing

## Customization

Edit `robot_config.yaml` to adjust chassis dimensions, sensor parameters, or navigation settings. The config is referenced by Isaac Sim scene setup and ROS 2 launch files.
