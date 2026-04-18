"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Avatar } from "@/shared/ui/Avatar";
import { Button } from "@/shared/ui/Button";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import { PostCard } from "@/features/social/PostCard";
import { fetchFeedPage } from "@/features/social/feedPagination";
import {
  MAX_COMMENT_LENGTH,
  fetchComments,
  postComment,
  type CommentDto,
} from "@/features/social/comments";
import type { components } from "@/types/api.generated";

type Post = components["schemas"]["PostDto"];

export default function PostDetailPage() {
  const params = useParams<{ postId: string }>();
  const postId = params?.postId ?? "";
  const [post, setPost] = useState<Post | null>(null);
  const [loadingPost, setLoadingPost] = useState(true);
  const [comments, setComments] = useState<CommentDto[]>([]);
  const [loadingComments, setLoadingComments] = useState(true);
  const [draft, setDraft] = useState("");
  const [posting, setPosting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!postId) return;
    let cancelled = false;
    (async () => {
      setLoadingPost(true);
      const page = await fetchFeedPage();
      const found = page.posts.find((p) => p.id === postId) ?? null;
      if (!cancelled) {
        setPost(found);
        setLoadingPost(false);
      }
      const commentList = await fetchComments(postId);
      if (!cancelled) {
        setComments(commentList);
        setLoadingComments(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [postId]);

  const tooLong = draft.length > MAX_COMMENT_LENGTH;

  async function handleSubmitComment() {
    if (posting || !draft.trim() || tooLong) return;
    setPosting(true);
    setError(null);
    const optimistic: CommentDto = {
      id: `local-${Date.now()}`,
      authorHandle: "you",
      authorDisplayName: "You",
      content: draft.trim(),
      createdAt: new Date().toISOString(),
    };
    setComments((prev) => [...prev, optimistic]);
    const result = await postComment(postId, draft.trim());
    if (result.ok) {
      setComments((prev) =>
        prev.map((c) => (c.id === optimistic.id ? result.comment : c)),
      );
      setDraft("");
    } else {
      setComments((prev) => prev.filter((c) => c.id !== optimistic.id));
      setError(result.message);
    }
    setPosting(false);
  }

  return (
    <div className="flex flex-col gap-5">
      <PageHeader title="Post" leftSlot={<BackButton />} />
      {loadingPost && !post ? (
        <div
          className="h-40 rounded-2xl"
          style={{ background: "var(--oat)" }}
        />
      ) : post ? (
        <PostCard post={post} />
      ) : (
        <p className="text-sm italic" style={{ color: "var(--muted)" }}>
          That post isn&apos;t available.
        </p>
      )}

      <section aria-label="Comments" className="flex flex-col gap-3">
        <h2
          className="text-base font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          Comments
        </h2>
        {loadingComments ? (
          <div
            className="h-20 rounded-xl"
            style={{ background: "var(--oat)" }}
          />
        ) : comments.length === 0 ? (
          <p className="text-sm italic" style={{ color: "var(--muted)" }}>
            Be the first to comment.
          </p>
        ) : (
          <ul className="flex flex-col gap-3">
            {comments.map((c) => (
              <li key={c.id} className="flex items-start gap-2">
                <Avatar name={c.authorDisplayName} size="sm" variant="ink" />
                <div
                  className="flex-1 rounded-2xl p-3"
                  style={{
                    background: "var(--oat)",
                    border: "1px solid var(--border)",
                  }}
                >
                  <span
                    className="block text-xs font-semibold"
                    style={{ color: "var(--aubergine)" }}
                  >
                    {c.authorDisplayName}
                  </span>
                  <p
                    className="mt-1 text-sm"
                    style={{ color: "var(--charcoal)" }}
                  >
                    {c.content}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        )}

        <div className="flex flex-col gap-2">
          <textarea
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            rows={3}
            maxLength={MAX_COMMENT_LENGTH + 200}
            placeholder="Add a comment"
            aria-label="Comment"
            className="w-full rounded-xl px-4 py-3 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: `1px solid ${tooLong ? "var(--amber)" : "var(--border)"}`,
              color: "var(--charcoal)",
            }}
          />
          <div className="flex items-center justify-between text-xs">
            <span
              style={{
                color: tooLong ? "var(--amber)" : "var(--muted)",
              }}
            >
              {draft.length} / {MAX_COMMENT_LENGTH}
            </span>
            <Button
              variant="primary"
              onClick={handleSubmitComment}
              disabled={posting || !draft.trim() || tooLong}
            >
              {posting ? "Posting…" : "Post comment"}
            </Button>
          </div>
          {tooLong ? (
            <p
              role="alert"
              className="text-xs"
              style={{ color: "var(--amber)" }}
            >
              Keep comments under {MAX_COMMENT_LENGTH} characters.
            </p>
          ) : null}
          {error ? (
            <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
              {error}
            </p>
          ) : null}
        </div>
      </section>
    </div>
  );
}
