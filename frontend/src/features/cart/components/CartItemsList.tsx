"use client";

import type { CartItem } from "@/features/cart/types";

interface CartItemsListProps {
  items: CartItem[];
  onRemove: (itemId: string) => void;
}

export default function CartItemsList({ items, onRemove }: CartItemsListProps) {
  if (items.length === 0) {
    return (
      <div className="py-8 text-center text-gray-500">
        <p className="text-lg font-medium">Your cart is empty</p>
        <p className="text-sm">Search for products to add them here</p>
      </div>
    );
  }

  return (
    <ul className="divide-y divide-gray-200">
      {items.map((item) => (
        <li key={item.id} className="flex items-center gap-4 py-4">
          <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded bg-gray-100">
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.title}
                className="h-full w-full object-contain"
              />
            ) : (
              <span className="text-xs text-gray-400">No img</span>
            )}
          </div>

          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <p className="truncate text-sm font-medium">{item.title}</p>
              {item.substitution && (
                <span className="shrink-0 rounded bg-purple-100 px-2 py-0.5 text-xs font-medium text-purple-700">
                  Substitution
                </span>
              )}
            </div>
            <p className="text-xs text-gray-500">{item.merchantName}</p>
          </div>

          <p className="shrink-0 text-sm font-bold">
            {item.currency || "USD"} {item.price.toFixed(2)}
          </p>

          <button
            onClick={() => onRemove(item.id)}
            className="shrink-0 rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-red-500"
          >
            &times;
          </button>
        </li>
      ))}
    </ul>
  );
}
