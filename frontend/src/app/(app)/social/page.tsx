"use client";

import { useEffect, useState } from "react";
import { Button } from "@/shared/ui/Button";
import { PostCard } from "@/features/social/PostCard";
import { CreatePostSheet } from "@/features/social/CreatePostSheet";
import {
  buildLocalPost,
  createProductPost,
  type CreateProductPostPayload,
} from "@/features/social/createPost";
import { useFeed } from "@/lib/api/useFeed";
import type { components } from "@/types/api.generated";

type Post = components["schemas"]["PostDto"];

export default function SocialPage() {
  const feed = useFeed();
  const [posts, setPosts] = useState<Post[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    if (!feed.isLoading && !feed.error) {
      setPosts(feed.posts);
    }
  }, [feed.isLoading, feed.error, feed.posts]);

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

      {feed.isLoading && posts.length === 0 ? (
        <FeedSkeleton />
      ) : feed.error ? (
        <FeedError onRetry={() => window.location.reload()} />
      ) : posts.length === 0 ? (
        <EmptyFeed />
      ) : (
        <ul className="flex flex-col gap-4">
          {posts.map((post) => (
            <li key={post.id}>
              <PostCard post={post} />
            </li>
          ))}
        </ul>
      )}

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
          onAnimationEnd={() => setToast(null)}
        >
          {toast}
        </div>
      ) : null}
    </div>
  );
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
