"use client";

import { useParams } from "next/navigation";
import Link from "next/link";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import { BasketBudgetSummary } from "@/features/basket/BasketBudgetSummary";
import { useBasket } from "@/lib/api/useBasket";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];
type BasketItem = components["schemas"]["BasketItemDto"];

export default function BasketPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const { data, isLoading, error } = useBasket(id);

  if (isLoading) return <BasketSkeleton />;
  if (error?.notFound || !data) return <BasketNotFound />;
  return <BasketView basket={data} />;
}

function BasketView({ basket }: { basket: Basket }) {
  const groups = groupByRetailer(basket.items ?? []);
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
          <RetailerSection
            key={retailer}
            retailer={retailer}
            items={items}
            subtotal={subtotal}
          />
        ))
      )}
    </div>
  );
}

function RetailerSection({
  retailer,
  items,
  subtotal,
}: {
  retailer: string;
  items: BasketItem[];
  subtotal: number;
}) {
  return (
    <section
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
          <li
            key={item.id}
            className="flex items-center justify-between rounded-xl p-3"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
            }}
          >
            <div className="flex flex-col gap-0.5">
              <span
                className="text-sm font-medium"
                style={{ color: "var(--aubergine)" }}
              >
                {item.name}
              </span>
              <span className="text-xs" style={{ color: "var(--muted)" }}>
                {item.brand ?? ""} · Qty {item.quantity}
              </span>
            </div>
            <span
              className="text-sm"
              style={{
                color: "var(--aubergine)",
                fontVariantNumeric: "tabular-nums",
              }}
            >
              £{(item.price * item.quantity).toFixed(2)}
            </span>
          </li>
        ))}
      </ul>
    </section>
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
    subtotal: groupItems.reduce(
      (sum, item) =>
        sum +
        (Number.isFinite(item.price) ? item.price * item.quantity : 0),
      0,
    ),
  }));
}
