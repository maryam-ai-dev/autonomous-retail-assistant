"use client";

import type { AuditEvent } from "@/features/audit-log/services/audit-service";
import EventCard from "./EventCard";

const EVENT_LABELS: Record<string, string> = {
  PRODUCTS_RANKED: "Products ranked for your search",
  APPROVAL_REQUIRED: "Approval required for purchase",
  APPROVAL_REQUESTED: "Approval request created",
  PURCHASE_AUTHORIZED: "Purchase approved",
  PURCHASE_REJECTED: "Purchase rejected",
  CART_CHECKOUT_INITIATED: "Checkout started",
  CHECKOUT_COMPLETED: "Purchase completed",
  CHECKOUT_FAILED: "Purchase failed",
};

interface AuditTimelineProps {
  events: AuditEvent[];
}

function getLabel(eventType: string): string {
  return EVENT_LABELS[eventType] ?? eventType;
}

export default function AuditTimeline({ events }: AuditTimelineProps) {
  if (events.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p className="text-lg font-medium">No audit events yet</p>
        <p className="text-sm">Events will appear here as you use the system</p>
      </div>
    );
  }

  return (
    <div className="relative space-y-3 pl-6">
      <div className="absolute left-2 top-0 bottom-0 w-0.5 bg-gray-200" />
      {events.map((event) => (
        <div key={event.id} className="relative">
          <div className="absolute -left-6 top-4 h-3 w-3 rounded-full border-2 border-blue-500 bg-white" />
          <EventCard event={event} label={getLabel(event.eventType)} />
        </div>
      ))}
    </div>
  );
}
