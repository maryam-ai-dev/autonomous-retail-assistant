"use client";

import { useEffect, useState } from "react";
import { useRequireAuth } from "@/core/auth/guards";
import ApprovalQueue from "@/features/approvals/components/ApprovalQueue";
import ApprovalDetails from "@/features/approvals/components/ApprovalDetails";
import {
  getApprovals,
  approve,
  reject,
  type ApprovalRequest,
} from "@/features/approvals/services/approvals-service";

export default function ApprovalsPage() {
  useRequireAuth();

  const [approvals, setApprovals] = useState<ApprovalRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processing, setProcessing] = useState(false);
  const [selected, setSelected] = useState<ApprovalRequest | null>(null);

  useEffect(() => {
    loadApprovals();
  }, []);

  async function loadApprovals() {
    try {
      const data = await getApprovals();
      setApprovals(data);
    } catch {
      setError("Failed to load approvals");
    } finally {
      setLoading(false);
    }
  }

  async function handleApprove(id: string) {
    setProcessing(true);
    try {
      await approve(id);
      setSelected(null);
      await loadApprovals();
    } catch {
      setError("Failed to approve");
    } finally {
      setProcessing(false);
    }
  }

  async function handleReject(id: string) {
    setProcessing(true);
    try {
      await reject(id);
      setSelected(null);
      await loadApprovals();
    } catch {
      setError("Failed to reject");
    } finally {
      setProcessing(false);
    }
  }

  if (loading) {
    return <div className="py-8 text-center text-gray-500">Loading approvals...</div>;
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Approvals</h1>

      {error && (
        <div className="rounded bg-red-50 p-3 text-sm text-red-600">{error}</div>
      )}

      <ApprovalQueue
        approvals={approvals}
        onApprove={handleApprove}
        onReject={handleReject}
        onSelect={setSelected}
        processing={processing}
      />

      {selected && (
        <ApprovalDetails
          approval={selected}
          onApprove={handleApprove}
          onReject={handleReject}
          onClose={() => setSelected(null)}
          processing={processing}
        />
      )}
    </div>
  );
}
