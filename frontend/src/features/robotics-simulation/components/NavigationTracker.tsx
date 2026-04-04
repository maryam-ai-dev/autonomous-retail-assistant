"use client";

import { NavigationStatus } from "../types";

interface NavigationTrackerProps {
  navigationStatus: NavigationStatus | null;
}

function statusColor(status: string): string {
  switch (status) {
    case "ASSIGNED": return "bg-yellow-100 text-yellow-800";
    case "IN_PROGRESS": return "bg-blue-100 text-blue-800";
    case "COMPLETED": return "bg-green-100 text-green-800";
    case "FAILED": return "bg-red-100 text-red-800";
    default: return "bg-gray-100 text-gray-800";
  }
}

function progressPercent(status: string): number {
  switch (status) {
    case "ASSIGNED": return 25;
    case "IN_PROGRESS": return 60;
    case "COMPLETED": return 100;
    case "FAILED": return 100;
    default: return 0;
  }
}

export default function NavigationTracker({ navigationStatus }: NavigationTrackerProps) {
  if (!navigationStatus) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-4">
        <h3 className="mb-2 text-sm font-semibold text-gray-700">Navigation</h3>
        <p className="text-sm text-gray-400">No active navigation task</p>
      </div>
    );
  }

  const progress = progressPercent(navigationStatus.status);
  const barColor = navigationStatus.status === "FAILED" ? "bg-red-500" : "bg-blue-500";

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold text-gray-700">Navigation</h3>
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500">Task ID</span>
          <span className="font-mono text-xs text-gray-700">
            {navigationStatus.task_id.slice(0, 8)}...
          </span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500">Destination</span>
          <span className="text-gray-700">{navigationStatus.destination_label}</span>
        </div>
        <div className="flex items-center justify-between text-sm">
          <span className="text-gray-500">Status</span>
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${statusColor(navigationStatus.status)}`}>
            {navigationStatus.status}
          </span>
        </div>
        <div className="pt-1">
          <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
            <div
              className={`h-full rounded-full transition-all duration-500 ${barColor}`}
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
