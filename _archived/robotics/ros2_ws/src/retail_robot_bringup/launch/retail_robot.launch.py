"""Main launch file — starts all Aisleon retail robot nodes."""

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
        Node(
            package="navigation_service",
            executable="navigation_node",
            name="navigation_node",
            output="screen",
        ),
        Node(
            package="task_planner",
            executable="task_planner_node",
            name="task_planner_node",
            output="screen",
        ),
        Node(
            package="human_handoff_service",
            executable="handoff_node",
            name="handoff_node",
            output="screen",
        ),
        Node(
            package="simulation_bridge",
            executable="bridge_node",
            name="simulation_bridge_node",
            output="screen",
        ),
    ])
