"""Human handoff service node — handles staff escalation when robot cannot complete a task."""

import rclpy
from rclpy.node import Node
from std_msgs.msg import String
from std_srvs.srv import Trigger

from retail_robot_interfaces.msg import HandoffRequest


class HandoffNode(Node):
    """Receives handoff requests, notifies staff, and provides a resolve service."""

    def __init__(self) -> None:
        super().__init__("handoff_node")

        # Subscriber for incoming handoff requests
        self.request_sub = self.create_subscription(
            HandoffRequest, "/handoff/request", self.on_handoff_request, 10
        )

        # Publisher for handoff status updates
        self.status_pub = self.create_publisher(
            HandoffRequest, "/handoff/status", 10
        )

        # Publisher for robot status
        self.robot_status_pub = self.create_publisher(
            String, "/robot/status", 10
        )

        # Service to resolve an active handoff
        self.resolve_srv = self.create_service(
            Trigger, "/handoff/resolve", self.handle_resolve
        )

        self._active_handoff = None

        self.get_logger().info("Handoff node started")

    def on_handoff_request(self, msg: HandoffRequest) -> None:
        """Handle an incoming handoff request."""
        self.get_logger().info(
            f"Handoff request received — id: {msg.request_id}, "
            f"reason: {msg.reason}, task: {msg.task_id}, "
            f"confidence: {msg.confidence}, location: {msg.location_label}"
        )

        self._active_handoff = msg

        # Publish RECEIVED status
        self._publish_status(msg, "RECEIVED")

        # After 1 second publish STAFF_NOTIFIED and update robot status
        self.create_timer(1.0, lambda: self._notify_staff(msg))

    def _notify_staff(self, msg: HandoffRequest) -> None:
        """Transition to STAFF_NOTIFIED and update robot status."""
        self._publish_status(msg, "STAFF_NOTIFIED")

        robot_status = String()
        robot_status.data = "WAITING_FOR_STAFF"
        self.robot_status_pub.publish(robot_status)

        self.get_logger().info(
            f"Staff notified for handoff {msg.request_id} — robot waiting"
        )

    def _publish_status(self, msg: HandoffRequest, status: str) -> None:
        """Publish a handoff status update reusing the HandoffRequest msg with updated reason."""
        status_msg = HandoffRequest()
        status_msg.request_id = msg.request_id
        status_msg.reason = status
        status_msg.task_id = msg.task_id
        status_msg.confidence = msg.confidence
        status_msg.location_label = msg.location_label
        self.status_pub.publish(status_msg)
        self.get_logger().info(f"Handoff {msg.request_id} status: {status}")

    def handle_resolve(
        self,
        request: Trigger.Request,
        response: Trigger.Response,
    ) -> Trigger.Response:
        """Mark the active handoff as resolved."""
        if self._active_handoff is None:
            response.success = False
            response.message = "No active handoff to resolve"
            self.get_logger().warn("Resolve called but no active handoff")
            return response

        self._publish_status(self._active_handoff, "RESOLVED")

        robot_status = String()
        robot_status.data = "IDLE"
        self.robot_status_pub.publish(robot_status)

        self.get_logger().info(
            f"Handoff {self._active_handoff.request_id} resolved"
        )

        response.success = True
        response.message = f"Handoff {self._active_handoff.request_id} resolved"
        self._active_handoff = None
        return response


def main(args=None) -> None:
    rclpy.init(args=args)
    node = HandoffNode()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == "__main__":
    main()
