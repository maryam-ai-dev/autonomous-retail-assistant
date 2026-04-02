"use client";

import type { ApprovalRequest } from "@/features/approvals/services/approvals-service";
import ApprovalActionBar from "./ApprovalActionBar";

interface ApprovalDetailsProps {
  approval: ApprovalRequest;
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  onClose: () => void;
  processing: boolean;
}

export default function ApprovalDetails({
  approval,
  onApprove,
  onReject,
  onClose,
  processing,
}: ApprovalDetailsProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-bold">Approval Details</h2>
          <button
            onClick={onClose}
            className="rounded p-1 text-gray-500 hover:bg-gray-100"
          >
            &times;
          </button>
        </div>

        <div className="mb-4 space-y-3">
          <div>
            <p className="text-xs text-gray-500">Status</p>
            <p className="text-sm font-medium">{approval.status}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Reason</p>
            <p className="text-sm">{approval.triggerReason}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Total Amount</p>
            <p className="text-sm font-bold">
              USD {approval.totalAmount.toFixed(2)}
            </p>
          </div>
          <div>
            <p className="text-xs text-gray-500">Created</p>
            <p className="text-sm">
              {new Date(approval.createdAt).toLocaleString()}
            </p>
          </div>
          {approval.decidedAt && (
            <div>
              <p className="text-xs text-gray-500">Decided</p>
              <p className="text-sm">
                {new Date(approval.decidedAt).toLocaleString()} — {approval.decision}
              </p>
            </div>
          )}
        </div>

        {approval.status === "PENDING" && (
          <ApprovalActionBar
            onApprove={() => onApprove(approval.id)}
            onReject={() => onReject(approval.id)}
            processing={processing}
          />
        )}
      </div>
    </div>
  );
}
