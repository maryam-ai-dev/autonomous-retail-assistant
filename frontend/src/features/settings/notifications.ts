"use client";

import apiClient from "@/core/api/client";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

export type NotificationPreferences = {
  newFollower: boolean;
  basketComment: boolean;
  helpfulReaction: boolean;
  weeklyBudgetSummary: boolean;
};

export const DEFAULT_NOTIFICATIONS: NotificationPreferences = {
  newFollower: true,
  basketComment: true,
  helpfulReaction: true,
  weeklyBudgetSummary: true,
};

export async function saveNotifications(
  prefs: NotificationPreferences,
): Promise<{ ok: boolean }> {
  if (USE_MOCKS) return { ok: true };
  try {
    await apiClient.put("/api/preferences/notifications", prefs, {
      timeout: 10_000,
    });
    return { ok: true };
  } catch {
    return { ok: false };
  }
}
