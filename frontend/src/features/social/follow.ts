"use client";

import apiClient from "@/core/api/client";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

export async function setFollowState(
  handle: string,
  following: boolean,
): Promise<{ ok: boolean }> {
  if (USE_MOCKS) return { ok: true };
  try {
    if (following) {
      await apiClient.post(
        `/api/social/follows/${encodeURIComponent(handle)}`,
        {},
        { timeout: 8_000 },
      );
    } else {
      await apiClient.delete(
        `/api/social/follows/${encodeURIComponent(handle)}`,
        { timeout: 8_000 },
      );
    }
    return { ok: true };
  } catch {
    return { ok: false };
  }
}

export async function saveBasket(
  basketId: string,
): Promise<{ ok: boolean }> {
  if (USE_MOCKS) return { ok: true };
  try {
    await apiClient.post(
      "/api/baskets/saved",
      { basketId },
      { timeout: 8_000 },
    );
    return { ok: true };
  } catch {
    return { ok: false };
  }
}
