import type { RankedProduct } from "@/features/product-search/types";

/**
 * Stub — will be fully replaced in Sprint 9 with real backend calls.
 */
export async function addItem(product: RankedProduct): Promise<void> {
  console.log("Added to cart (stub):", product.product.title);
}
