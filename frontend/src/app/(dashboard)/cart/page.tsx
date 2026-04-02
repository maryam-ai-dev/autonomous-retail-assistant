"use client";

import { useEffect, useState } from "react";
import { useRequireAuth } from "@/core/auth/guards";
import CartItemsList from "@/features/cart/components/CartItemsList";
import CartSummary from "@/features/cart/components/CartSummary";
import PolicyWarningsPanel from "@/features/cart/components/PolicyWarningsPanel";
import ApprovalTriggerPanel from "@/features/cart/components/ApprovalTriggerPanel";
import { getCart, removeItem, checkout } from "@/features/cart/services/cart-service";
import type { Cart, CheckoutResult } from "@/features/cart/types";

export default function CartPage() {
  useRequireAuth();

  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [checking, setChecking] = useState(false);
  const [error, setError] = useState("");
  const [checkoutResult, setCheckoutResult] = useState<CheckoutResult | null>(null);

  useEffect(() => {
    loadCart();
  }, []);

  async function loadCart() {
    try {
      const data = await getCart();
      setCart(data);
    } catch {
      setError("Failed to load cart");
    } finally {
      setLoading(false);
    }
  }

  async function handleRemove(itemId: string) {
    try {
      const updated = await removeItem(itemId);
      setCart(updated);
    } catch {
      setError("Failed to remove item");
    }
  }

  async function handleCheckout() {
    setError("");
    setCheckoutResult(null);
    setChecking(true);

    try {
      const result = await checkout();
      setCheckoutResult(result);
      if (result.outcome === "CHECKED_OUT") {
        await loadCart();
      }
    } catch {
      setError("Checkout failed. Please try again.");
    } finally {
      setChecking(false);
    }
  }

  if (loading) {
    return <div className="py-8 text-center text-gray-500">Loading cart...</div>;
  }

  const items = cart?.items ?? [];
  const totalAmount = cart?.totalAmount ?? items.reduce((sum, i) => sum + i.price, 0);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Cart</h1>

      {error && (
        <div className="rounded bg-red-50 p-3 text-sm text-red-600">{error}</div>
      )}

      {checkoutResult && <ApprovalTriggerPanel result={checkoutResult} />}

      {checkoutResult?.outcome === "APPROVAL_REQUIRED" &&
        checkoutResult.reasons && (
          <PolicyWarningsPanel warnings={checkoutResult.reasons} />
        )}

      <CartItemsList items={items} onRemove={handleRemove} />

      {items.length > 0 && (
        <CartSummary
          totalAmount={totalAmount}
          itemCount={items.length}
          onCheckout={handleCheckout}
          checking={checking}
        />
      )}
    </div>
  );
}
