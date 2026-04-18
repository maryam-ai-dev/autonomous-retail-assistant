"use client";

import { useState } from "react";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import { useBudgetSummary } from "@/lib/api/useBudgetSummary";
import type { components } from "@/types/api.generated";

type BudgetSummary = components["schemas"]["BudgetSummaryDto"];

function currentMonth(): string {
  const d = new Date();
  const m = `${d.getMonth() + 1}`.padStart(2, "0");
  return `${d.getFullYear()}-${m}`;
}

export default function BudgetPage() {
  const [month] = useState<string>(currentMonth());
  const { data, isLoading, error } = useBudgetSummary(month);

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
        <span className="text-xs" style={{ color: "var(--muted)" }}>
          {formatMonth(month)}
        </span>
      </header>

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
    </div>
  );
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
