"use client";

import { useState } from "react";
import type { AuditEvent } from "@/features/audit-log/services/audit-service";

interface EventCardProps {
  event: AuditEvent;
  label: string;
}

export default function EventCard({ event, label }: EventCardProps) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium">{label}</p>
          <p className="text-xs text-gray-500">
            {new Date(event.createdAt).toLocaleString()}
          </p>
        </div>
        <button
          onClick={() => setExpanded(!expanded)}
          className="rounded px-2 py-1 text-xs text-gray-500 hover:bg-gray-100"
        >
          {expanded ? "Hide" : "Details"}
        </button>
      </div>

      {expanded && event.payload && (
        <pre className="mt-3 max-h-48 overflow-auto rounded bg-gray-50 p-3 text-xs text-gray-700">
          {JSON.stringify(event.payload, null, 2)}
        </pre>
      )}
    </div>
  );
}
