"use client";

import type { RankedProduct } from "@/features/product-search/types";

interface ProductCardProps {
  rankedProduct: RankedProduct;
  onAddToCart: (product: RankedProduct) => void;
  onViewDetails: (product: RankedProduct) => void;
}

function getConfidenceBadge(score: number): {
  label: string;
  className: string;
} {
  if (score >= 0.75) {
    return { label: "High", className: "bg-green-100 text-green-700" };
  }
  if (score >= 0.45) {
    return { label: "Medium", className: "bg-yellow-100 text-yellow-700" };
  }
  return { label: "Low", className: "bg-red-100 text-red-700" };
}

export default function ProductCard({
  rankedProduct,
  onAddToCart,
  onViewDetails,
}: ProductCardProps) {
  const { product, explanation, trust_score } = rankedProduct;
  const badge = getConfidenceBadge(trust_score.overall_trust_score);
  const imageUrl =
    product.image_urls?.length > 0 ? product.image_urls[0] : null;
  const topReason = explanation.top_reasons?.[0] ?? "";

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="mb-3 flex aspect-square items-center justify-center overflow-hidden rounded bg-gray-100">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={product.title}
            className="h-full w-full object-contain"
          />
        ) : (
          <span className="text-sm text-gray-400">No image</span>
        )}
      </div>

      <div className="mb-2 flex items-start justify-between gap-2">
        <h3 className="line-clamp-2 text-sm font-medium">{product.title}</h3>
        <span
          className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-medium ${badge.className}`}
        >
          {badge.label}
        </span>
      </div>

      <div className="mb-2 flex items-center justify-between">
        <span className="text-lg font-bold">
          {product.currency} {product.price.toFixed(2)}
        </span>
        <span className="rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-600">
          {product.source_name.toUpperCase()}
        </span>
      </div>

      <p className="mb-1 text-xs text-gray-500">{product.merchant_name}</p>

      {topReason && (
        <p className="mb-3 text-xs text-blue-600">{topReason}</p>
      )}

      <div className="flex gap-2">
        <button
          onClick={() => onAddToCart(rankedProduct)}
          className="flex-1 rounded bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700"
        >
          Add to Cart
        </button>
        <button
          onClick={() => onViewDetails(rankedProduct)}
          className="flex-1 rounded border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50"
        >
          View Details
        </button>
      </div>
    </div>
  );
}
