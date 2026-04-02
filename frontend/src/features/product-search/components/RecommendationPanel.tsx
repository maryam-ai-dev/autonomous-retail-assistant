"use client";

import type { RankedProduct } from "@/features/product-search/types";

interface RecommendationPanelProps {
  rankedProduct: RankedProduct;
  onAddToCart: (product: RankedProduct) => void;
  onClose: () => void;
}

function ConfidenceMeter({ score }: { score: number }) {
  const pct = Math.round(score * 100);
  const color =
    pct >= 75 ? "bg-green-500" : pct >= 45 ? "bg-yellow-500" : "bg-red-500";

  return (
    <div>
      <div className="mb-1 flex items-center justify-between text-sm">
        <span className="font-medium">Confidence</span>
        <span className="font-bold">{pct}%</span>
      </div>
      <div className="h-2.5 w-full rounded-full bg-gray-200">
        <div
          className={`h-2.5 rounded-full ${color}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

function actionabilityLabel(actionability: string): string {
  switch (actionability) {
    case "safe_to_proceed":
      return "Safe to proceed — no approval needed";
    case "needs_approval":
      return "Approval will be required before checkout";
    case "needs_user_input":
      return "Needs your review before proceeding";
    case "block":
      return "This purchase would be blocked by policy";
    default:
      return actionability;
  }
}

export default function RecommendationPanel({
  rankedProduct,
  onAddToCart,
  onClose,
}: RecommendationPanelProps) {
  const { product, explanation, trust_score } = rankedProduct;
  const imageUrl =
    product.image_urls?.length > 0 ? product.image_urls[0] : null;

  return (
    <div className="fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col border-l border-gray-200 bg-white shadow-xl">
      <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
        <h2 className="text-lg font-bold">Product Details</h2>
        <button
          onClick={onClose}
          className="rounded p-1 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
        >
          &times;
        </button>
      </div>

      <div className="flex-1 overflow-auto px-6 py-4 space-y-5">
        {imageUrl && (
          <div className="flex items-center justify-center rounded bg-gray-100 p-4">
            <img
              src={imageUrl}
              alt={product.title}
              className="max-h-48 object-contain"
            />
          </div>
        )}

        <div>
          <h3 className="text-base font-semibold">{product.title}</h3>
          <p className="mt-1 text-2xl font-bold">
            {product.currency} {product.price.toFixed(2)}
          </p>
          <p className="mt-1 text-sm text-gray-500">
            Sold by {product.merchant_name}
          </p>
        </div>

        <ConfidenceMeter score={trust_score.overall_trust_score} />

        {explanation.top_reasons.length > 0 && (
          <div>
            <h4 className="mb-2 text-sm font-semibold">Why we recommend this</h4>
            <ul className="space-y-1">
              {explanation.top_reasons.map((reason, i) => (
                <li key={i} className="flex items-start gap-2 text-sm text-gray-700">
                  <span className="mt-0.5 text-green-500">&#10003;</span>
                  {reason}
                </li>
              ))}
            </ul>
          </div>
        )}

        {explanation.tradeoffs.length > 0 && (
          <div>
            <h4 className="mb-2 text-sm font-semibold">Things to consider</h4>
            <ul className="space-y-1">
              {explanation.tradeoffs.map((tradeoff, i) => (
                <li key={i} className="flex items-start gap-2 text-sm text-gray-500">
                  <span className="mt-0.5 text-yellow-500">&#9888;</span>
                  {tradeoff}
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="rounded bg-gray-50 p-3">
          <p className="text-sm text-gray-700">
            {actionabilityLabel(trust_score.actionability)}
          </p>
        </div>
      </div>

      <div className="border-t border-gray-200 px-6 py-4 flex gap-3">
        <button
          onClick={() => onAddToCart(rankedProduct)}
          className="flex-1 rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Add to Cart
        </button>
        <button
          onClick={onClose}
          className="flex-1 rounded border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Close
        </button>
      </div>
    </div>
  );
}
