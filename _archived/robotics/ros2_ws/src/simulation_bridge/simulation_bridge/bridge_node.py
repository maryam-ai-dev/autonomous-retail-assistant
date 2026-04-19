"""Simulation bridge node — exposes robot state via HTTP for frontend integration.

Development/demo bridge only. This is NOT production-grade robotics transport.
It provides a simple HTTP interface on port 8765 for the frontend dashboard
to query robot state and submit tasks. Do not use in production deployments.
"""

import json
import threading
from functools import partial
from http.server import HTTPServer, BaseHTTPRequestHandler

import rclpy
from rclpy.node import Node
from rclpy.action import ActionClient
from rclpy.callback_groups import ReentrantCallbackGroup
from geometry_msgs.msg import Pose2D
from std_msgs.msg import String

from retail_robot_interfaces.action import GuideCustomer
from retail_robot_interfaces.msg import HandoffRequest, NavigationTask


class BridgeNode(Node):
    """Subscribes to robot topics and serves state over HTTP on port 8765."""

    def __init__(self) -> None:
        super().__init__("simulation_bridge_node")

        self._cb_group = ReentrantCallbackGroup()

        # Current state aggregated from all subscriptions
        self.current_state = {
            "pose": {"x": 0.0, "y": 0.0, "theta": 0.0},
            "navigation_status": None,
            "handoff_status": None,
            "robot_status": "IDLE",
        }
        self._state_lock = threading.Lock()

        # Subscribers
        self.create_subscription(
            Pose2D, "/robot/pose", self._on_pose, 10,
            callback_group=self._cb_group,
        )
        self.create_subscription(
            NavigationTask, "/navigation/status", self._on_nav_status, 10,
            callback_group=self._cb_group,
        )
        self.create_subscription(
            HandoffRequest, "/handoff/status", self._on_handoff_status, 10,
            callback_group=self._cb_group,
        )
        self.create_subscription(
            String, "/robot/status", self._on_robot_status, 10,
            callback_group=self._cb_group,
        )

        # Action client for GuideCustomer
        self._guide_client = ActionClient(
            self, GuideCustomer, "/guide_customer",
            callback_group=self._cb_group,
        )

        # Start HTTP server in background thread
        self._http_thread = threading.Thread(
            target=self._run_http_server, daemon=True
        )
        self._http_thread.start()

        self.get_logger().info(
            "Simulation bridge node started — HTTP server on port 8765"
        )

    def _on_pose(self, msg: Pose2D) -> None:
        with self._state_lock:
            self.current_state["pose"] = {
                "x": msg.x, "y": msg.y, "theta": msg.theta,
            }

    def _on_nav_status(self, msg: NavigationTask) -> None:
        with self._state_lock:
            self.current_state["navigation_status"] = {
                "task_id": msg.task_id,
                "destination_label": msg.destination_label,
                "destination_x": msg.destination_x,
                "destination_y": msg.destination_y,
                "status": msg.status,
            }

    def _on_handoff_status(self, msg: HandoffRequest) -> None:
        with self._state_lock:
            self.current_state["handoff_status"] = {
                "request_id": msg.request_id,
                "reason": msg.reason,
                "task_id": msg.task_id,
                "confidence": msg.confidence,
                "location_label": msg.location_label,
            }

    def _on_robot_status(self, msg: String) -> None:
        with self._state_lock:
            self.current_state["robot_status"] = msg.data

    def get_state(self) -> dict:
        """Return a snapshot of the current state."""
        with self._state_lock:
            return dict(self.current_state)

    def send_guide_goal(self, product_id: str, customer_id: str) -> dict:
        """Send a GuideCustomer goal and return immediately."""
        if not self._guide_client.wait_for_server(timeout_sec=3.0):
            return {"accepted": False, "reason": "GuideCustomer action server not available"}

        goal = GuideCustomer.Goal()
        goal.product_id = product_id
        goal.customer_id = customer_id

        future = self._guide_client.send_goal_async(goal)
        self.get_logger().info(
            f"Sent GuideCustomer goal: product={product_id}, customer={customer_id}"
        )
        return {"accepted": True, "product_id": product_id, "customer_id": customer_id}

    def _run_http_server(self) -> None:
        """Run the HTTP server in a background thread."""
        handler = partial(BridgeHTTPHandler, bridge_node=self)
        server = HTTPServer(("0.0.0.0", 8765), handler)
        self.get_logger().info("HTTP server listening on http://0.0.0.0:8765")
        server.serve_forever()


class BridgeHTTPHandler(BaseHTTPRequestHandler):
    """Simple HTTP handler for GET /state and POST /task.

    Development/demo bridge only — not production transport.
    """

    def __init__(self, *args, bridge_node: BridgeNode, **kwargs) -> None:
        self.bridge_node = bridge_node
        super().__init__(*args, **kwargs)

    def do_GET(self) -> None:
        if self.path == "/state":
            state = self.bridge_node.get_state()
            self._json_response(200, state)
        else:
            self._json_response(404, {"error": "Not found"})

    def do_POST(self) -> None:
        if self.path == "/task":
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length)
            try:
                data = json.loads(body)
                product_id = data.get("product_id", "")
                customer_id = data.get("customer_id", "unknown")

                if not product_id:
                    self._json_response(400, {"error": "product_id is required"})
                    return

                result = self.bridge_node.send_guide_goal(product_id, customer_id)
                status = 202 if result.get("accepted") else 503
                self._json_response(status, result)
            except json.JSONDecodeError:
                self._json_response(400, {"error": "Invalid JSON"})
        else:
            self._json_response(404, {"error": "Not found"})

    def _json_response(self, status: int, data: dict) -> None:
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode())

    def log_message(self, format, *args) -> None:
        """Suppress default HTTP logging — ROS logger is used instead."""
        pass


def main(args=None) -> None:
    rclpy.init(args=args)
    node = BridgeNode()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == "__main__":
    main()
