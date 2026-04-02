"use client";

import { useState } from "react";

interface ApprovalActionBarProps {
  onApprove: () => void;
  onReject: () => void;
  processing: boolean;
}

export default function ApprovalActionBar({
  onApprove,
  onReject,
  processing,
}: ApprovalActionBarProps) {
  const [confirming, setConfirming] = useState<"approve" | "reject" | null>(null);

  function handleConfirm() {
    if (confirming === "approve") onApprove();
    if (confirming === "reject") onReject();
    setConfirming(null);
  }

  if (confirming) {
    return (
      <div className="flex items-center gap-2">
        <span className="text-sm text-gray-600">
          {confirming === "approve" ? "Approve this purchase?" : "Reject this purchase?"}
        </span>
        <button
          onClick={handleConfirm}
          disabled={processing}
          className="rounded bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {processing ? "Processing..." : "Confirm"}
        </button>
        <button
          onClick={() => setConfirming(null)}
          disabled={processing}
          className="rounded border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50"
        >
          Cancel
        </button>
      </div>
    );
  }

  return (
    <div className="flex gap-2">
      <button
        onClick={() => setConfirming("approve")}
        className="rounded bg-green-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-green-700"
      >
        Approve
      </button>
      <button
        onClick={() => setConfirming("reject")}
        className="rounded bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700"
      >
        Reject
      </button>
    </div>
  );
}
