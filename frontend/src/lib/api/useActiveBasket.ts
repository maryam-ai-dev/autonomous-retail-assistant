"use client";

import type { components } from "@/types/api.generated";
import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type Basket = components["schemas"]["BasketDto"];

export function useActiveBasket(): QueryState<Basket | null> {
  return useApiQuery<Basket | null>({
    key: "active-basket",
    fetcher: () =>
      apiGet<Basket | null>("/api/baskets/active").catch(() => null),
    mockData: null,
  });
}
