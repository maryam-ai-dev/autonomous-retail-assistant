"use client";

interface CartSummaryProps {
  totalAmount: number;
  itemCount: number;
  onCheckout: () => void;
  checking: boolean;
}

export default function CartSummary({
  totalAmount,
  itemCount,
  onCheckout,
  checking,
}: CartSummaryProps) {
  return (
    <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
      <div className="mb-3 flex items-center justify-between">
        <span className="text-sm text-gray-600">
          {itemCount} {itemCount === 1 ? "item" : "items"}
        </span>
        <span className="text-lg font-bold">USD {totalAmount.toFixed(2)}</span>
      </div>
      <button
        onClick={onCheckout}
        disabled={checking || itemCount === 0}
        className="w-full rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
      >
        {checking ? "Processing..." : "Checkout"}
      </button>
    </div>
  );
}
