"use client";

import axios from "axios";
import { useEffect, useState } from "react";
import apiClient from "@/core/api/client";
import { BottomSheet } from "@/shared/ui/BottomSheet";
import { Button } from "@/shared/ui/Button";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type BasketItem = components["schemas"]["BasketItemDto"];

type SwapAlternative = {
  productId: string;
  name: string;
  brand: string | null;
  retailer: BasketItem["retailer"];
  price: number;
  whyThis: string;
};

type SwapSheetProps = {
  open: boolean;
  basketId: string;
  item: BasketItem | null;
  onClose: () => void;
  onSelect: (alt: SwapAlternative) => void;
};

export function SwapSheet({
  open,
  basketId,
  item,
  onClose,
  onSelect,
}: SwapSheetProps) {
  const [alts, setAlts] = useState<SwapAlternative[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !item) return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setAlts(null);
    setError(null);
    fetchAlternatives(basketId, item.id, item)
      .then((result) => setAlts(result))
      .catch(() => {
        setError("No alternatives found");
        setAlts([]);
      });
  }, [open, basketId, item]);

  return (
    <BottomSheet open={open} onClose={onClose} title="Swap for…">
      {alts === null && !error ? (
        <SwapSkeleton />
      ) : error || (alts && alts.length === 0) ? (
        <p className="text-sm" style={{ color: "var(--muted)" }}>
          No alternatives found right now.
        </p>
      ) : (
        <ul className="flex flex-col gap-2">
          {(alts ?? []).map((alt) => (
            <li
              key={alt.productId}
              className="flex items-center justify-between rounded-xl p-3"
              style={{
                background: "var(--cream)",
                border: "1px solid var(--border)",
              }}
            >
              <div className="flex flex-col gap-1">
                <span
                  className="text-sm font-medium"
                  style={{ color: "var(--aubergine)" }}
                >
                  {alt.name}
                </span>
                <div className="flex items-center gap-2">
                  <RetailerBadge retailer={alt.retailer} />
                  <span className="text-xs" style={{ color: "var(--muted)" }}>
                    {alt.whyThis}
                  </span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className="text-sm font-semibold"
                  style={{
                    color: "var(--aubergine)",
                    fontVariantNumeric: "tabular-nums",
                  }}
                >
                  £{alt.price.toFixed(2)}
                </span>
                <Button
                  variant="secondary"
                  onClick={() => onSelect(alt)}
                >
                  Swap
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </BottomSheet>
  );
}

function SwapSkeleton() {
  return (
    <div
      role="status"
      aria-live="polite"
      className="flex flex-col gap-2"
    >
      {[0, 1, 2].map((idx) => (
        <div
          key={idx}
          className="h-14 rounded-xl"
          style={{ background: "var(--cream)" }}
        />
      ))}
      <span className="sr-only">Loading alternatives…</span>
    </div>
  );
}

async function fetchAlternatives(
  basketId: string,
  itemId: string,
  item: BasketItem,
): Promise<SwapAlternative[]> {
  if (USE_MOCKS) return buildMockAlternatives(item);
  try {
    const response = await apiClient.get<SwapAlternative[]>(
      `/api/baskets/${encodeURIComponent(basketId)}/items/${encodeURIComponent(itemId)}/alternatives`,
      { timeout: 10_000 },
    );
    if (!Array.isArray(response.data)) return [];
    return response.data.slice(0, 3);
  } catch (err) {
    if (axios.isAxiosError(err) && err.response?.status === 404) return [];
    throw err;
  }
}

function buildMockAlternatives(item: BasketItem): SwapAlternative[] {
  const base = item.price;
  return [
    {
      productId: `${item.id}-alt-1`,
      name: `${item.name} (own-brand)`,
      brand: "Own brand",
      retailer: item.retailer,
      price: Math.max(0.3, base * 0.75),
      whyThis: "Cheaper alternative in the same category.",
    },
    {
      productId: `${item.id}-alt-2`,
      name: `${item.name} (premium)`,
      brand: item.brand ?? "Premium",
      retailer: item.retailer,
      price: base * 1.15,
      whyThis: "Higher-quality option.",
    },
    {
      productId: `${item.id}-alt-3`,
      name: `${item.name} (larger pack)`,
      brand: item.brand ?? "Own brand",
      retailer: item.retailer,
      price: base * 1.45,
      whyThis: "Better value per unit if you go through a lot.",
    },
  ];
}
