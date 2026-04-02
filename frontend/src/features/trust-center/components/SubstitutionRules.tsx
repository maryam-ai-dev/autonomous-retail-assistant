"use client";

import { useState } from "react";

interface SubstitutionRulesProps {
  allowSubstitutions: boolean;
  maxDelta: number | null;
  onSave: (allow: boolean, maxDelta: number | null) => void;
}

export default function SubstitutionRules({
  allowSubstitutions,
  maxDelta,
  onSave,
}: SubstitutionRulesProps) {
  const [allow, setAllow] = useState(allowSubstitutions);
  const [delta, setDelta] = useState(maxDelta?.toString() ?? "");
  const [saved, setSaved] = useState(false);

  function handleSave() {
    onSave(allow, delta ? parseFloat(delta) : null);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <h3 className="mb-3 text-sm font-semibold">Substitution Rules</h3>
      <div className="space-y-3">
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            checked={allow}
            onChange={(e) => setAllow(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300"
          />
          <label className="text-sm">Allow substitutions</label>
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Max Price Delta
          </label>
          <input
            type="number"
            step="0.01"
            value={delta}
            onChange={(e) => setDelta(e.target.value)}
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
