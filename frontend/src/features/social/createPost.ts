"use client";

import apiClient from "@/core/api/client";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type Post = components["schemas"]["PostDto"];
type ReactionType = components["schemas"]["PostDto"]["reactions"][number]["type"];
type RetailerKey = components["schemas"]["PostDto"]["product"] extends infer P
  ? P extends { retailer: infer R }
    ? R
    : never
  : never;

export type CreateProductPostPayload = {
  caption: string;
  reactionType: ReactionType;
  product: {
    retailer: RetailerKey;
    name: string;
    brand?: string | null;
    price: number;
  };
};

export async function createProductPost(
  payload: CreateProductPostPayload,
): Promise<{ ok: true; post: Post } | { ok: false; message: string }> {
  if (USE_MOCKS) {
    return {
      ok: true,
      post: buildLocalPost(payload),
    };
  }
  try {
    const response = await apiClient.post<Post>(
      "/api/social/posts",
      {
        type: "PRODUCT",
        caption: payload.caption,
        reactionType: payload.reactionType,
        product: payload.product,
      },
      { timeout: 10_000 },
    );
    return { ok: true, post: response.data };
  } catch (err) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Could not post",
    };
  }
}

export function buildLocalPost(payload: CreateProductPostPayload): Post {
  return {
    id: `local-${Date.now()}`,
    type: "PRODUCT",
    authorHandle: "you",
    authorDisplayName: "You",
    caption: payload.caption,
    product: {
      retailer: payload.product.retailer,
      name: payload.product.name,
      brand: payload.product.brand ?? null,
      price: payload.product.price,
      imageUrl: null,
      dietaryTags: [],
    },
    basket: null,
    reactions: [{ type: payload.reactionType, count: 1 }],
    commentCount: 0,
    createdAt: new Date().toISOString(),
  };
}
