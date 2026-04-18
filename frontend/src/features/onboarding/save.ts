"use client";

import apiClient from "@/core/api/client";
import type { OnboardingAnswers } from "./types";

const TASTE_PROFILE_ENDPOINT = "/api/preferences/taste-profile";
const PENDING_STORAGE_KEY = "aisleon_taste_profile_pending";

export type SaveResult = "saved" | "fallback";

export async function saveTasteProfile(
  answers: OnboardingAnswers,
): Promise<SaveResult> {
  const payload = {
    priority: answers.priority,
    household: answers.household,
    dietary: answers.dietary,
    budgetQuality: answers.budgetQuality,
    brands: answers.brands,
    values: answers.values,
  };
  try {
    await apiClient.put(TASTE_PROFILE_ENDPOINT, payload, { timeout: 10_000 });
    return "saved";
  } catch {
    try {
      if (typeof window !== "undefined") {
        window.localStorage.setItem(
          PENDING_STORAGE_KEY,
          JSON.stringify(answers),
        );
      }
    } catch {
      // localStorage also unavailable — answers live only in memory
    }
    return "fallback";
  }
}
