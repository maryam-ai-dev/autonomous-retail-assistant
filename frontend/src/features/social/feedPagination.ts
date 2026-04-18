"use client";

import apiClient from "@/core/api/client";
import { MOCK_FEED_PAGE } from "@/lib/mock/feed";
import type { components } from "@/types/api.generated";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

type FeedPage = components["schemas"]["FeedPageDto"];

export async function fetchFeedPage(cursor?: string): Promise<FeedPage> {
  if (USE_MOCKS) {
    return cursor
      ? { posts: [], nextCursor: null, hasMore: false }
      : MOCK_FEED_PAGE;
  }
  const response = await apiClient.get<FeedPage>(
    "/api/social/posts/feed",
    {
      params: cursor ? { cursor } : undefined,
      timeout: 10_000,
    },
  );
  return response.data;
}
