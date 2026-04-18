"use client";

import { MOCK_FEED_PAGE } from "@/lib/mock/feed";
import type { components } from "@/types/api.generated";
import type { QueryError } from "@/types/app";
import { apiGet, useApiQuery } from "./useApiQuery";

type FeedPage = components["schemas"]["FeedPageDto"];

type UseFeedResult = {
  posts: FeedPage["posts"];
  nextCursor: FeedPage["nextCursor"];
  hasMore: FeedPage["hasMore"];
  isLoading: boolean;
  error: QueryError | null;
};

export function useFeed(cursor?: string): UseFeedResult {
  const key = cursor ? `feed:${cursor}` : "feed:initial";
  const state = useApiQuery<FeedPage>({
    key,
    fetcher: () =>
      apiGet<FeedPage>("/api/social/posts/feed", cursor ? { cursor } : undefined),
    mockData: MOCK_FEED_PAGE,
  });
  return {
    posts: state.data?.posts ?? [],
    nextCursor: state.data?.nextCursor ?? null,
    hasMore: state.data?.hasMore ?? false,
    isLoading: state.isLoading,
    error: state.error,
  };
}
