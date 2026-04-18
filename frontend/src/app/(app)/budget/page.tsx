"use client";

import { useState } from "react";
import Link from "next/link";
import { Button } from "@/shared/ui/Button";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import { useBasketHistory } from "@/lib/api/useBasketHistory";
import { useBudgetSummary } from "@/lib/api/useBudgetSummary";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];

type BudgetSummary = components["schemas"]["BudgetSummaryDto"];

function currentMonth(): string {
  const d = new Date();
  const m = `${d.getMonth() + 1}`.padStart(2, "0");
  return `${d.getFullYear()}-${m}`;
}

export default function BudgetPage() {
  const [month, setMonth] = useState<string>(currentMonth());
  const { data, isLoading, error } = useBudgetSummary(month);
  const history = useBasketHistory(month);

  if (isLoading && !data) return <BudgetSkeleton />;
  if (error || !data) return <BudgetError />;

  const hasNoData = data.basketsCount === 0 && data.spent === 0;

  return (
    <div className="flex flex-col gap-5">
      <header>
        <h1
          className="text-3xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          Your budget
        </h1>
      </header>

      <MonthSelector month={month} onChange={setMonth} />


      {hasNoData ? (
        <p
          className="py-10 text-center text-sm italic"
          style={{ color: "var(--muted)" }}
        >
          No baskets yet this month — once you build one you&apos;ll see your
          spending here.
        </p>
      ) : (
        <>
          <StatGrid summary={data} />
          <RetailerBars byRetailer={data.byRetailer} spent={data.spent} />
          <InsightsBox insights={data.insights ?? []} />
        </>
      )}

      <BasketHistory
        baskets={history.data ?? []}
        isLoading={history.isLoading}
      />
    </div>
  );
}

