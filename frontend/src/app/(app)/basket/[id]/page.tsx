"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import { BasketApproveBar } from "@/features/basket/BasketApproveBar";
import { BasketBudgetSummary } from "@/features/basket/BasketBudgetSummary";
import { BasketItemCard } from "@/features/basket/BasketItemCard";
import { CheckoutActions } from "@/features/basket/CheckoutActions";
import { SwapSheet } from "@/features/basket/SwapSheet";
import { approveBasket } from "@/features/basket/checkout";
import { useBasket } from "@/lib/api/useBasket";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];
type BasketItem = components["schemas"]["BasketItemDto"];

export default function BasketPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const { data, isLoading, error } = useBasket(id);

  const [basket, setBasket] = useState<Basket | null>(null);
  const [swapTarget, setSwapTarget] = useState<BasketItem | null>(null);
  const [approving, setApproving] = useState(false);
  const [approveError, setApproveError] = useState<string | null>(null);

  useEffect(() => {
    if (data) setBasket(data);
  }, [data]);

  if (isLoading && !basket) return <BasketSkeleton />;
  if (error?.notFound || !basket) return <BasketNotFound />;

  const updateQuantity = (itemId: string, quantity: number) => {
    setBasket((prev) => {
      if (!prev) return prev;
      const items = prev.items.map((item) =>
        item.id === itemId ? { ...item, quantity } : item,
      );
      return { ...prev, items, totalCost: totalCostOf(items) };
    });
  };

  const removeItem = (itemId: string) => {
    setBasket((prev) => {
      if (!prev) return prev;
      const items = prev.items.filter((item) => item.id !== itemId);
      return { ...prev, items, totalCost: totalCostOf(items) };
    });
  };

  const swapItem = (altProduct: {
    productId: string;
    name: string;
    brand: string | null;
    retailer: BasketItem["retailer"];
    price: number;
    whyThis: string;
  }) => {
    if (!swapTarget) return;
    setBasket((prev) => {
      if (!prev) return prev;
      const items = prev.items.map((item) =>
        item.id === swapTarget.id
          ? {
              ...item,
              productId: altProduct.productId,
              name: altProduct.name,
              brand: altProduct.brand,
              retailer: altProduct.retailer,
              price: altProduct.price,
              whyThis: altProduct.whyThis,
              substitutionFlag: null,
            }
          : item,
      );
      return { ...prev, items, totalCost: totalCostOf(items) };
    });
    setSwapTarget(null);
  };

  async function handleApprove() {
    if (approving || !basket) return;
    setApproving(true);
    setApproveError(null);
    const previousStatus = basket.status;
    setBasket({ ...basket, status: "APPROVED" });
    const result = await approveBasket(basket.id);
    if (!result.ok) {
      setBasket((prev) => (prev ? { ...prev, status: previousStatus } : prev));
      setApproveError(result.message);
    } else {
      setBasket((prev) =>
        prev ? { ...prev, status: result.basket.status } : prev,
      );
    }
    setApproving(false);
  }

  const groups = groupByRetailer(basket.items ?? []);
  const isApproved = basket.status === "APPROVED" || basket.status === "CHECKED_OUT";

  return (
    <div className="flex flex-col gap-5">
      <PageHeader title="Your basket" leftSlot={<BackButton />} />

      <section
        aria-label="Basket budget"
        className="flex flex-col gap-3 rounded-2xl p-5"
        style={{
          background: "var(--oat)",
          border: "1px solid var(--border)",
        }}
      >
        <span
          className="inline-flex max-w-max items-center rounded-full px-3 py-1 text-xs font-medium"
          style={{
            background: "var(--clay-light)",
            color: "var(--clay)",
          }}
        >
          {basket.intentText}
        </span>
        <span
          className="text-4xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
            fontVariantNumeric: "tabular-nums",
          }}
        >
          £{basket.totalCost.toFixed(2)}
        </span>
        <BasketBudgetSummary basket={basket} />
      </section>

      {groups.length === 0 ? (
        <EmptyBasket />
      ) : (
        groups.map(({ retailer, items, subtotal }) => (
          <section
            key={retailer}
            aria-label={`${retailer} items`}
            className="flex flex-col gap-3"
          >
            <div className="flex items-center justify-between">
              <RetailerBadge retailer={retailer} />
              <span
                className="text-sm font-medium"
                style={{
                  color: "var(--aubergine)",
                  fontVariantNumeric: "tabular-nums",
                }}
              >
                £{subtotal.toFixed(2)}
              </span>
            </div>
            <ul className="flex flex-col gap-2">
              {items.map((item) => (
                <BasketItemCard
                  key={item.id}
                  item={item}
                  onQuantityChange={updateQuantity}
                  onRemove={removeItem}
                  onSwap={(target) => setSwapTarget(target)}
                />
              ))}
            </ul>
          </section>
        ))
      )}

      {isApproved ? (
        <CheckoutActions basket={basket} />
      ) : (
        <>
          {approveError ? (
            <p
              role="alert"
              className="text-sm"
              style={{ color: "var(--amber)" }}
            >
              {approveError}
            </p>
          ) : null}
          <BasketApproveBar
            basket={basket}
            onApprove={handleApprove}
            approving={approving}
          />
        </>
      )}

      <SwapSheet
        open={swapTarget !== null}
        basketId={basket.id}
        item={swapTarget}
        onClose={() => setSwapTarget(null)}
        onSelect={swapItem}
      />
    </div>
  );
}

function BasketSkeleton() {
  return (
    <div className="flex flex-col gap-4">
      <div
        className="h-8 w-40 rounded"
        style={{ background: "var(--oat)" }}
      />
      <div
        className="h-40 rounded-2xl"
        style={{ background: "var(--oat)" }}
      />
      <div
        className="h-24 rounded-2xl"
        style={{ background: "var(--oat)" }}
      />
    </div>
  );
}

function BasketNotFound() {
  return (
    <div className="flex flex-col items-center gap-4 pt-10 text-center">
      <h1
        className="text-2xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        We couldn&apos;t find that basket
      </h1>
      <p className="text-sm" style={{ color: "var(--muted)" }}>
        It may have been deleted or the link isn&apos;t quite right.
      </p>
      <Link
        href="/home"
        className="text-sm font-semibold underline"
        style={{ color: "var(--clay)" }}
      >
        Go back home
      </Link>
    </div>
  );
}

function EmptyBasket() {
  return (
    <p className="text-sm italic" style={{ color: "var(--muted)" }}>
      Nothing here yet — add something to get started.
    </p>
  );
}

function totalCostOf(items: BasketItem[]): number {
  return items.reduce(
    (sum, item) =>
      sum +
      (Number.isFinite(item.price) ? item.price * item.quantity : 0),
    0,
  );
}

function groupByRetailer(items: BasketItem[]) {
  const map = new Map<string, BasketItem[]>();
  for (const item of items) {
    const key = item.retailer;
    const list = map.get(key) ?? [];
    list.push(item);
    map.set(key, list);
  }
  return Array.from(map.entries()).map(([retailer, groupItems]) => ({
    retailer,
    items: groupItems,
    subtotal: totalCostOf(groupItems),
  }));
}
