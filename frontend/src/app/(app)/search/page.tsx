"use client";

import { useState } from "react";
import { useRequireAuth } from "@/core/auth/guards";
import SearchForm from "@/features/product-search/components/SearchForm";
import SearchResultsGrid from "@/features/product-search/components/SearchResultsGrid";
import RecommendationPanel from "@/features/product-search/components/RecommendationPanel";
import { searchProducts } from "@/features/product-search/services/search-service";
import { addItem } from "@/features/cart/services/cart-service";
import type { RankedProduct } from "@/features/product-search/types";

export default function SearchPage() {
  useRequireAuth();

  const [rankedProducts, setRankedProducts] = useState<RankedProduct[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedProduct, setSelectedProduct] = useState<RankedProduct | null>(
    null
  );
  const [toast, setToast] = useState("");

  async function handleSearch(query: string) {
    setError("");
    setLoading(true);
    setSelectedProduct(null);

    try {
      const response = await searchProducts(query);
      setRankedProducts(response.rankedProducts ?? []);
    } catch {
      setError("Search failed. Please try again.");
      setRankedProducts([]);
    } finally {
      setLoading(false);
    }
  }

  async function handleAddToCart(rp: RankedProduct) {
    await addItem(rp);
    setToast(`Added "${rp.product.title}" to cart (coming in Sprint 9)`);
    setTimeout(() => setToast(""), 3000);
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Product Search</h1>

      <SearchForm onSearch={handleSearch} loading={loading} />

      {error && (
        <div className="rounded bg-red-50 p-3 text-sm text-red-600">
          {error}
        </div>
      )}

      <SearchResultsGrid
        rankedProducts={rankedProducts}
        loading={loading}
        onAddToCart={handleAddToCart}
        onViewDetails={setSelectedProduct}
      />

      {selectedProduct && (
        <RecommendationPanel
          rankedProduct={selectedProduct}
          onAddToCart={handleAddToCart}
          onClose={() => setSelectedProduct(null)}
        />
      )}

      {toast && (
        <div className="fixed bottom-4 right-4 z-50 rounded bg-green-600 px-4 py-2 text-sm text-white shadow-lg">
          {toast}
        </div>
      )}
    </div>
  );
}
