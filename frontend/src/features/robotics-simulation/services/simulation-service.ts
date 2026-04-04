import { RobotState, SendTaskResponse } from "../types";

const BRIDGE_URL = "http://localhost:8765";

export async function pollState(): Promise<RobotState> {
  const response = await fetch(`${BRIDGE_URL}/state`);
  if (!response.ok) {
    throw new Error(`Bridge state request failed: ${response.status}`);
  }
  return response.json();
}

export async function sendTask(
  productId: string,
  customerId: string
): Promise<SendTaskResponse> {
  const response = await fetch(`${BRIDGE_URL}/task`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ product_id: productId, customer_id: customerId }),
  });
  if (!response.ok && response.status !== 202) {
    throw new Error(`Bridge task request failed: ${response.status}`);
  }
  return response.json();
}
