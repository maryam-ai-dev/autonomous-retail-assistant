"use client";

import {
  MOCK_BASKET_APPROVED,
  MOCK_BASKET_DRAFT,
  MOCK_BASKET_FLAGGED,
  MOCK_BASKET_MIXED_DIETARY,
} from "@/lib/mock/baskets";
import type { components } from "@/types/api.generated";
import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type Basket = components["schemas"]["BasketDto"];

function mockFor(id: string): Basket {
  if (id.includes("flag")) return MOCK_BASKET_FLAGGED;
  if (id.includes("approved")) return MOCK_BASKET_APPROVED;
  if (id.includes("mixed")) return MOCK_BASKET_MIXED_DIETARY;
  return MOCK_BASKET_DRAFT;
}

export function useBasket(id: string): QueryState<Basket> {
  return useApiQuery<Basket>({
    key: `basket:${id}`,
    fetcher: () => apiGet<Basket>(`/api/baskets/${encodeURIComponent(id)}`),
    mockData: mockFor(id),
    enabled: Boolean(id),
  });
}
