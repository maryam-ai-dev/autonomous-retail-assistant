"""Navigation service node — receives tasks, looks up shelves, and simulates navigation."""

import rclpy
from rclpy.node import Node
from geometry_msgs.msg import Pose2D

from retail_robot_interfaces.msg import NavigationTask
from retail_robot_interfaces.srv import ShelfLookup


class NavigationNode(Node):
    """Subscribes to navigation tasks, calls ShelfLookup, and simulates movement."""

    def __init__(self) -> None:
        super().__init__("navigation_node")

        # Subscriber for incoming navigation tasks
        self.task_sub = self.create_subscription(
            NavigationTask, "/navigation/task", self.on_task_received, 10
        )

        # Publisher for navigation status updates
        self.status_pub = self.create_publisher(
            NavigationTask, "/navigation/status", 10
        )

        # Publisher for simulated robot pose
        self.pose_pub = self.create_publisher(Pose2D, "/robot/pose", 10)

        # ShelfLookup service client
        self.shelf_client = self.create_client(ShelfLookup, "/shelf_lookup")

        # Navigation state
        self._current_task = None
        self._start_x = 0.0
        self._start_y = 0.0
        self._dest_x = 0.0
        self._dest_y = 0.0
        self._elapsed = 0.0
        self._nav_duration = 3.0
        self._pose_timer = None

        self.get_logger().info("Navigation node started")

    def on_task_received(self, msg: NavigationTask) -> None:
        """Handle an incoming navigation task."""
        self.get_logger().info(
            f"Received navigation task {msg.task_id} -> {msg.destination_label}"
        )

        self._current_task = msg

        # Call ShelfLookup to get coordinates
        if not self.shelf_client.wait_for_service(timeout_sec=5.0):
            self.get_logger().warn("ShelfLookup service not available")
            self._start_navigation(
                msg, msg.destination_x, msg.destination_y
            )
            return

        request = ShelfLookup.Request()
        request.product_id = msg.destination_label

        future = self.shelf_client.call_async(request)
        future.add_done_callback(
            lambda f: self._on_shelf_lookup_done(f, msg)
        )

    def _on_shelf_lookup_done(self, future, task_msg: NavigationTask) -> None:
        """Handle ShelfLookup response and begin navigation."""
        try:
            response = future.result()
            if response.found:
                self.get_logger().info(
                    f"Shelf lookup found: aisle {response.aisle_label} "
                    f"at ({response.x}, {response.y})"
                )
                self._start_navigation(task_msg, response.x, response.y)
            else:
                self.get_logger().warn(
                    f"Product {task_msg.destination_label} not found in store map, "
                    f"using task coordinates"
                )
                self._start_navigation(
                    task_msg, task_msg.destination_x, task_msg.destination_y
                )
        except Exception as e:
            self.get_logger().error(f"ShelfLookup call failed: {e}")
            self._start_navigation(
                task_msg, task_msg.destination_x, task_msg.destination_y
            )

    def _start_navigation(
        self, task_msg: NavigationTask, dest_x: float, dest_y: float
    ) -> None:
        """Begin simulated navigation: ASSIGNED -> IN_PROGRESS -> COMPLETED."""
        self._start_x = 0.0
        self._start_y = 0.0
        self._dest_x = dest_x
        self._dest_y = dest_y
        self._elapsed = 0.0

        # Publish ASSIGNED status
        self._publish_status(task_msg, "ASSIGNED")

        # After 1 second publish IN_PROGRESS, start pose updates
        self.create_timer(
            1.0,
            lambda: self._transition_to_in_progress(task_msg),
        )

    def _transition_to_in_progress(self, task_msg: NavigationTask) -> None:
        """Transition to IN_PROGRESS and start pose publishing."""
        self._publish_status(task_msg, "IN_PROGRESS")

        # Publish pose every 0.5 seconds during navigation (2 seconds remaining)
        self._elapsed = 0.0
        if self._pose_timer is not None:
            self.destroy_timer(self._pose_timer)

        self._pose_timer = self.create_timer(
            0.5,
            lambda: self._update_pose(task_msg),
        )

    def _update_pose(self, task_msg: NavigationTask) -> None:
        """Publish interpolated pose and complete when done."""
        self._elapsed += 0.5
        nav_time = 2.0  # 2 seconds of actual movement (after 1s ASSIGNED)

        progress = min(1.0, self._elapsed / nav_time)

        # Linear interpolation from start to destination
        pose = Pose2D()
        pose.x = self._start_x + (self._dest_x - self._start_x) * progress
        pose.y = self._start_y + (self._dest_y - self._start_y) * progress
        pose.theta = 0.0
        self.pose_pub.publish(pose)

        self.get_logger().info(
            f"Pose: ({pose.x:.2f}, {pose.y:.2f}) — progress {progress * 100:.0f}%"
        )

        if progress >= 1.0:
            # Navigation complete
            if self._pose_timer is not None:
                self.destroy_timer(self._pose_timer)
                self._pose_timer = None
            self._publish_status(task_msg, "COMPLETED")
            self.get_logger().info(
                f"Navigation task {task_msg.task_id} COMPLETED"
            )

    def _publish_status(
        self, task_msg: NavigationTask, status: str
    ) -> None:
        """Publish a status update for the current task."""
        status_msg = NavigationTask()
        status_msg.task_id = task_msg.task_id
        status_msg.destination_label = task_msg.destination_label
        status_msg.destination_x = self._dest_x
        status_msg.destination_y = self._dest_y
        status_msg.status = status
        self.status_pub.publish(status_msg)
        self.get_logger().info(
            f"Task {task_msg.task_id} status: {status}"
        )


def main(args=None) -> None:
    rclpy.init(args=args)
    node = NavigationNode()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == "__main__":
    main()
