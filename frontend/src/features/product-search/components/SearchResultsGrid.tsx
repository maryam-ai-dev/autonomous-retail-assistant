"use client";

import type { RankedProduct } from "@/features/product-search/types";
import ProductCard from "./ProductCard";

interface SearchResultsGridProps {
  rankedProducts: RankedProduct[];
  loading: boolean;
  onAddToCart: (product: RankedProduct) => void;
  onViewDetails: (product: RankedProduct) => void;
}

function LoadingSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      {Array.from({ length: 4 }).map((_, i) => (
        <div
          key={i}
          className="animate-pulse rounded-lg border border-gray-200 bg-white p-4"
        >
          <div className="mb-3 aspect-square rounded bg-gray-200" />
          <div className="mb-2 h-4 w-3/4 rounded bg-gray-200" />
          <div className="mb-2 h-6 w-1/3 rounded bg-gray-200" />
          <div className="mb-3 h-3 w-1/2 rounded bg-gray-200" />
          <div className="flex gap-2">
            <div className="h-8 flex-1 rounded bg-gray-200" />
            <div className="h-8 flex-1 rounded bg-gray-200" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function SearchResultsGrid({
  rankedProducts,
  loading,
  onAddToCart,
  onViewDetails,
}: SearchResultsGridProps) {
  if (loading) {
    return <LoadingSkeleton />;
  }

  if (rankedProducts.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-gray-500">
        <p className="text-lg font-medium">No results found</p>
        <p className="text-sm">Try a different search query</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      {rankedProducts.map((rp) => (
        <ProductCard
          key={rp.product.external_product_id || rp.rank}
          rankedProduct={rp}
          onAddToCart={onAddToCart}
          onViewDetails={onViewDetails}
        />
      ))}
    </div>
  );
}
