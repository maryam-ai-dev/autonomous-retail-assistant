"use client";

import { useState } from "react";
import { useRobotState } from "@/features/robotics-simulation/hooks/useRobotState";
import { sendTask } from "@/features/robotics-simulation/services/simulation-service";
import StoreMap from "@/features/robotics-simulation/components/StoreMap";
import NavigationTracker from "@/features/robotics-simulation/components/NavigationTracker";
import TaskStatus from "@/features/robotics-simulation/components/TaskStatus";
import HandoffAlert from "@/features/robotics-simulation/components/HandoffAlert";

const PRODUCT_IDS = [
  "PROD_001", "PROD_002", "PROD_003", "PROD_004", "PROD_005",
  "PROD_006", "PROD_007", "PROD_008", "PROD_009", "PROD_010",
];

export default function RoboticsSimulationPage() {
  const { state, error } = useRobotState();
  const [productId, setProductId] = useState(PRODUCT_IDS[0]);
  const [customerId, setCustomerId] = useState("");
  const [sending, setSending] = useState(false);
  const [taskMessage, setTaskMessage] = useState<string | null>(null);

  async function handleSendTask() {
    if (!productId) return;
    setSending(true);
    setTaskMessage(null);
    try {
      const result = await sendTask(productId, customerId || "anonymous");
      if (result.accepted) {
        setTaskMessage("Task sent successfully");
      } else {
        setTaskMessage(result.reason ?? "Task was not accepted");
      }
    } catch {
      setTaskMessage("Failed to send task — is the bridge running?");
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Robotics Simulation</h1>
        <p className="mt-1 text-sm text-gray-500">
          Requires ROS 2 workspace and simulation bridge running locally.
          See robotics/README.md for setup.
        </p>
      </div>

      {/* Top: status + handoff alert */}
      <div className="space-y-3">
        <TaskStatus robotState={state} error={error} />
        <HandoffAlert handoffStatus={state?.handoff_status ?? null} />
      </div>

      {/* Main panels */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {/* Left: store map */}
        <div className="lg:col-span-2">
          <StoreMap robotState={state} />
        </div>

        {/* Right: navigation + send task */}
        <div className="space-y-4">
          <NavigationTracker navigationStatus={state?.navigation_status ?? null} />

          {/* Send Task form */}
          <div className="rounded-lg border border-gray-200 bg-white p-4">
            <h3 className="mb-3 text-sm font-semibold text-gray-700">Send Task</h3>
            <div className="space-y-3">
              <div>
                <label htmlFor="product-id" className="block text-xs font-medium text-gray-600">
                  Product ID
                </label>
                <select
                  id="product-id"
                  value={productId}
                  onChange={(e) => setProductId(e.target.value)}
                  className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  {PRODUCT_IDS.map((id) => (
                    <option key={id} value={id}>{id}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="customer-id" className="block text-xs font-medium text-gray-600">
                  Customer ID
                </label>
                <input
                  id="customer-id"
                  type="text"
                  value={customerId}
                  onChange={(e) => setCustomerId(e.target.value)}
                  placeholder="e.g. USER_001"
                  className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>
              <button
                onClick={handleSendTask}
                disabled={sending}
                className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              >
                {sending ? "Sending..." : "Guide Customer"}
              </button>
              {taskMessage && (
                <p className="text-xs text-gray-600">{taskMessage}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
