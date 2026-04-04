"""Launch file for the navigation service node."""

from launch import LaunchDescription
from launch_ros.actions import Node


def generate_launch_description() -> LaunchDescription:
    return LaunchDescription([
        Node(
            package="navigation_service",
            executable="navigation_node",
            name="navigation_node",
            output="screen",
        ),
    ])
