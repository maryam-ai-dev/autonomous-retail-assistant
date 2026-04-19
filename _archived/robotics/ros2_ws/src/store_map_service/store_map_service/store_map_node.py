"""Store map service node — provides product-to-shelf location lookups."""

import rclpy
from rclpy.node import Node
from std_msgs.msg import Bool

from retail_robot_interfaces.srv import ShelfLookup


# In-memory product map: product_id → {aisle, x, y, shelf_label}
PRODUCT_MAP = {
    "PROD_001": {"aisle": "A1", "x": 1.0, "y": 2.5, "shelf_label": "A1-S1"},
    "PROD_002": {"aisle": "A1", "x": 1.0, "y": 4.0, "shelf_label": "A1-S2"},
    "PROD_003": {"aisle": "A2", "x": 3.0, "y": 1.5, "shelf_label": "A2-S1"},
    "PROD_004": {"aisle": "A2", "x": 3.0, "y": 3.5, "shelf_label": "A2-S2"},
    "PROD_005": {"aisle": "B1", "x": 5.0, "y": 2.0, "shelf_label": "B1-S1"},
    "PROD_006": {"aisle": "B1", "x": 5.0, "y": 4.5, "shelf_label": "B1-S2"},
    "PROD_007": {"aisle": "B2", "x": 7.0, "y": 1.0, "shelf_label": "B2-S1"},
    "PROD_008": {"aisle": "B2", "x": 7.0, "y": 3.0, "shelf_label": "B2-S2"},
    "PROD_009": {"aisle": "C1", "x": 9.0, "y": 2.5, "shelf_label": "C1-S1"},
    "PROD_010": {"aisle": "C1", "x": 9.0, "y": 5.0, "shelf_label": "C1-S2"},
}


class StoreMapNode(Node):
    """Serves shelf lookup requests and publishes readiness on startup."""

    def __init__(self) -> None:
        super().__init__("store_map_node")

        self.shelf_lookup_srv = self.create_service(
            ShelfLookup, "/shelf_lookup", self.handle_shelf_lookup
        )

        self.ready_pub = self.create_publisher(Bool, "/store_map/ready", 10)

        # Publish ready after a short delay to allow subscribers to connect
        self.create_timer(1.0, self._publish_ready)

        self.get_logger().info(
            f"Store map node started — {len(PRODUCT_MAP)} products loaded"
        )

    def _publish_ready(self) -> None:
        msg = Bool()
        msg.data = True
        self.ready_pub.publish(msg)
        self.get_logger().info("Published store map ready")
        # Only publish once — destroy the timer
        self.destroy_timer(self._ready_timer)

    def create_timer(self, period, callback):
        self._ready_timer = super().create_timer(period, callback)
        return self._ready_timer

    def handle_shelf_lookup(
        self,
        request: ShelfLookup.Request,
        response: ShelfLookup.Response,
    ) -> ShelfLookup.Response:
        """Look up product location in the store map."""
        product = PRODUCT_MAP.get(request.product_id)

        if product is not None:
            response.aisle_label = product["aisle"]
            response.x = float(product["x"])
            response.y = float(product["y"])
            response.found = True
            self.get_logger().info(
                f"Shelf lookup: {request.product_id} -> "
                f"{product['shelf_label']} ({product['x']}, {product['y']})"
            )
        else:
            response.aisle_label = ""
            response.x = 0.0
            response.y = 0.0
            response.found = False
            self.get_logger().warn(
                f"Shelf lookup: {request.product_id} not found"
            )

        return response


def main(args=None) -> None:
    rclpy.init(args=args)
    node = StoreMapNode()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        pass
    finally:
        node.destroy_node()
        rclpy.shutdown()


if __name__ == "__main__":
    main()