function MonthSelector({
  month,
  onChange,
}: {
  month: string;
  onChange: (next: string) => void;
}) {
  function shift(delta: number) {
    const [year, m] = month.split("-").map(Number);
    if (!year || !m) return;
    const d = new Date(year, m - 1 + delta, 1);
    onChange(
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`,
    );
  }
  return (
    <div
      className="flex items-center justify-between rounded-full p-1"
      style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
    >
      <Button variant="ghost" onClick={() => shift(-1)} aria-label="Previous month">
        ←
      </Button>
      <span
        className="text-sm font-semibold"
        style={{ color: "var(--aubergine)" }}
      >
        {formatMonth(month)}
      </span>
      <Button variant="ghost" onClick={() => shift(1)} aria-label="Next month">
        →
      </Button>
    </div>
  );
}

function BasketHistory({
  baskets,
  isLoading,
}: {
  baskets: Basket[];
  isLoading: boolean;
}) {
  if (isLoading && baskets.length === 0) {
    return (
      <div
        className="h-24 rounded-2xl"
        style={{ background: "var(--oat)" }}
      />
    );
  }
  if (baskets.length === 0) {
    return (
      <section aria-label="Basket history" className="flex flex-col gap-2">
        <h2
          className="text-sm font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          Baskets this month
        </h2>
        <p className="text-sm italic" style={{ color: "var(--muted)" }}>
          No baskets for this month.
        </p>
      </section>
    );
  }
  return (
    <section aria-label="Basket history" className="flex flex-col gap-2">
      <h2
        className="text-sm font-semibold"
        style={{ color: "var(--aubergine)" }}
      >
        Baskets this month
      </h2>
      <ul className="flex flex-col gap-2">
        {baskets.map((b) => (
          <li key={b.id}>
            <Link
              href={`/basket/${encodeURIComponent(b.id)}`}
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
                  {b.intentText}
                </span>
                <span className="text-xs" style={{ color: "var(--muted)" }}>
                  {new Date(b.createdAt).toLocaleDateString("en-GB", {
                    day: "numeric",
                    month: "short",
                  })}
                </span>
              </div>
              <div className="flex flex-col items-end gap-1">
                <span
                  className="text-sm font-semibold"
                  style={{
                    color: "var(--aubergine)",
                    fontVariantNumeric: "tabular-nums",
                  }}
                >
                  £{b.totalCost.toFixed(2)}
                </span>
                <span
                  className="rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide"
                  style={statusStyle(b.status)}
                >
                  {b.status}
                </span>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}

function statusStyle(status: Basket["status"]): {
  background: string;
  color: string;
} {
  switch (status) {
    case "APPROVED":
      return { background: "var(--sage-light)", color: "#35502B" };
    case "CHECKED_OUT":
      return { background: "var(--ink-light)", color: "#1A2E47" };
    default:
      return { background: "var(--clay-light)", color: "var(--clay)" };
  }
}

function StatGrid({ summary }: { summary: BudgetSummary }) {
  const tiles: { label: string; value: string }[] = [
    { label: "Spent", value: money(summary.spent) },
    { label: "Back in your pocket", value: money(summary.savedVsFullPrice) },
    { label: "Baskets", value: String(summary.basketsCount) },
    { label: "Avg basket", value: money(summary.avgBasket) },
  ];
  return (
    <div className="grid grid-cols-2 gap-3">
      {tiles.map((t) => (
        <div
          key={t.label}
          className="flex flex-col gap-1 rounded-2xl p-4"
          style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
        >
          <span
            className="text-[11px] uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            {t.label}
          </span>
          <span
            className="text-lg font-semibold"
            style={{
              color: "var(--aubergine)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            {t.value}
          </span>
        </div>
      ))}
    </div>
  );
}

function RetailerBars({
  byRetailer,
  spent,
}: {
  byRetailer: BudgetSummary["byRetailer"];
  spent: number;
}) {
  const entries = Object.entries(byRetailer).sort(
    ([, a], [, b]) => (b ?? 0) - (a ?? 0),
  );
  if (entries.length === 0) return null;
  const max = Math.max(spent, ...entries.map(([, v]) => v ?? 0));
  return (
    <section aria-label="Spending by retailer" className="flex flex-col gap-2">
      <h2
        className="text-sm font-semibold"
        style={{ color: "var(--aubergine)" }}
      >
        By retailer
      </h2>
      <ul className="flex flex-col gap-2">
        {entries.map(([retailer, amount]) => {
          const value = amount ?? 0;
          const pct = max > 0 ? (value / max) * 100 : 0;
          return (
            <li key={retailer} className="flex flex-col gap-1">
              <div className="flex items-center justify-between">
                <RetailerBadge retailer={retailer} />
                <span
                  className="text-xs font-semibold"
                  style={{
                    color: "var(--aubergine)",
                    fontVariantNumeric: "tabular-nums",
                  }}
                >
                  {money(value)}
                </span>
              </div>
              <div
                className="h-2 w-full overflow-hidden rounded-full"
                style={{ background: "var(--oat)" }}
              >
                <div
                  className="h-full"
                  style={{
                    width: `${pct}%`,
                    background: "var(--clay)",
                    transition: "width 300ms ease-out",
                  }}
                />
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}

function InsightsBox({ insights }: { insights: string[] }) {
  if (!insights || insights.length === 0) return null;
  return (
    <section
      aria-label="Aisleon noticed"
      className="flex flex-col gap-2 rounded-2xl p-4"
      style={{
        background: "var(--sage-light)",
        border: "1px solid var(--sage)",
      }}
    >
      <h2
        className="text-sm font-semibold"
        style={{ color: "#35502B" }}
      >
        Aisleon noticed
      </h2>
      <ul className="flex flex-col gap-1 text-sm" style={{ color: "#35502B" }}>
        {insights.map((line, idx) => (
          <li key={idx}>• {line}</li>
        ))}
      </ul>
    </section>
  );
}

function BudgetSkeleton() {
  return (
    <div className="flex flex-col gap-4">
      <div
        className="h-8 w-40 rounded"
        style={{ background: "var(--oat)" }}
      />
      <div className="grid grid-cols-2 gap-3">
        {[0, 1, 2, 3].map((i) => (
          <div
            key={i}
            className="h-20 rounded-2xl"
            style={{ background: "var(--oat)" }}
          />
        ))}
      </div>
    </div>
  );
}

function BudgetError() {
  return (
    <p className="text-sm italic" style={{ color: "var(--muted)" }}>
      Couldn&apos;t load your budget — try again shortly.
    </p>
  );
}

function money(value: number): string {
  return `£${value.toFixed(2)}`;
}

function formatMonth(month: string): string {
  const [year, m] = month.split("-");
  if (!year || !m) return month;
  const date = new Date(Number(year), Number(m) - 1, 1);
  return date.toLocaleDateString("en-GB", { month: "long", year: "numeric" });
}
