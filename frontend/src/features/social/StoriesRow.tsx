"use client";

import Link from "next/link";
import { Avatar } from "@/shared/ui/Avatar";
import type { components } from "@/types/api.generated";

type Post = components["schemas"]["PostDto"];

type Story = {
  handle: string;
  displayName: string;
  ringColour: "clay" | "sage";
};

function storiesFrom(posts: Post[]): Story[] {
  const seen = new Map<string, Story>();
  for (const post of posts) {
    if (seen.has(post.authorHandle)) continue;
    const isBasket = post.type === "BASKET" || post.basket !== null;
    seen.set(post.authorHandle, {
      handle: post.authorHandle,
      displayName: post.authorDisplayName,
      ringColour: isBasket ? "clay" : "sage",
    });
  }
  return Array.from(seen.values());
}

type StoriesRowProps = {
  posts: Post[];
};

export function StoriesRow({ posts }: StoriesRowProps) {
  const stories = storiesFrom(posts);
  if (stories.length === 0) return null;
  return (
    <div
      role="region"
      aria-label="Recent activity from people you follow"
      className="-mx-4 overflow-x-auto px-4"
    >
      <ul className="flex gap-3">
        {stories.map((story) => (
          <li key={story.handle}>
            <Link
              href={`/profile/${encodeURIComponent(story.handle)}`}
              aria-label={`${story.displayName}'s latest`}
              className="flex w-16 flex-col items-center gap-1"
            >
              <span
                className="flex h-14 w-14 items-center justify-center rounded-full"
                style={{
                  padding: 2,
                  background: "var(--cream)",
                  border: `2px solid var(--${story.ringColour})`,
                }}
              >
                <Avatar
                  name={story.displayName}
                  size="md"
                  variant={story.ringColour === "clay" ? "clay" : "sage"}
                />
              </span>
              <span
                className="max-w-full truncate text-[11px]"
                style={{ color: "var(--muted)" }}
              >
                {story.displayName}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
