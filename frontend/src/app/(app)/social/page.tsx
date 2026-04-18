"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Button } from "@/shared/ui/Button";
import { PostCard } from "@/features/social/PostCard";
import { CreatePostSheet } from "@/features/social/CreatePostSheet";
import {
  buildLocalPost,
  createProductPost,
  type CreateProductPostPayload,
} from "@/features/social/createPost";
import { fetchFeedPage } from "@/features/social/feedPagination";
import { StoriesRow } from "@/features/social/StoriesRow";
import type { components } from "@/types/api.generated";

type Post = components["schemas"]["PostDto"];

export default function SocialPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [cursor, setCursor] = useState<string | undefined>(undefined);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(true);
  const [loadingMore, setLoadingMore] = useState<boolean>(false);
  const [initialError, setInitialError] = useState<string | null>(null);
  const [paginationError, setPaginationError] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const loadPage = useCallback(
    async (nextCursor?: string) => {
      const isFirst = nextCursor === undefined;
      if (isFirst) {
        setLoading(true);
        setInitialError(null);
      } else {
        setLoadingMore(true);
        setPaginationError(null);
      }
      try {
        const page = await fetchFeedPage(nextCursor);
        setPosts((prev) => dedupe(prev, page.posts));
        setCursor(page.nextCursor ?? undefined);
        setHasMore(Boolean(page.hasMore));
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "Couldn't load posts";
        if (isFirst) setInitialError(message);
        else setPaginationError(message);
      } finally {
        if (isFirst) setLoading(false);
        else setLoadingMore(false);
      }
    },
    [],
  );

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadPage(undefined);
  }, [loadPage]);

  useEffect(() => {
    if (!hasMore) return;
    const el = sentinelRef.current;
    if (!el || typeof IntersectionObserver === "undefined") return;
    const observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry?.isIntersecting && !loadingMore && cursor) {
          loadPage(cursor);
        }
      },
      { rootMargin: "200px" },
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [hasMore, loadingMore, cursor, loadPage]);

  async function handleCreate(payload: CreateProductPostPayload) {
    const optimistic = buildLocalPost(payload);
    setPosts((prev) => [optimistic, ...prev]);
    const result = await createProductPost(payload);
    if (result.ok) {
      setPosts((prev) =>
        prev.map((p) => (p.id === optimistic.id ? result.post : p)),
      );
    } else {
      setPosts((prev) => prev.filter((p) => p.id !== optimistic.id));
      setToast(result.message);
      throw new Error(result.message);
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1
          className="text-3xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          From your network
        </h1>
        <Button
          variant="primary"
          onClick={() => setCreateOpen(true)}
          aria-label="Create a new post"
        >
          + Post
        </Button>
      </div>

      <StoriesRow posts={posts} />

      {loading && posts.length === 0 ? (
        <FeedSkeleton />
      ) : initialError ? (
        <FeedError onRetry={() => loadPage(undefined)} />
      ) : posts.length === 0 ? (
        <EmptyFeed />
      ) : (
        <>
          <ul className="flex flex-col gap-4">
            {posts.map((post) => (
              <li key={post.id}>
                <PostCard post={post} />
              </li>
            ))}
          </ul>
          {hasMore ? (
            <div
              ref={sentinelRef}
              className="flex items-center justify-center py-4"
            >
              {loadingMore ? (
                <span
                  className="text-xs"
                  style={{ color: "var(--muted)" }}
                >
                  Loading more…
                </span>
              ) : null}
            </div>
          ) : null}
        </>
      )}

      {paginationError ? (
        <div
          role="alert"
          className="fixed inset-x-0 bottom-20 z-40 mx-auto flex max-w-md items-center justify-between rounded-full px-4 py-2 text-xs"
          style={{
            background: "var(--amber-light)",
            color: "#6B2A11",
            border: "1px solid var(--amber)",
          }}
        >
          <span>{paginationError}</span>
          <button
            type="button"
            onClick={() => loadPage(cursor)}
            className="ml-2 font-semibold underline"
            style={{ color: "#6B2A11" }}
          >
            Retry
          </button>
        </div>
      ) : null}

      <CreatePostSheet
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSubmit={handleCreate}
      />

      {toast ? (
        <div
          role="status"
          aria-live="polite"
          className="fixed inset-x-0 bottom-20 z-50 mx-auto max-w-md rounded-full px-4 py-2 text-center text-xs"
          style={{
            background: "var(--amber-light)",
            color: "#6B2A11",
            border: "1px solid var(--amber)",
          }}
          onClick={() => setToast(null)}
        >
          {toast}
        </div>
      ) : null}
    </div>
  );
}

function dedupe(prev: Post[], incoming: Post[]): Post[] {
  const seen = new Set(prev.map((p) => p.id));
  const additions = incoming.filter((p) => !seen.has(p.id));
  return [...prev, ...additions];
}

function FeedSkeleton() {
  return (
    <div className="flex flex-col gap-4">
      {[0, 1].map((idx) => (
        <div
          key={idx}
          className="h-36 rounded-2xl"
          style={{ background: "var(--oat)" }}
        />
      ))}
    </div>
  );
}

function FeedError({ onRetry }: { onRetry: () => void }) {
  return (
    <div
      role="alert"
      className="flex flex-col gap-3 rounded-2xl p-4"
      style={{
        background: "var(--amber-light)",
        border: "1px solid var(--amber)",
        color: "#6B2A11",
      }}
    >
      <p className="text-sm font-medium">
        Couldn&apos;t load the feed — try again?
      </p>
      <Button variant="secondary" onClick={onRetry}>
        Retry
      </Button>
    </div>
  );
}

function EmptyFeed() {
  return (
    <p
      className="py-10 text-center text-sm italic"
      style={{ color: "var(--muted)" }}
    >
      No posts yet — be the first to share a find.
    </p>
  );
}
