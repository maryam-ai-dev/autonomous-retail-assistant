"use client";

import Link from "next/link";
import type { CheckoutResult } from "@/features/cart/types";

interface ApprovalTriggerPanelProps {
  result: CheckoutResult;
}

export default function ApprovalTriggerPanel({
  result,
}: ApprovalTriggerPanelProps) {
  if (result.outcome === "CHECKED_OUT") return null;

  if (result.outcome === "APPROVAL_REQUIRED") {
    return (
      <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
        <h3 className="mb-1 text-sm font-semibold text-blue-800">
          Approval Required
        </h3>
        <p className="mb-3 text-sm text-blue-700">
          Your purchase needs approval before it can proceed.
        </p>
        <Link
          href="/approvals"
          className="inline-block rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          View Approvals
        </Link>
      </div>
    );
  }

  if (result.outcome === "BLOCKED") {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4">
        <h3 className="mb-2 text-sm font-semibold text-red-800">
          Purchase Blocked
        </h3>
        {result.reasons && result.reasons.length > 0 && (
          <ul className="space-y-1">
            {result.reasons.map((reason, i) => (
              <li key={i} className="text-sm text-red-700">
                {reason}
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  return null;
}
