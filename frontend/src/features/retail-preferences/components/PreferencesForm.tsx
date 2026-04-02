"use client";

import { useEffect, useState } from "react";
import {
  getPreferences,
  updatePreferences,
  type PreferencesData,
} from "@/features/retail-preferences/services/preferences-service";

export default function PreferencesForm() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  const [budgetCap, setBudgetCap] = useState("");
  const [preferredBrands, setPreferredBrands] = useState("");
  const [blockedBrands, setBlockedBrands] = useState("");
  const [allowSubstitutions, setAllowSubstitutions] = useState(true);
  const [approvalThreshold, setApprovalThreshold] = useState("");

  useEffect(() => {
    getPreferences()
      .then((data: PreferencesData) => {
        setBudgetCap(data.budgetCap?.toString() ?? "");
        setPreferredBrands(data.preferredBrands?.join(", ") ?? "");
        setBlockedBrands(data.blockedBrands?.join(", ") ?? "");
        setAllowSubstitutions(data.allowSubstitutions);
        setApprovalThreshold(data.approvalThreshold?.toString() ?? "");
      })
      .catch(() => setError("Failed to load preferences"))
      .finally(() => setLoading(false));
  }, []);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);

    try {
      const parseTags = (val: string) =>
        val
          .split(",")
          .map((s) => s.trim())
          .filter((s) => s.length > 0);

      await updatePreferences({
        budgetCap: budgetCap ? parseFloat(budgetCap) : null,
        preferredBrands: parseTags(preferredBrands),
        blockedBrands: parseTags(blockedBrands),
        allowSubstitutions,
        approvalThreshold: approvalThreshold
          ? parseFloat(approvalThreshold)
          : null,
      });
      setSuccess("Preferences saved successfully.");
    } catch {
      setError("Failed to save preferences.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <div>Loading preferences...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-lg space-y-4">
      <h1 className="text-2xl font-bold">Retail Preferences</h1>

      {error && (
        <div className="rounded bg-red-50 p-3 text-sm text-red-600">
          {error}
        </div>
      )}
      {success && (
        <div className="rounded bg-green-50 p-3 text-sm text-green-600">
          {success}
        </div>
      )}

      <div>
        <label htmlFor="budgetCap" className="block text-sm font-medium mb-1">
          Budget Cap
        </label>
        <input
          id="budgetCap"
          type="number"
          step="0.01"
          value={budgetCap}
          onChange={(e) => setBudgetCap(e.target.value)}
          className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label
          htmlFor="preferredBrands"
          className="block text-sm font-medium mb-1"
        >
          Preferred Brands (comma-separated)
        </label>
        <input
          id="preferredBrands"
          type="text"
          value={preferredBrands}
          onChange={(e) => setPreferredBrands(e.target.value)}
          className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label
          htmlFor="blockedBrands"
          className="block text-sm font-medium mb-1"
        >
          Blocked Brands (comma-separated)
        </label>
        <input
          id="blockedBrands"
          type="text"
          value={blockedBrands}
          onChange={(e) => setBlockedBrands(e.target.value)}
          className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div className="flex items-center gap-2">
        <input
          id="allowSubstitutions"
          type="checkbox"
          checked={allowSubstitutions}
          onChange={(e) => setAllowSubstitutions(e.target.checked)}
          className="h-4 w-4 rounded border-gray-300"
        />
        <label htmlFor="allowSubstitutions" className="text-sm font-medium">
          Allow Substitutions
        </label>
      </div>

      <div>
        <label
          htmlFor="approvalThreshold"
          className="block text-sm font-medium mb-1"
        >
          Approval Threshold
        </label>
        <input
          id="approvalThreshold"
          type="number"
          step="0.01"
          value={approvalThreshold}
          onChange={(e) => setApprovalThreshold(e.target.value)}
          className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <button
        type="submit"
        disabled={saving}
        className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
      >
        {saving ? "Saving..." : "Save Preferences"}
      </button>
    </form>
  );
}
