"use client";

import { useState } from "react";
import { Button } from "@/shared/ui/Button";
import { saveQuickSize } from "./submit";

type QuickSizePromptProps = {
  onSaved: () => void;
  onSkip: () => void;
};

const TOP_SIZES = ["XS", "S", "M", "L", "XL", "XXL"];
const SHOE_SIZES = [
  "3", "3.5", "4", "4.5", "5", "5.5", "6", "6.5", "7", "7.5",
  "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12",
];

/**
 * Sprint F35.2: proactive inline size prompt that fires before the server-
 * side 428 gate for size-dependent fashion intents. Shown above the submit
 * button when the user's clothing profile is incomplete. "Save and continue"
 * persists the sizes and resubmits; "Skip for now" submits as-is and the
 * 428 gate will catch it if Spring still requires a profile.
 */
export function QuickSizePrompt({ onSaved, onSkip }: QuickSizePromptProps) {
  const [topSize, setTopSize] = useState<string>("");
  const [shoeSize, setShoeSize] = useState<string>("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSave() {
    if (!topSize && !shoeSize) {
      setError("Pick at least one size or tap skip for now.");
      return;
    }
    setError(null);
    setSaving(true);
    const result = await saveQuickSize({ topSize, shoeSizeUk: shoeSize });
    setSaving(false);
    if (result.ok) {
      onSaved();
    } else {
      setError(result.message ?? "Couldn't save — try again.");
    }
  }

  return (
    <div
      role="group"
      aria-label="Clothing size prompt"
      className="flex flex-col gap-3 rounded-2xl p-4"
      style={{
        background: "var(--cream)",
        border: "1px solid var(--border)",
      }}
    >
      <p
        className="text-sm font-medium"
        style={{ color: "var(--aubergine)" }}
      >
        Quick question — what&apos;s your clothing size?
      </p>
      <p className="text-xs" style={{ color: "var(--muted)" }}>
        We&apos;ll only ask once.
      </p>
      <div className="flex gap-3">
        <label className="flex flex-1 flex-col gap-1">
          <span
            className="text-xs uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Tops
          </span>
          <select
            value={topSize}
            onChange={(e) => setTopSize(e.target.value)}
            aria-label="Top size"
            className="rounded-xl px-3 py-2 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
              minHeight: 44,
            }}
          >
            <option value="">Select</option>
            {TOP_SIZES.map((size) => (
              <option key={size} value={size}>
                {size}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-1 flex-col gap-1">
          <span
            className="text-xs uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Shoe (UK)
          </span>
          <select
            value={shoeSize}
            onChange={(e) => setShoeSize(e.target.value)}
            aria-label="UK shoe size"
            className="rounded-xl px-3 py-2 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
              minHeight: 44,
            }}
          >
            <option value="">Select</option>
            {SHOE_SIZES.map((size) => (
              <option key={size} value={size}>
                {size}
              </option>
            ))}
          </select>
        </label>
      </div>
      {error ? (
        <p role="alert" className="text-xs" style={{ color: "#6B2A11" }}>
          {error}
        </p>
      ) : null}
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={onSkip} disabled={saving}>
          Skip for now
        </Button>
        <div className="flex-1" />
        <Button variant="primary" onClick={handleSave} disabled={saving}>
          {saving ? "Saving…" : "Save and continue"}
        </Button>
      </div>
    </div>
  );
}
