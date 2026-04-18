"use client";

import axios from "axios";
import apiClient from "@/core/api/client";
import { MOCK_SHARED_BASKET } from "@/lib/mock/shared-basket";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type SharedBasket = components["schemas"]["SharedBasketDto"];

export type SharedBasketResult =
  | { kind: "ok"; data: SharedBasket }
  | { kind: "not-found" }
  | { kind: "error"; message: string };

export async function fetchSharedBasket(
  shareId: string,
): Promise<SharedBasketResult> {
  if (USE_MOCKS) {
    return {
      kind: "ok",
      data: { ...MOCK_SHARED_BASKET, shareId },
    };
  }
  try {
    const response = await apiClient.get<SharedBasket>(
      `/api/baskets/shared/${encodeURIComponent(shareId)}`,
      { timeout: 10_000 },
    );
    return { kind: "ok", data: response.data };
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.status === 404) {
      return { kind: "not-found" };
    }
    return {
      kind: "error",
      message: err instanceof Error ? err.message : "Couldn't load basket",
    };
  }
}

export async function forkSharedBasket(
  shareId: string,
): Promise<{ ok: true; basketId: string } | { ok: false; message: string }> {
  if (USE_MOCKS) {
    return { ok: true, basketId: `bk-forked-${Date.now()}` };
  }
  try {
    const response = await apiClient.post<{ basketId: string }>(
      `/api/baskets/shared/${encodeURIComponent(shareId)}/fork`,
      {},
      { timeout: 15_000 },
    );
    return { ok: true, basketId: response.data.basketId };
  } catch (err) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Fork failed",
    };
  }
}
