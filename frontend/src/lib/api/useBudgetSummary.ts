"use client";

import { MOCK_BUDGET_SUMMARY } from "@/lib/mock/budget";
import type { components } from "@/types/api.generated";
import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type BudgetSummary = components["schemas"]["BudgetSummaryDto"];

export function useBudgetSummary(month: string): QueryState<BudgetSummary> {
  return useApiQuery<BudgetSummary>({
    key: `budget:${month}`,
    fetcher: () =>
      apiGet<BudgetSummary>("/api/budget/summary", { month }),
    mockData: { ...MOCK_BUDGET_SUMMARY, month: month || MOCK_BUDGET_SUMMARY.month },
    enabled: Boolean(month),
  });
}
