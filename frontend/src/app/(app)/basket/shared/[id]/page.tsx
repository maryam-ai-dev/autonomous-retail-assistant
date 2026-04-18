"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Avatar } from "@/shared/ui/Avatar";
import { Button } from "@/shared/ui/Button";
import { DietaryCertaintyBadge } from "@/shared/ui/DietaryCertaintyBadge";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import { Tag } from "@/shared/ui/Tag";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import {
  fetchComments,
  postComment,
  MAX_COMMENT_LENGTH,
  type CommentDto,
} from "@/features/social/comments";
import {
  fetchSharedBasket,
  forkSharedBasket,
  type SharedBasketResult,
} from "@/features/shared-basket/fetch";
import { FollowButton } from "@/features/social/FollowButton";
import { saveBasket } from "@/features/social/follow";
import { isAuthenticated } from "@/core/auth/session";
import type { components } from "@/types/api.generated";

type SharedBasket = components["schemas"]["SharedBasketDto"];
type BasketItem = components["schemas"]["BasketItemDto"];

type DietarySummaryCounts = {
  halalVerified: number;
  halalLikely: number;
  halalUnknown: number;
};

export default function SharedBasketPage() {
  const params = useParams<{ id: string }>();
  const shareId = params?.id ?? "";
  const [result, setResult] = useState<SharedBasketResult | null>(null);
  const [expanded, setExpanded] = useState(false);
  const [comments, setComments] = useState<CommentDto[]>([]);
  const [draft, setDraft] = useState("");
  const [posting, setPosting] = useState(false);
  const [forkError, setForkError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    if (!shareId) return;
    let cancelled = false;
    (async () => {
      const fetched = await fetchSharedBasket(shareId);
      if (!cancelled) setResult(fetched);
      const commentList = await fetchComments(`shared-basket:${shareId}`);
      if (!cancelled) setComments(commentList);
    })();
    return () => {
      cancelled = true;
    };
  }, [shareId]);

  if (!result) return <SharedBasketSkeleton />;

  if (result.kind === "not-found")
    return (
      <NotFound message="That shared basket isn't available." homeHref="/home" />
    );
  if (result.kind === "error")
    return <NotFound message={result.message} homeHref="/home" />;

  const shared = result.data;
  const visibleItems = expanded
    ? shared.basket.items
    : shared.basket.items.slice(0, 3);
  const dietaryCounts = countDietary(shared.basket.items);

  async function handleBuild() {
    if (!isAuthenticated()) {
      router.push(
        `/login?returnTo=${encodeURIComponent(
          `/basket/shared/${shareId}`,
        )}`,
      );
      return;
    }
    const forkResult = await forkSharedBasket(shareId);
    if (!forkResult.ok) {
      setForkError(forkResult.message);
      return;
    }
    router.push(`/basket/${encodeURIComponent(forkResult.basketId)}`);
  }

  async function handleSubmitComment() {
    const content = draft.trim();
    if (!content || content.length > MAX_COMMENT_LENGTH || posting) return;
    setPosting(true);
    const optimistic: CommentDto = {
      id: `local-${Date.now()}`,
      authorHandle: "you",
      authorDisplayName: "You",
      content,
      createdAt: new Date().toISOString(),
    };
    setComments((prev) => [...prev, optimistic]);
    const result = await postComment(`shared-basket:${shareId}`, content);
    if (result.ok) {
      setComments((prev) =>
        prev.map((c) => (c.id === optimistic.id ? result.comment : c)),
      );
      setDraft("");
    } else {
      setComments((prev) => prev.filter((c) => c.id !== optimistic.id));
    }
    setPosting(false);
  }

  return (
    <div className="flex flex-col gap-5">
      <PageHeader title="Shared basket" leftSlot={<BackButton />} />

      <PosterHeader shared={shared} />

      <section className="flex flex-col gap-2">
        <h2
          className="text-2xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          {shared.title}
        </h2>
        {shared.description ? (
          <p className="text-sm" style={{ color: "var(--muted)" }}>
            {shared.description}
          </p>
        ) : null}
        {shared.tags.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {shared.tags.map((t) => (
              <Tag key={t} variant="clay">
                {t}
              </Tag>
            ))}
          </div>
        ) : null}
      </section>

      <StatsTiles shared={shared} />

      <DietarySummary counts={dietaryCounts} />

      <section aria-label="Basket items" className="flex flex-col gap-3">
        <h3
          className="text-base font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          Items
        </h3>
        <ul className="flex flex-col gap-2">
          {visibleItems.map((item) => (
            <li
              key={item.id}
              className="flex items-center justify-between rounded-xl p-3"
              style={{
                background: "var(--oat)",
                border: "1px solid var(--border)",
              }}
            >
              <div className="flex flex-col gap-1">
                <span
                  className="text-sm font-medium"
                  style={{ color: "var(--aubergine)" }}
                >
                  {item.name}
                </span>
                <div className="flex items-center gap-2">
                  <RetailerBadge retailer={item.retailer} />
                  <span
                    className="text-xs"
                    style={{ color: "var(--muted)" }}
                  >
                    Qty {item.quantity}
                  </span>
                </div>
              </div>
              <span
                className="text-sm font-semibold"
                style={{
                  color: "var(--aubergine)",
                  fontVariantNumeric: "tabular-nums",
                }}
              >
                £{(item.price * item.quantity).toFixed(2)}
              </span>
            </li>
          ))}
        </ul>
        {shared.basket.items.length > 3 ? (
          <button
            type="button"
            onClick={() => setExpanded((v) => !v)}
            className="self-start text-xs font-semibold"
            style={{ color: "var(--clay)" }}
          >
            {expanded ? "Show less" : `See all ${shared.basket.items.length} items`}
          </button>
        ) : null}
      </section>

      <div className="flex flex-col gap-2">
        <Button variant="primary" fullWidth onClick={handleBuild}>
          Build this basket for me
        </Button>
        {isAuthenticated() ? (
          <SaveBasketButton basketId={shared.basket.id} />
        ) : null}
      </div>
      {forkError ? (
        <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
          {forkError}
        </p>
      ) : null}

      <section aria-label="Comments" className="flex flex-col gap-3">
        <h3
          className="text-base font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          Comments
        </h3>
        {comments.length === 0 ? (
          <p className="text-sm italic" style={{ color: "var(--muted)" }}>
            Be the first to comment.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {comments.map((c) => (
              <li
                key={c.id}
                className="max-w-[85%] rounded-2xl px-3 py-2"
                style={{
                  background: "var(--oat)",
                  border: "1px solid var(--border)",
                }}
              >
                <span
                  className="text-xs font-semibold"
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
              </li>
            ))}
          </ul>
        )}
        {isAuthenticated() ? (
          <div className="flex flex-col gap-2">
            <textarea
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              rows={2}
              maxLength={MAX_COMMENT_LENGTH + 200}
              placeholder="Add a comment"
              aria-label="Comment"
              className="w-full rounded-xl px-3 py-2 text-sm outline-none"
              style={{
                background: "var(--oat)",
                border: `1px solid ${
                  draft.length > MAX_COMMENT_LENGTH
                    ? "var(--amber)"
                    : "var(--border)"
                }`,
              }}
            />
            <Button
              variant="secondary"
              onClick={handleSubmitComment}
              disabled={
                posting ||
                !draft.trim() ||
                draft.length > MAX_COMMENT_LENGTH
              }
            >
              {posting ? "Posting…" : "Post"}
            </Button>
          </div>
        ) : null}
      </section>
    </div>
  );
}

