"""Launch file for the task planner node."""

from launch import LaunchDescription
from launch_ros.actions import Node


def generate_launch_description() -> LaunchDescription:
    return LaunchDescription([
        Node(
            package="task_planner",
            executable="task_planner_node",
            name="task_planner_node",
            output="screen",
        ),
    ])
