"use client";

import {
  MOCK_BASKET_APPROVED,
  MOCK_BASKET_MIXED_DIETARY,
} from "@/lib/mock/baskets";
import type { components } from "@/types/api.generated";
import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type Basket = components["schemas"]["BasketDto"];

function mockFor(month: string): Basket[] {
  const now = new Date();
  const current = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  if (month > current) return [];
  return [MOCK_BASKET_APPROVED, MOCK_BASKET_MIXED_DIETARY];
}

export function useBasketHistory(month: string): QueryState<Basket[]> {
  return useApiQuery<Basket[]>({
    key: `basket-history:${month}`,
    fetcher: () => apiGet<Basket[]>("/api/baskets", { month }),
    mockData: mockFor(month),
    enabled: Boolean(month),
  });
}