function PosterHeader({ shared }: { shared: SharedBasket }) {
  const authed = isAuthenticated();
  return (
    <div className="flex items-center gap-3">
      <Avatar
        name={shared.poster.displayName}
        size="md"
        variant="ink"
      />
      <div className="flex flex-col">
        <span
          className="text-sm font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          {shared.poster.displayName}
        </span>
        <span className="text-xs" style={{ color: "var(--muted)" }}>
          @{shared.poster.handle}
        </span>
      </div>
      {authed ? (
        <div className="ml-auto">
          <FollowButton
            handle={shared.poster.handle}
            isSelf={shared.poster.isSelf}
          />
        </div>
      ) : null}
    </div>
  );
}

function SaveBasketButton({ basketId }: { basketId: string }) {
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);
  async function handleClick() {
    if (saved || busy) return;
    setBusy(true);
    const result = await saveBasket(basketId);
    setBusy(false);
    if (result.ok) setSaved(true);
  }
  return (
    <Button
      variant="secondary"
      onClick={handleClick}
      disabled={busy || saved}
      aria-pressed={saved}
    >
      {saved ? "Saved" : busy ? "Saving…" : "Save basket"}
    </Button>
  );
}

function StatsTiles({ shared }: { shared: SharedBasket }) {
  const tiles: { label: string; value: string }[] = [
    { label: "Total", value: `£${shared.basket.totalCost.toFixed(2)}` },
    { label: "Items", value: String(shared.basket.items.length) },
    {
      label: "Estimated saving",
      value: `£${shared.estimatedSaving.toFixed(2)}`,
    },
  ];
  return (
    <div className="grid grid-cols-3 gap-2">
      {tiles.map((t) => (
        <div
          key={t.label}
          className="flex flex-col gap-1 rounded-xl p-3"
          style={{
            background: "var(--oat)",
            border: "1px solid var(--border)",
          }}
        >
          <span
            className="text-[11px] uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            {t.label}
          </span>
          <span
            className="text-base font-semibold"
            style={{
              color: "var(--aubergine)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            {t.value}
          </span>
        </div>
      ))}
    </div>
  );
}

function DietarySummary({ counts }: { counts: DietarySummaryCounts }) {
  const total = counts.halalVerified + counts.halalLikely + counts.halalUnknown;
  if (total === 0) return null;
  return (
    <section
      aria-label="Dietary certainty summary"
      className="flex flex-wrap gap-2 rounded-xl p-3"
      style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
    >
      {counts.halalVerified > 0 ? (
        <span
          className="inline-flex items-center gap-2"
          style={{ color: "var(--aubergine)" }}
        >
          <strong>{counts.halalVerified}</strong>{" "}
          <DietaryCertaintyBadge tag="HALAL_VERIFIED" />
        </span>
      ) : null}
      {counts.halalLikely > 0 ? (
        <span
          className="inline-flex items-center gap-2"
          style={{ color: "var(--aubergine)" }}
        >
          <strong>{counts.halalLikely}</strong>{" "}
          <DietaryCertaintyBadge tag="HALAL_LIKELY" />
        </span>
      ) : null}
      {counts.halalUnknown > 0 ? (
        <span
          className="inline-flex items-center gap-2"
          style={{ color: "var(--aubergine)" }}
        >
          <strong>{counts.halalUnknown}</strong>{" "}
          <DietaryCertaintyBadge tag="HALAL_UNKNOWN" />
        </span>
      ) : null}
    </section>
  );
}

function countDietary(items: BasketItem[]): DietarySummaryCounts {
  let verified = 0;
  let likely = 0;
  let unknown = 0;
  for (const item of items) {
    const tags = item.dietaryTags ?? [];
    if (tags.includes("HALAL_VERIFIED")) verified += 1;
    else if (tags.includes("HALAL_LIKELY")) likely += 1;
    else if (tags.includes("HALAL_UNKNOWN")) unknown += 1;
  }
  return {
    halalVerified: verified,
    halalLikely: likely,
    halalUnknown: unknown,
  };
}

function SharedBasketSkeleton() {
  return (
    <div className="flex flex-col gap-4">
      <div
        className="h-8 w-40 rounded"
        style={{ background: "var(--oat)" }}
      />
      <div
        className="h-40 rounded-2xl"
        style={{ background: "var(--oat)" }}
      />
    </div>
  );
}

function NotFound({
  message,
  homeHref,
}: {
  message: string;
  homeHref: string;
}) {
  return (
    <div className="flex flex-col items-center gap-4 pt-10 text-center">
      <h1
        className="text-2xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        {message}
      </h1>
      <Link
        href={homeHref}
        className="text-sm font-semibold underline"
        style={{ color: "var(--clay)" }}
      >
        Go home
      </Link>
    </div>
  );
}
