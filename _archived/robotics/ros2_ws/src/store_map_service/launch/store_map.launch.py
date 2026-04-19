"""Launch file for the store map service node."""

from launch import LaunchDescription
from launch_ros.actions import Node


def generate_launch_description() -> LaunchDescription:
    return LaunchDescription([
        Node(
            package="store_map_service",
            executable="store_map_node",
            name="store_map_node",
            output="screen",
        ),
    ])
