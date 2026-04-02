import apiClient from "@/core/api/client";
import { DISCOVERY } from "@/core/api/endpoints";
import type { SearchResponse } from "@/features/product-search/types";

export async function searchProducts(
  query: string,
  maxResults: number = 10
): Promise<SearchResponse> {
  const { data } = await apiClient.post<SearchResponse>(DISCOVERY.SEARCH, {
    query,
    maxResults,
  });
  return data;
}
