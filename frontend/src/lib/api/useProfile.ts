"use client";

import { MOCK_PROFILE } from "@/lib/mock/profile";
import type { components } from "@/types/api.generated";
import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type Profile = components["schemas"]["ProfileDto"];

export function useProfile(handle: string): QueryState<Profile> {
  return useApiQuery<Profile>({
    key: `profile:${handle}`,
    fetcher: () =>
      apiGet<Profile>(`/api/social/profiles/${encodeURIComponent(handle)}`),
    mockData: { ...MOCK_PROFILE, handle: handle || MOCK_PROFILE.handle },
    enabled: Boolean(handle),
  });
}
