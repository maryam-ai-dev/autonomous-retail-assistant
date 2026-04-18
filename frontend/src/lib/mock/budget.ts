import type { components } from "@/types/api.generated";

type BudgetSummary = components["schemas"]["BudgetSummaryDto"];

export const MOCK_BUDGET_SUMMARY: BudgetSummary = {
  month: "2026-04",
  spent: 218.4,
  savedVsFullPrice: 23.1,
  basketsCount: 4,
  avgBasket: 54.6,
  byRetailer: {
    TESCO: 95.2,
    SAINSBURYS: 72.3,
    BOOTS: 30.9,
    ARGOS: 20,
  },
  insights: [
    "You saved £12 at Boots this month by switching to Clubcard prices.",
    "Your average basket is £5 under budget — steady trend.",
  ],
};
