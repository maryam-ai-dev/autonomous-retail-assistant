"use client";

import { RobotState } from "../types";

interface TaskStatusProps {
  robotState: RobotState | null;
  error: string | null;
}

function statusIcon(status: string): string {
  switch (status) {
    case "IDLE": return "●";
    case "NAVIGATING": return "▶";
    case "WAITING_FOR_STAFF": return "⏳";
    default: return "○";
  }
}

function statusStyle(status: string): string {
  switch (status) {
    case "IDLE": return "bg-green-100 text-green-800";
    case "NAVIGATING": return "bg-blue-100 text-blue-800";
    case "WAITING_FOR_STAFF": return "bg-amber-100 text-amber-800";
    default: return "bg-gray-100 text-gray-800";
  }
}

export default function TaskStatus({ robotState, error }: TaskStatusProps) {
  if (error) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4">
        <div className="flex items-center gap-2">
          <span className="text-red-500">●</span>
          <span className="text-sm font-medium text-red-700">Bridge Offline</span>
        </div>
        <p className="mt-1 text-xs text-red-500">{error}</p>
      </div>
    );
  }

  const status = robotState?.robot_status ?? "IDLE";
  const navStatus = robotState?.navigation_status;
  const lastCompleted = navStatus?.status === "COMPLETED" ? navStatus.destination_label : null;

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-700">Robot Status</h3>
        <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${statusStyle(status)}`}>
          {statusIcon(status)} {status}
        </span>
      </div>
      {lastCompleted && (
        <p className="mt-2 text-xs text-gray-500">
          Last completed: <span className="font-medium text-gray-700">{lastCompleted}</span>
        </p>
      )}
    </div>
  );
}
