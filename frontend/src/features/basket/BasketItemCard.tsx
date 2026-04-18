"use client";

import { DietaryCertaintyBadge } from "@/shared/ui/DietaryCertaintyBadge";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import type { components } from "@/types/api.generated";
import type { DietaryTag } from "@/lib/dietary";
import { DIETARY_UI } from "@/lib/dietary";

type BasketItem = components["schemas"]["BasketItemDto"];

type BasketItemCardProps = {
  item: BasketItem;
  onQuantityChange: (id: string, quantity: number) => void;
  onRemove: (id: string) => void;
  onSwap: (item: BasketItem) => void;
};

export function BasketItemCard({
  item,
  onQuantityChange,
  onRemove,
  onSwap,
}: BasketItemCardProps) {
  const hasWarnings = (item.normalizationWarnings ?? []).length > 0;
  const dietaryBadges = (item.dietaryTags ?? []).filter(
    (tag): tag is DietaryTag => tag in DIETARY_UI,
  );
  const flagged = item.substitutionFlag ?? null;

  return (
    <li
      className="flex flex-col gap-3 rounded-xl p-3"
      style={{
        background: flagged ? "var(--amber-light)" : "var(--oat)",
        border: `1px solid ${flagged ? "var(--amber)" : "var(--border)"}`,
      }}
    >
      {flagged ? (
        <div className="flex items-start justify-between gap-2">
          <div className="flex flex-col gap-1">
            <span
              className="text-xs font-semibold uppercase tracking-wide"
              style={{ color: "#6B2A11" }}
            >
              Substitution flagged
            </span>
            <p className="text-sm" style={{ color: "#6B2A11" }}>
              {flagged.message}
            </p>
          </div>
          <button
            type="button"
            onClick={() => onSwap(item)}
            aria-label={`See alternatives for ${item.name}`}
            className="shrink-0 text-xs font-semibold"
            style={{ color: "#6B2A11", minHeight: 44 }}
          >
            See alternatives →
          </button>
        </div>
      ) : null}

      <div className="flex gap-3">
      <Thumbnail imageUrl={item.imageUrl} alt={item.name} />
      <div className="flex flex-1 flex-col gap-1.5">
        <div className="flex items-start justify-between gap-2">
          <span
            className="text-sm font-medium leading-snug"
            style={{ color: "var(--aubergine)" }}
          >
            {item.name}
          </span>
          <span
            className="text-sm font-semibold"
            style={{
              color: "var(--aubergine)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            £{(item.price * item.quantity).toFixed(2)}
          </span>
        </div>
        <div className="flex items-center gap-2">
          <RetailerBadge retailer={item.retailer} />
          {hasWarnings ? (
            <span
              role="img"
              aria-label="Dietary tags unverified"
              title={item.normalizationWarnings.join(". ")}
              className="inline-flex h-4 w-4 items-center justify-center rounded-full text-[9px] font-bold"
              style={{ background: "var(--amber-light)", color: "#6B2A11" }}
            >
              !
            </span>
          ) : null}
        </div>
        <p className="text-xs" style={{ color: "var(--muted)" }}>
          {item.whyThis}
        </p>
        {dietaryBadges.length > 0 ? (
          <div className="flex flex-wrap gap-1.5">
            {dietaryBadges.map((tag) => (
              <DietaryCertaintyBadge key={tag} tag={tag} />
            ))}
          </div>
        ) : null}
        <div className="flex items-center gap-2 pt-1">
          <QuantityStepper
            value={item.quantity}
            onChange={(next) => onQuantityChange(item.id, next)}
            itemName={item.name}
          />
          <button
            type="button"
            onClick={() => onSwap(item)}
            aria-label={`Swap ${item.name}`}
            className="text-xs font-semibold"
            style={{ color: "var(--clay)", minHeight: 44 }}
          >
            Swap
          </button>
          <div className="flex-1" />
          <button
            type="button"
            onClick={() => onRemove(item.id)}
            aria-label={`Remove ${item.name}`}
            className="inline-flex items-center justify-center rounded-full"
            style={{
              color: "var(--muted)",
              minHeight: 36,
              minWidth: 36,
            }}
          >
            <span aria-hidden="true" className="text-lg">×</span>
            <span className="ml-1 text-xs font-medium">Remove</span>
          </button>
        </div>
      </div>
      </div>
    </li>
  );
}

function Thumbnail({
  imageUrl,
  alt,
}: {
  imageUrl?: string | null;
  alt: string;
}) {
  const style = {
    width: 38,
    height: 38,
    background: "var(--cream)",
    border: "1px solid var(--border)",
  } as const;
  if (imageUrl) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={imageUrl}
        alt={alt}
        className="shrink-0 rounded-lg object-cover"
        style={style}
      />
    );
  }
  return (
    <div
      aria-hidden="true"
      className="shrink-0 rounded-lg"
      style={style}
    />
  );
}

function QuantityStepper({
  value,
  onChange,
  itemName,
}: {
  value: number;
  onChange: (next: number) => void;
  itemName: string;
}) {
  return (
    <div
      className="inline-flex items-center gap-1 rounded-full"
      style={{ background: "var(--cream)", border: "1px solid var(--border)" }}
    >
      <button
        type="button"
        onClick={() => onChange(Math.max(1, value - 1))}
        aria-label={`Decrease quantity of ${itemName}`}
        disabled={value <= 1}
        className="inline-flex items-center justify-center rounded-full text-sm font-semibold"
        style={{
          color: "var(--aubergine)",
          minHeight: 36,
          minWidth: 36,
          opacity: value <= 1 ? 0.4 : 1,
          cursor: value <= 1 ? "not-allowed" : "pointer",
        }}
      >
        −
      </button>
      <span
        aria-label={`Quantity ${value}`}
        aria-live="polite"
        className="w-5 text-center text-sm font-semibold"
        style={{
          color: "var(--aubergine)",
          fontVariantNumeric: "tabular-nums",
        }}
      >
        {value}
      </span>
      <button
        type="button"
        onClick={() => onChange(value + 1)}
        aria-label={`Increase quantity of ${itemName}`}
        className="inline-flex items-center justify-center rounded-full text-sm font-semibold"
        style={{
          color: "var(--aubergine)",
          minHeight: 36,
          minWidth: 36,
        }}
      >
        +
      </button>
    </div>
  );
}
