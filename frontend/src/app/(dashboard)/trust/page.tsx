"use client";

import { useEffect, useState } from "react";
import { useRequireAuth } from "@/core/auth/guards";
import apiClient from "@/core/api/client";
import { PREFERENCES, MERCHANTS } from "@/core/api/endpoints";
import ApprovedMerchants, {
  type Merchant,
} from "@/features/trust-center/components/ApprovedMerchants";
import BudgetRules from "@/features/trust-center/components/BudgetRules";
import SubstitutionRules from "@/features/trust-center/components/SubstitutionRules";
import BlockedBrands from "@/features/trust-center/components/BlockedBrands";
import type { PreferencesData } from "@/features/retail-preferences/services/preferences-service";
import { updatePreferences } from "@/features/retail-preferences/services/preferences-service";

export default function TrustPage() {
  useRequireAuth();

  const [prefs, setPrefs] = useState<PreferencesData | null>(null);
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      apiClient.get<PreferencesData>(PREFERENCES.BASE),
      apiClient.get<Merchant[]>(MERCHANTS.BASE),
    ])
      .then(([prefsRes, merchantsRes]) => {
        setPrefs(prefsRes.data);
        setMerchants(merchantsRes.data);
      })
      .finally(() => setLoading(false));
  }, []);

  async function handleMerchantToggle(id: string, approve: boolean) {
    const action = approve ? "approve" : "block";
    await apiClient.put(`${MERCHANTS.BASE}/${id}/${action}`);
    const { data } = await apiClient.get<Merchant[]>(MERCHANTS.BASE);
    setMerchants(data);
  }

  async function handleBudgetSave(
    budgetCap: number | null,
    approvalThreshold: number | null
  ) {
    const updated = await updatePreferences({ budgetCap, approvalThreshold });
    setPrefs(updated);
  }

  async function handleSubstitutionSave(
    allow: boolean,
    maxDelta: number | null
  ) {
    const updated = await updatePreferences({
      allowSubstitutions: allow,
      maxSubstitutionPriceDelta: maxDelta,
    });
    setPrefs(updated);
  }

  async function handleBlockedBrandsSave(brands: string[]) {
    const updated = await updatePreferences({ blockedBrands: brands });
    setPrefs(updated);
  }

  if (loading || !prefs) {
    return (
      <div className="py-8 text-center text-gray-500">
        Loading trust settings...
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Trust Center</h1>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <BudgetRules
          budgetCap={prefs.budgetCap}
          approvalThreshold={prefs.approvalThreshold}
          onSave={handleBudgetSave}
        />
        <SubstitutionRules
          allowSubstitutions={prefs.allowSubstitutions}
          maxDelta={prefs.maxSubstitutionPriceDelta}
          onSave={handleSubstitutionSave}
        />
        <BlockedBrands
          brands={prefs.blockedBrands}
          onSave={handleBlockedBrandsSave}
        />
        <ApprovedMerchants
          merchants={merchants}
          onToggle={handleMerchantToggle}
        />
      </div>
    </div>
  );
}
