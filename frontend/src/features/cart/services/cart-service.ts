import apiClient from "@/core/api/client";
import { CART } from "@/core/api/endpoints";
import type { Cart, CheckoutResult } from "@/features/cart/types";
import type { RankedProduct } from "@/features/product-search/types";

export async function getCart(): Promise<Cart> {
  const { data } = await apiClient.get<Cart>(CART.BASE);
  return data;
}

export async function addItem(product: RankedProduct): Promise<Cart> {
  const p = product.product;
  const { data } = await apiClient.post<Cart>(CART.ITEMS, {
    externalProductId: p.external_product_id,
    title: p.title,
    price: p.price,
    currency: p.currency,
    merchantId: p.merchant_id,
    merchantName: p.merchant_name,
    merchantRating: p.merchant_rating,
    sourceType: p.source_type,
    sourceName: p.source_name,
    productUrl: p.product_url,
    imageUrl: p.image_urls?.[0] ?? "",
  });
  return data;
}

export async function removeItem(itemId: string): Promise<Cart> {
  const { data } = await apiClient.delete<Cart>(`${CART.ITEMS}/${itemId}`);
  return data;
}

export async function checkout(): Promise<CheckoutResult> {
  const { data } = await apiClient.post<CheckoutResult>(CART.CHECKOUT);
  return data;
}
