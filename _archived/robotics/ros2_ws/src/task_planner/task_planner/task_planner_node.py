"""Task planner node — orchestrates GuideCustomer actions via shelf lookup and navigation."""

import rclpy
from rclpy.node import Node
from rclpy.action import ActionServer, CancelResponse, GoalResponse
from rclpy.callback_groups import ReentrantCallbackGroup

from retail_robot_interfaces.action import GuideCustomer
from retail_robot_interfaces.msg import HandoffRequest, NavigationTask
from retail_robot_interfaces.srv import ShelfLookup

import uuid


class TaskPlannerNode(Node):
    """Action server for GuideCustomer — coordinates shelf lookup, navigation, and handoff."""

    def __init__(self) -> None:
        super().__init__("task_planner_node")

        self._cb_group = ReentrantCallbackGroup()

        self._action_server = ActionServer(
            self,
            GuideCustomer,
            "/guide_customer",
            execute_callback=self.execute_callback,
            goal_callback=self.goal_callback,
            cancel_callback=self.cancel_callback,
            callback_group=self._cb_group,
        )

        # Service client for shelf lookup
        self._shelf_client = self.create_client(
            ShelfLookup, "/shelf_lookup", callback_group=self._cb_group
        )

        # Publisher for navigation tasks
        self._nav_pub = self.create_publisher(
            NavigationTask, "/navigation/task", 10
        )

        # Subscriber for navigation status
        self._nav_status = None
        self._nav_sub = self.create_subscription(
            NavigationTask,
            "/navigation/status",
            self._on_nav_status,
            10,
            callback_group=self._cb_group,
        )

        # Publisher for handoff requests
        self._handoff_pub = self.create_publisher(
            HandoffRequest, "/handoff/request", 10
        )

        self.get_logger().info("Task planner node started")

    def goal_callback(self, goal_request) -> GoalResponse:
        """Accept all incoming goals."""
        self.get_logger().info(
            f"Received goal: product={goal_request.product_id}, "
            f"customer={goal_request.customer_id}"
        )
        return GoalResponse.ACCEPT

    def cancel_callback(self, goal_handle) -> CancelResponse:
        """Accept cancellation requests."""
        self.get_logger().info("Received cancel request")
        return CancelResponse.ACCEPT

    async def execute_callback(self, goal_handle) -> GuideCustomer.Result:
        """Execute the GuideCustomer action."""
        product_id = goal_handle.request.product_id
        customer_id = goal_handle.request.customer_id
        task_id = str(uuid.uuid4())

        self.get_logger().info(
            f"Executing guide task {task_id}: "
            f"product={product_id}, customer={customer_id}"
        )

        # Send initial feedback
        feedback = GuideCustomer.Feedback()
        feedback.progress = 0.0
        feedback.current_location = "start"
        goal_handle.publish_feedback(feedback)

        # Step 1: Shelf lookup
        if not self._shelf_client.wait_for_service(timeout_sec=5.0):
            self.get_logger().error("ShelfLookup service not available")
            goal_handle.abort()
            return GuideCustomer.Result(success=False, final_status="SHELF_LOOKUP_UNAVAILABLE")

        shelf_request = ShelfLookup.Request()
        shelf_request.product_id = product_id
        shelf_response = await self._shelf_client.call_async(shelf_request)

        if not shelf_response.found:
            self.get_logger().warn(f"Product {product_id} not found in store map")
            goal_handle.abort()
            return GuideCustomer.Result(success=False, final_status="PRODUCT_NOT_FOUND")

        # Check for cancellation
        if goal_handle.is_cancel_requested:
            goal_handle.canceled()
            return GuideCustomer.Result(success=False, final_status="CANCELLED")

        feedback.progress = 0.2
        feedback.current_location = f"aisle {shelf_response.aisle_label} located"
        goal_handle.publish_feedback(feedback)

        # Step 2: Publish navigation task
        self._nav_status = None
        nav_msg = NavigationTask()
        nav_msg.task_id = task_id
        nav_msg.destination_label = product_id
        nav_msg.destination_x = shelf_response.x
        nav_msg.destination_y = shelf_response.y
        nav_msg.status = "PENDING"
        self._nav_pub.publish(nav_msg)

        self.get_logger().info(f"Published navigation task {task_id}")

        feedback.progress = 0.3
        feedback.current_location = "navigating"
        goal_handle.publish_feedback(feedback)

        # Step 3: Monitor navigation status
        result = await self._monitor_navigation(goal_handle, task_id, feedback)
        return result

    async def _monitor_navigation(
        self,
        goal_handle,
        task_id: str,
        feedback: GuideCustomer.Feedback,
    ) -> GuideCustomer.Result:
        """Wait for navigation to complete, sending feedback updates."""
        rate = self.create_rate(2)  # 2 Hz polling
        timeout_count = 0
        max_timeout = 30  # 15 seconds at 2 Hz

        while rclpy.ok():
            # Check for cancellation
            if goal_handle.is_cancel_requested:
                self.get_logger().info("Goal cancelled during navigation")
                goal_handle.canceled()
                return GuideCustomer.Result(success=False, final_status="CANCELLED")

            if self._nav_status is not None:
                status = self._nav_status

                if status == "ASSIGNED":
                    feedback.progress = 0.4
                    feedback.current_location = "navigation assigned"
                    goal_handle.publish_feedback(feedback)

                elif status == "IN_PROGRESS":
                    feedback.progress = 0.6
                    feedback.current_location = "en route"
                    goal_handle.publish_feedback(feedback)

                elif status == "COMPLETED":
                    feedback.progress = 1.0
                    feedback.current_location = "destination reached"
                    goal_handle.publish_feedback(feedback)
                    goal_handle.succeed()
                    return GuideCustomer.Result(
                        success=True, final_status="COMPLETED"
                    )

                elif status == "FAILED":
                    self.get_logger().warn(
                        f"Navigation failed for task {task_id}, triggering handoff"
                    )
                    self._trigger_handoff(task_id)
                    goal_handle.abort()
                    return GuideCustomer.Result(
                        success=False, final_status="NAVIGATION_FAILED_HANDOFF_TRIGGERED"
                    )

            timeout_count += 1
            if timeout_count >= max_timeout:
                self.get_logger().warn(f"Navigation timeout for task {task_id}")
                self._trigger_handoff(task_id)
                goal_handle.abort()
                return GuideCustomer.Result(
                    success=False, final_status="NAVIGATION_TIMEOUT"
                )

            rate.sleep()

    def _on_nav_status(self, msg: NavigationTask) -> None:
        """Track latest navigation status."""
        self._nav_status = msg.status

    def _trigger_handoff(self, task_id: str) -> None:
        """Publish a handoff request when navigation fails."""
        handoff = HandoffRequest()
        handoff.request_id = str(uuid.uuid4())
        handoff.reason = "Navigation failed — requesting staff assistance"
        handoff.task_id = task_id
        handoff.confidence = 0.0
        handoff.location_label = "unknown"
        self._handoff_pub.publish(handoff)
        self.get_logger().info(f"Handoff request published for task {task_id}")


def main(args=None) -> None:
    rclpy.init(args=args)
    node = TaskPlannerNode()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == "__main__":
    main()
