import apiClient from "@/core/api/client";
import { PREFERENCES } from "@/core/api/endpoints";

export interface PreferencesData {
  userId: string;
  budgetCap: number | null;
  preferredBrands: string[];
  blockedBrands: string[];
  blockedCategories: string[];
  allowSubstitutions: boolean;
  approvalThreshold: number | null;
  maxSubstitutionPriceDelta: number | null;
}

export async function getPreferences(): Promise<PreferencesData> {
  const { data } = await apiClient.get<PreferencesData>(PREFERENCES.BASE);
  return data;
}

export async function updatePreferences(
  updates: Partial<Omit<PreferencesData, "userId">>
): Promise<PreferencesData> {
  const { data } = await apiClient.put<PreferencesData>(
    PREFERENCES.BASE,
    updates
  );
  return data;
}
