"use client";

import apiClient from "@/core/api/client";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type ReactionType = components["schemas"]["PostDto"]["reactions"][number]["type"];

export async function toggleReaction(
  postId: string,
  type: ReactionType,
): Promise<{ ok: boolean }> {
  if (USE_MOCKS) return { ok: true };
  try {
    await apiClient.post(
      `/api/social/posts/${encodeURIComponent(postId)}/react`,
      { type },
      { timeout: 8_000 },
    );
    return { ok: true };
  } catch {
    return { ok: false };
  }
}
