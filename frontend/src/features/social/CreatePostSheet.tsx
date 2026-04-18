"use client";

import { useState } from "react";
import { BottomSheet } from "@/shared/ui/BottomSheet";
import { Button } from "@/shared/ui/Button";
import { Input } from "@/shared/ui/Input";
import type { components } from "@/types/api.generated";
import type { CreateProductPostPayload } from "./createPost";

type ReactionType = components["schemas"]["PostDto"]["reactions"][number]["type"];
type RetailerKey = components["schemas"]["PostDto"]["product"] extends infer P
  ? P extends { retailer: infer R }
    ? R
    : never
  : never;

type CreatePostSheetProps = {
  open: boolean;
  onClose: () => void;
  onSubmit: (payload: CreateProductPostPayload) => Promise<void>;
};

const REACTIONS: { key: ReactionType; label: string }[] = [
  { key: "TRIED_THIS", label: "Tried this" },
  { key: "BETTER_ALT", label: "Better alt" },
  { key: "WOULDNT_RECOMMEND", label: "Wouldn't recommend" },
];

const RETAILERS: RetailerKey[] = [
  "TESCO",
  "SAINSBURYS",
  "BOOTS",
  "ARGOS",
  "ASDA",
  "MORRISONS",
  "OCADO",
];

export function CreatePostSheet({
  open,
  onClose,
  onSubmit,
}: CreatePostSheetProps) {
  const [caption, setCaption] = useState("");
  const [productName, setProductName] = useState("");
  const [brand, setBrand] = useState("");
  const [retailer, setRetailer] = useState<RetailerKey>("TESCO");
  const [price, setPrice] = useState("");
  const [reaction, setReaction] = useState<ReactionType>("TRIED_THIS");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const priceNumber = Number.parseFloat(price);
  const canSubmit =
    caption.trim() &&
    productName.trim() &&
    Number.isFinite(priceNumber) &&
    priceNumber >= 0;

  async function handleSubmit() {
    if (!canSubmit || submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({
        caption: caption.trim(),
        reactionType: reaction,
        product: {
          retailer,
          name: productName.trim(),
          brand: brand.trim() || null,
          price: priceNumber,
        },
      });
      resetAndClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not post");
    } finally {
      setSubmitting(false);
    }
  }

  function resetAndClose() {
    setCaption("");
    setProductName("");
    setBrand("");
    setPrice("");
    setRetailer("TESCO");
    setReaction("TRIED_THIS");
    setError(null);
    onClose();
  }

  return (
    <BottomSheet open={open} onClose={resetAndClose} title="Share a find">
      <div className="flex flex-col gap-4">
        <Input
          label="Product name"
          value={productName}
          onChange={(e) => setProductName(e.target.value)}
          required
        />
        <Input
          label="Brand (optional)"
          value={brand}
          onChange={(e) => setBrand(e.target.value)}
        />
        <div className="flex flex-col gap-1.5">
          <label
            htmlFor="create-post-retailer"
            className="text-xs font-medium uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Retailer
          </label>
          <select
            id="create-post-retailer"
            value={retailer}
            onChange={(e) => setRetailer(e.target.value as RetailerKey)}
            className="w-full rounded-xl px-4 py-3 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
            }}
          >
            {RETAILERS.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        </div>
        <Input
          label="Price (£)"
          type="number"
          step="0.01"
          min="0"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          required
        />
        <div className="flex flex-col gap-1.5">
          <span
            className="text-xs font-medium uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Your take
          </span>
          <div className="flex flex-wrap gap-2" role="radiogroup" aria-label="Reaction">
            {REACTIONS.map((r) => {
              const on = reaction === r.key;
              return (
                <button
                  key={r.key}
                  type="button"
                  role="radio"
                  aria-checked={on}
                  onClick={() => setReaction(r.key)}
                  className="inline-flex items-center rounded-full px-3 py-2 text-xs font-medium"
                  style={{
                    background: on ? "var(--clay-light)" : "var(--cream)",
                    border: `1px solid ${on ? "var(--clay)" : "var(--border)"}`,
                    color: on ? "var(--clay)" : "var(--aubergine)",
                    minHeight: 36,
                  }}
                >
                  {r.label}
                </button>
              );
            })}
          </div>
        </div>
        <div className="flex flex-col gap-1.5">
          <label
            htmlFor="create-post-caption"
            className="text-xs font-medium uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Caption
          </label>
          <textarea
            id="create-post-caption"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            rows={3}
            placeholder="Why this? What caught your eye?"
            className="w-full rounded-xl px-4 py-3 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
            }}
          />
        </div>
        {error ? (
          <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
            {error}
          </p>
        ) : null}
        <div className="flex gap-3">
          <Button variant="ghost" onClick={resetAndClose}>
            Cancel
          </Button>
          <div className="flex-1" />
          <Button
            variant="primary"
            onClick={handleSubmit}
            disabled={!canSubmit || submitting}
          >
            {submitting ? "Posting…" : "Post"}
          </Button>
        </div>
      </div>
    </BottomSheet>
  );
}
