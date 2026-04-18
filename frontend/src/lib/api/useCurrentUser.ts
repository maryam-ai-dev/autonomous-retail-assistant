"use client";

import type { QueryState } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type CurrentUser = {
  displayName: string | null;
  handle: string | null;
};

const MOCK_USER: CurrentUser = { displayName: "Maryam", handle: "maryam" };

export function useCurrentUser(): QueryState<CurrentUser> {
  return useApiQuery<CurrentUser>({
    key: "current-user",
    fetcher: () => apiGet<CurrentUser>("/api/profile/me"),
    mockData: MOCK_USER,
  });
}
