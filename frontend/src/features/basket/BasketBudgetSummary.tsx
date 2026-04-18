import { BudgetBar } from "@/shared/ui/BudgetBar";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];
type BasketItem = components["schemas"]["BasketItemDto"];

type BasketBudgetSummaryProps = {
  basket: Basket;
};

function itemHasValidPrice(item: BasketItem): boolean {
  return typeof item.price === "number" && Number.isFinite(item.price);
}

export function computeBasketTotals(basket: Basket): {
  validTotal: number;
  excludedCount: number;
  budget: number;
  remaining: number;
  overBudget: boolean;
} {
  let validTotal = 0;
  let excludedCount = 0;
  for (const item of basket.items ?? []) {
    if (itemHasValidPrice(item)) {
      validTotal += item.price * (item.quantity || 0);
    } else {
      excludedCount += 1;
    }
  }
  const budget = typeof basket.budget === "number" ? basket.budget : 0;
  const remaining = budget - validTotal;
  return {
    validTotal,
    excludedCount,
    budget,
    remaining,
    overBudget: validTotal > budget,
  };
}

function formatMoney(value: number): string {
  return `£${Math.abs(value).toFixed(2)}`;
}

export function BasketBudgetSummary({ basket }: BasketBudgetSummaryProps) {
  const { validTotal, excludedCount, budget, remaining, overBudget } =
    computeBasketTotals(basket);
  const warnings = [
    ...(basket.normalizationWarnings ?? []),
    ...(excludedCount > 0
      ? [`${excludedCount} item${excludedCount > 1 ? "s" : ""} excluded — missing price`]
      : []),
  ];
  const hasWarnings = warnings.length > 0;

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <span
          className="text-sm font-semibold"
          style={{
            color: "var(--aubergine)",
            fontVariantNumeric: "tabular-nums",
          }}
        >
          {formatMoney(validTotal)} of {formatMoney(budget)}
        </span>
        <div className="flex items-center gap-2">
          {hasWarnings ? (
            <span
              role="img"
              aria-label="Basket warning"
              title={warnings.join(". ")}
              className="inline-flex h-5 w-5 items-center justify-center rounded-full text-[11px] font-bold"
              style={{
                background: "var(--amber-light)",
                color: "#6B2A11",
              }}
            >
              !
            </span>
          ) : null}
          <span
            className="text-sm font-medium"
            style={{
              color: overBudget ? "var(--amber)" : "var(--sage)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            {overBudget
              ? `${formatMoney(remaining)} over`
              : `${formatMoney(remaining)} left`}
          </span>
        </div>
      </div>
      <BudgetBar
        value={validTotal}
        max={budget}
        ariaLabel="Basket budget"
      />
      {hasWarnings ? (
        <ul
          className="flex flex-col gap-1 text-xs"
          style={{ color: "#6B2A11" }}
        >
          {warnings.map((warning, idx) => (
            <li key={idx}>⚠ {warning}</li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
