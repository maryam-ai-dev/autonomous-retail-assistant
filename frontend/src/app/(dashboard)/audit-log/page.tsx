"use client";

import { useEffect, useState } from "react";
import { useRequireAuth } from "@/core/auth/guards";
import AuditTimeline from "@/features/audit-log/components/AuditTimeline";
import {
  getAuditEvents,
  type AuditEvent,
} from "@/features/audit-log/services/audit-service";

const EVENT_TYPES = [
  { value: "", label: "All Events" },
  { value: "PRODUCTS_RANKED", label: "Products Ranked" },
  { value: "APPROVAL_REQUIRED", label: "Approval Required" },
  { value: "APPROVAL_REQUESTED", label: "Approval Requested" },
  { value: "PURCHASE_AUTHORIZED", label: "Purchase Approved" },
  { value: "PURCHASE_REJECTED", label: "Purchase Rejected" },
  { value: "CART_CHECKOUT_INITIATED", label: "Checkout Started" },
  { value: "CHECKOUT_COMPLETED", label: "Checkout Completed" },
  { value: "CHECKOUT_FAILED", label: "Checkout Failed" },
];

export default function AuditLogPage() {
  useRequireAuth();

  const [events, setEvents] = useState<AuditEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("");

  useEffect(() => {
    setLoading(true);
    getAuditEvents(filter || undefined)
      .then(setEvents)
      .finally(() => setLoading(false));
  }, [filter]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Audit Log</h1>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="rounded border border-gray-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {EVENT_TYPES.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="py-8 text-center text-gray-500">Loading events...</div>
      ) : (
        <AuditTimeline events={events} />
      )}
    </div>
  );
}
