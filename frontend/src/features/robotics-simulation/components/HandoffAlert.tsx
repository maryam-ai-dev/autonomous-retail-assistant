"use client";

import { useState } from "react";
import { HandoffStatus } from "../types";

interface HandoffAlertProps {
  handoffStatus: HandoffStatus | null;
}

export default function HandoffAlert({ handoffStatus }: HandoffAlertProps) {
  const [resolving, setResolving] = useState(false);

  if (!handoffStatus || handoffStatus.reason === "RESOLVED") {
    return null;
  }

  async function handleResolve() {
    setResolving(true);
    try {
      await fetch("http://localhost:8765/handoff/resolve", { method: "POST" });
    } catch {
      // Bridge may not support this endpoint directly — staff resolves via ROS 2 service
    } finally {
      setResolving(false);
    }
  }

  return (
    <div className="rounded-lg border-2 border-amber-400 bg-amber-50 p-4">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="text-sm font-bold text-amber-800">Handoff Active</h3>
          <p className="mt-1 text-sm text-amber-700">
            <span className="font-medium">Reason:</span> {handoffStatus.reason}
          </p>
          <p className="text-sm text-amber-700">
            <span className="font-medium">Location:</span> {handoffStatus.location_label}
          </p>
          <p className="text-xs text-amber-600">
            Task: {handoffStatus.task_id.slice(0, 8)}... | Confidence: {handoffStatus.confidence.toFixed(2)}
          </p>
        </div>
        <button
          onClick={handleResolve}
          disabled={resolving}
          className="rounded-md bg-amber-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-amber-700 disabled:opacity-50"
        >
          {resolving ? "Resolving..." : "Mark as Resolved"}
        </button>
      </div>
    </div>
  );
}
