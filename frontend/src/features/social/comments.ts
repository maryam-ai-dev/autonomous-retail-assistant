"use client";

import apiClient from "@/core/api/client";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

export type CommentDto = {
  id: string;
  authorHandle: string;
  authorDisplayName: string;
  content: string;
  createdAt: string;
};

export const MAX_COMMENT_LENGTH = 500;

const MOCK_COMMENTS: CommentDto[] = [
  {
    id: "c1",
    authorHandle: "amira",
    authorDisplayName: "Amira",
    content: "I tried this last week — worth it.",
    createdAt: "2026-04-12T09:00:00Z",
  },
  {
    id: "c2",
    authorHandle: "layla",
    authorDisplayName: "Layla",
    content: "Cheaper than my usual, thanks!",
    createdAt: "2026-04-12T10:15:00Z",
  },
];

export async function fetchComments(postId: string): Promise<CommentDto[]> {
  if (USE_MOCKS) return MOCK_COMMENTS;
  try {
    const response = await apiClient.get<CommentDto[]>(
      `/api/social/posts/${encodeURIComponent(postId)}/comments`,
      { timeout: 8_000 },
    );
    return response.data ?? [];
  } catch {
    return [];
  }
}

export async function postComment(
  postId: string,
  content: string,
): Promise<{ ok: true; comment: CommentDto } | { ok: false; message: string }> {
  if (content.length > MAX_COMMENT_LENGTH) {
    return {
      ok: false,
      message: `Keep comments under ${MAX_COMMENT_LENGTH} characters.`,
    };
  }
  if (USE_MOCKS) {
    return {
      ok: true,
      comment: {
        id: `local-${Date.now()}`,
        authorHandle: "you",
        authorDisplayName: "You",
        content,
        createdAt: new Date().toISOString(),
      },
    };
  }
  try {
    const response = await apiClient.post<CommentDto>(
      `/api/social/posts/${encodeURIComponent(postId)}/comments`,
      { content },
      { timeout: 10_000 },
    );
    return { ok: true, comment: response.data };
  } catch (err) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : "Couldn't post comment",
    };
  }
}
