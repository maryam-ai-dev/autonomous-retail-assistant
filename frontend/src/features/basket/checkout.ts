"use client";

import axios from "axios";
import apiClient from "@/core/api/client";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type Basket = components["schemas"]["BasketDto"];

export const RETAILER_FALLBACK_URLS: Record<string, string> = {
  TESCO: "https://www.tesco.com",
  SAINSBURYS: "https://www.sainsburys.co.uk",
  BOOTS: "https://www.boots.com",
  ARGOS: "https://www.argos.co.uk",
  ASDA: "https://www.asda.com",
  MORRISONS: "https://www.morrisons.com",
  OCADO: "https://www.ocado.com",
};

export type ApproveResult =
  | { ok: true; basket: Basket }
  | { ok: false; message: string; isUnresolvedFlags: boolean };

export async function approveBasket(basketId: string): Promise<ApproveResult> {
  if (USE_MOCKS) {
    return {
      ok: true,
      basket: {
        id: basketId,
        status: "APPROVED",
        intentText: "",
        budget: 0,
        totalCost: 0,
        items: [],
        retailersUsed: [],
        normalizationWarnings: [],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    };
  }
  try {
    const response = await apiClient.post<Basket>(
      `/api/baskets/${encodeURIComponent(basketId)}/approve`,
      {},
      { timeout: 10_000 },
    );
    return { ok: true, basket: response.data };
  } catch (err: unknown) {
    if (axios.isAxiosError(err) && err.response?.status === 409) {
      return {
        ok: false,
        message: "Some items still need attention before approving.",
        isUnresolvedFlags: true,
      };
    }
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Could not approve basket",
      isUnresolvedFlags: false,
    };
  }
}

export async function fetchCheckoutLinks(
  basketId: string,
): Promise<Record<string, string>> {
  if (USE_MOCKS) return {};
  try {
    const response = await apiClient.get<Record<string, string>>(
      `/api/baskets/${encodeURIComponent(basketId)}/checkout-links`,
      { timeout: 8_000 },
    );
    return response.data ?? {};
  } catch {
    return {};
  }
}

export async function shareBasket(
  basketId: string,
): Promise<{ shareUrl: string } | null> {
  if (USE_MOCKS) {
    return {
      shareUrl: `${getOrigin()}/basket/shared/mock-${basketId}`,
    };
  }
  try {
    const response = await apiClient.post<{ shareId: string; shareUrl: string }>(
      `/api/baskets/${encodeURIComponent(basketId)}/share`,
      {},
      { timeout: 8_000 },
    );
    return { shareUrl: response.data.shareUrl };
  } catch {
    return null;
  }
}

function getOrigin(): string {
  if (typeof window === "undefined") return "https://aisleon.example";
  return window.location.origin;
}

export function resolveCheckoutUrl(
  retailer: string,
  links: Record<string, string>,
): string {
  return links[retailer] ?? RETAILER_FALLBACK_URLS[retailer] ?? "/";
}
