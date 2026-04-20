"use client";

import axios from "axios";
import apiClient from "@/core/api/client";
import { MOCK_BASKET_DRAFT } from "@/lib/mock/baskets";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";
const SUBMIT_TIMEOUT_MS = 15_000;

export type SubmitIntentSuccess = { kind: "success"; basket: Basket };
export type SubmitIntentClothingRequired = { kind: "clothing-required" };
export type SubmitIntentError = {
  kind: "error";
  isTimeout: boolean;
  message: string;
};
export type SubmitIntentResult =
  | SubmitIntentSuccess
  | SubmitIntentClothingRequired
  | SubmitIntentError;

export async function submitBasketIntent(
  intentText: string,
): Promise<SubmitIntentResult> {
  if (USE_MOCKS) {
    return {
      kind: "success",
      basket: { ...MOCK_BASKET_DRAFT, intentText },
    };
  }
  try {
    const response = await apiClient.post<Basket>(
      "/api/basket-intent/submit",
      { intentText },
      { timeout: SUBMIT_TIMEOUT_MS },
    );
    return { kind: "success", basket: response.data };
  } catch (err: unknown) {
    if (axios.isAxiosError(err)) {
      if (err.code === "ECONNABORTED" || err.code === "ERR_CANCELED") {
        return {
          kind: "error",
          isTimeout: true,
          message: "Taking longer than expected. Try again?",
        };
      }
      if (err.response?.status === 428) {
        return { kind: "clothing-required" };
      }
      return {
        kind: "error",
        isTimeout: false,
        message:
          (err.response?.data as { message?: string } | undefined)?.message ??
          err.message,
      };
    }
    return {
      kind: "error",
      isTimeout: false,
      message: err instanceof Error ? err.message : "Unknown error",
    };
  }
}

export type ClothingProfilePayload = {
  topSize: string;
  bottomSize: string;
  shoeSize: string;
  fit: string;
};

export async function saveClothingProfile(
  profile: ClothingProfilePayload,
): Promise<{ ok: boolean; message?: string }> {
  if (USE_MOCKS) return { ok: true };
  try {
    await apiClient.put("/api/preferences/clothing-profile", profile, {
      timeout: 10_000,
    });
    return { ok: true };
  } catch (err: unknown) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Save failed",
    };
  }
}

/**
 * Sprint F35.2: fashion-ish keyword heuristic. Fires the proactive size
 * prompt before the server-side 428 gate. Size-dependent fashion
 * subcategories only — accessories/bags/hats are intentionally excluded so
 * we don't prompt when size isn't needed.
 */
const SIZE_DEPENDENT_KEYWORDS = [
  "dress", "dresses", "skirt", "skirts",
  "top", "tops", "t-shirt", "tshirt", "tee", "tees", "shirt", "shirts",
  "blouse", "knitwear", "jumper", "jumpers", "sweater", "sweaters", "cardigan",
  "coat", "coats", "jacket", "jackets", "outerwear",
  "trousers", "jeans", "shorts", "leggings",
  "tracksuit", "sportswear", "activewear",
  "shoe", "shoes", "trainers", "sneakers", "boots", "heels", "sandals", "footwear",
  "lingerie", "underwear", "bra",
];

export function looksLikeSizeDependentFashion(text: string): boolean {
  const lower = text.toLowerCase();
  return SIZE_DEPENDENT_KEYWORDS.some((k) => new RegExp(`\\b${k}\\b`).test(lower));
}

export async function fetchClothingComplete(): Promise<boolean> {
  if (USE_MOCKS) return false;
  try {
    const response = await apiClient.get<{ complete: boolean }>(
      "/api/preferences/clothing-profile/complete",
      { timeout: 5_000 },
    );
    return Boolean(response.data.complete);
  } catch {
    // If we can't reach the check, assume complete — let the server-side 428
    // gate handle enforcement rather than spuriously prompting.
    return true;
  }
}

export type QuickSizePayload = {
  topSize: string;
  shoeSizeUk: string;
};

export async function saveQuickSize(
  payload: QuickSizePayload,
): Promise<{ ok: boolean; message?: string }> {
  if (USE_MOCKS) return { ok: true };
  try {
    const shoeNumeric = Number(payload.shoeSizeUk);
    await apiClient.put(
      "/api/preferences/taste-profile",
      {
        topSize: payload.topSize || null,
        shoeSizeUk: Number.isFinite(shoeNumeric) ? shoeNumeric : null,
      },
      { timeout: 10_000 },
    );
    return { ok: true };
  } catch (err: unknown) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Save failed",
    };
  }
}
