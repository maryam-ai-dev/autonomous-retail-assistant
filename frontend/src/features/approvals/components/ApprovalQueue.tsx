"use client";

import type { ApprovalRequest } from "@/features/approvals/services/approvals-service";
import ApprovalActionBar from "./ApprovalActionBar";

interface ApprovalQueueProps {
  approvals: ApprovalRequest[];
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  onSelect: (approval: ApprovalRequest) => void;
  processing: boolean;
}

export default function ApprovalQueue({
  approvals,
  onApprove,
  onReject,
  onSelect,
  processing,
}: ApprovalQueueProps) {
  if (approvals.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p className="text-lg font-medium">No pending approvals</p>
        <p className="text-sm">All purchases have been reviewed</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {approvals.map((approval) => (
        <div
          key={approval.id}
          className="cursor-pointer rounded-lg border border-gray-200 bg-white p-4 hover:border-blue-300"
          onClick={() => onSelect(approval)}
        >
          <div className="mb-2 flex items-start justify-between">
            <div>
              <p className="text-sm font-medium">{approval.triggerReason}</p>
              <p className="text-xs text-gray-500">
                {new Date(approval.createdAt).toLocaleString()}
              </p>
            </div>
            <span className="text-sm font-bold">
              USD {approval.totalAmount.toFixed(2)}
            </span>
          </div>

          <div onClick={(e) => e.stopPropagation()}>
            <ApprovalActionBar
              onApprove={() => onApprove(approval.id)}
              onReject={() => onReject(approval.id)}
              processing={processing}
            />
          </div>
        </div>
      ))}
    </div>
  );
}
