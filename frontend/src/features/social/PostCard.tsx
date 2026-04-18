"use client";

import Link from "next/link";
import { Avatar } from "@/shared/ui/Avatar";
import { Button } from "@/shared/ui/Button";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import type { components } from "@/types/api.generated";

type Post = components["schemas"]["PostDto"];

type PostCardProps = {
  post: Post;
  onAddToBasket?: (post: Post) => void;
};

export function PostCard({ post, onAddToBasket }: PostCardProps) {
  return (
    <article
      className="flex flex-col gap-3 rounded-2xl p-4"
      style={{
        background: "var(--oat)",
        border: "1px solid var(--border)",
      }}
    >
      <div className="flex items-center gap-3">
        <Avatar name={post.authorDisplayName} size="sm" variant="ink" />
        <div className="flex flex-col">
          <span
            className="text-sm font-semibold"
            style={{ color: "var(--aubergine)" }}
          >
            {post.authorDisplayName}
          </span>
          <Link
            href={`/profile/${encodeURIComponent(post.authorHandle)}`}
            className="text-xs"
            style={{ color: "var(--muted)" }}
          >
            @{post.authorHandle}
          </Link>
        </div>
        {post.product ? (
          <span className="ml-auto">
            <RetailerBadge retailer={post.product.retailer} />
          </span>
        ) : null}
      </div>

      <p className="text-sm" style={{ color: "var(--charcoal)" }}>
        {post.caption}
      </p>

      {post.product ? (
        <div
          className="flex items-center justify-between rounded-xl p-3"
          style={{
            background: "var(--cream)",
            border: "1px solid var(--border)",
          }}
        >
          <div className="flex flex-col gap-0.5">
            <span
              className="text-sm font-medium"
              style={{ color: "var(--aubergine)" }}
            >
              {post.product.name}
            </span>
            {post.product.brand ? (
              <span className="text-xs" style={{ color: "var(--muted)" }}>
                {post.product.brand}
              </span>
            ) : null}
          </div>
          <span
            className="text-sm font-semibold"
            style={{
              color: "var(--aubergine)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            £{post.product.price.toFixed(2)}
          </span>
        </div>
      ) : null}

      {onAddToBasket ? (
        <Button variant="secondary" onClick={() => onAddToBasket(post)}>
          Add to basket
        </Button>
      ) : null}
    </article>
  );
}
