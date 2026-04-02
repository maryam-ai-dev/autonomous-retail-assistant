"use client";

import { useState } from "react";

interface BudgetRulesProps {
  budgetCap: number | null;
  approvalThreshold: number | null;
  onSave: (budgetCap: number | null, approvalThreshold: number | null) => void;
}

export default function BudgetRules({
  budgetCap,
  approvalThreshold,
  onSave,
}: BudgetRulesProps) {
  const [cap, setCap] = useState(budgetCap?.toString() ?? "");
  const [threshold, setThreshold] = useState(approvalThreshold?.toString() ?? "");
  const [saved, setSaved] = useState(false);

  function handleSave() {
    onSave(
      cap ? parseFloat(cap) : null,
      threshold ? parseFloat(threshold) : null
    );
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">Budget Rules</h3>
      <div className="space-y-3">
        <div>
          <label className="block text-xs text-gray-500 mb-1">Budget Cap</label>
          <input
            type="number"
            step="0.01"
            value={cap}
            onChange={(e) => setCap(e.target.value)}
            className="w-full rounded border border-gray-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Approval Threshold
          </label>
          <input
            type="number"
            step="0.01"
            value={threshold}
            onChange={(e) => setThreshold(e.target.value)}
            className="w-full rounded border border-gray-300 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          onClick={handleSave}
          className="rounded bg-blue-600 px-4 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
        >
          {saved ? "Saved!" : "Save"}
        </button>
      </div>
    </div>
  );
}
