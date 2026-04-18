"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { Avatar } from "@/shared/ui/Avatar";
import { Button } from "@/shared/ui/Button";
import { DietaryCertaintyBadge } from "@/shared/ui/DietaryCertaintyBadge";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import type { components } from "@/types/api.generated";
import type { DietaryTag } from "@/lib/dietary";
import { DIETARY_UI } from "@/lib/dietary";

type Post = components["schemas"]["PostDto"];

type PostCardProps = {
  post: Post;
  onAddToBasket?: (post: Post) => void;
};

const REACTION_LABELS: Record<string, string> = {
  TRIED_THIS: "Tried this",
  BETTER_ALT: "Better alt",
  WOULDNT_RECOMMEND: "Wouldn't recommend",
};

export function PostCard({ post, onAddToBasket }: PostCardProps) {
  const router = useRouter();
  const product = post.product ?? null;
  const basket = post.basket ?? null;
  const dietaryBadges = (product?.dietaryTags ?? []).filter(
    (tag): tag is DietaryTag => tag in DIETARY_UI,
  );
  const reactionTotal = (post.reactions ?? []).reduce(
    (sum, r) => sum + (r.count ?? 0),
    0,
  );
  const topReaction = (post.reactions ?? [])
    .slice()
    .sort((a, b) => (b.count ?? 0) - (a.count ?? 0))[0];

  const handleBuildSimilar = () => {
    if (!basket) return;
    const params = new URLSearchParams();
    const tags = basket.tags && basket.tags.length > 0 ? basket.tags.join(", ") : basket.title;
    params.set("intent", tags);
    router.push(`/home?${params.toString()}`);
  };

  return (
    <article
      className="flex flex-col gap-3 rounded-2xl p-4"
      style={{
        background: "var(--oat)",
        border: "1px solid var(--border)",
      }}
    >
      <header className="flex items-center gap-3">
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
      </header>

      <p className="text-sm" style={{ color: "var(--charcoal)" }}>
        {post.caption}
      </p>

      {product ? (
        <ProductCard product={product} dietaryBadges={dietaryBadges} />
      ) : null}

      {basket && (basket.itemThumbnails ?? []).length > 0 ? (
        <BasketPostCard basket={basket} onBuildSimilar={handleBuildSimilar} />
      ) : null}

      <footer className="flex items-center justify-between gap-3 text-xs">
        <div
          className="flex items-center gap-4"
          style={{ color: "var(--muted)" }}
        >
          {reactionTotal > 0 ? (
            <span>
              {reactionTotal} {REACTION_LABELS[topReaction?.type ?? ""] ?? "reactions"}
            </span>
          ) : null}
          <Link
            href={`/social/${encodeURIComponent(post.id)}`}
            style={{ color: "var(--muted)" }}
          >
            {post.commentCount} comments
          </Link>
          {post.basket ? (
            <Link
              href={`/basket/shared/${encodeURIComponent(post.basket.basketId)}`}
              style={{ color: "var(--clay)" }}
            >
              View basket
            </Link>
          ) : null}
        </div>
        {onAddToBasket && product ? (
          <Button variant="secondary" onClick={() => onAddToBasket(post)}>
            Add to basket
          </Button>
        ) : null}
      </footer>
    </article>
  );
}

function ProductCard({
  product,
  dietaryBadges,
}: {
  product: NonNullable<Post["product"]>;
  dietaryBadges: DietaryTag[];
}) {
  return (
    <div
      className="flex flex-col gap-2 rounded-xl p-3"
      style={{
        background: "var(--cream)",
        border: "1px solid var(--border)",
      }}
    >
      <ProductImage
        imageUrl={product.imageUrl}
        alt={product.name}
        price={product.price}
        retailer={product.retailer}
      />
      <div className="flex flex-col gap-1">
        <span
          className="text-sm font-medium"
          style={{ color: "var(--aubergine)" }}
        >
          {product.name}
        </span>
        {product.brand ? (
          <span className="text-xs" style={{ color: "var(--muted)" }}>
            {product.brand}
          </span>
        ) : null}
        {dietaryBadges.length > 0 ? (
          <div className="flex flex-wrap gap-1.5">
            {dietaryBadges.map((tag) => (
              <DietaryCertaintyBadge key={tag} tag={tag} />
            ))}
          </div>
        ) : null}
      </div>
    </div>
  );
}

function BasketPostCard({
  basket,
  onBuildSimilar,
}: {
  basket: NonNullable<Post["basket"]>;
  onBuildSimilar: () => void;
}) {
  const thumbnails = (basket.itemThumbnails ?? []).slice(0, 4);
  while (thumbnails.length < 4) thumbnails.push("");
  const dietary = basket.dietarySummary ?? null;
  const hasPleaseVerify = Boolean(
    dietary && (dietary.hasHalalLikely || dietary.hasHalalUnknown),
  );
  return (
    <div
      className="flex flex-col gap-3 rounded-xl p-3"
      style={{
        background: "var(--cream)",
        border: "1px solid var(--border)",
      }}
    >
      <div className="grid grid-cols-2 gap-2">
        {thumbnails.map((src, idx) => (
          <div
            key={idx}
            className="flex h-20 items-center justify-center overflow-hidden rounded-lg"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
            }}
          >
            {src ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={src}
                alt={`Basket item ${idx + 1}`}
                className="h-full w-full object-cover"
              />
            ) : (
              <span aria-hidden="true" className="text-xl">
                🛒
              </span>
            )}
          </div>
        ))}
      </div>
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-0.5">
          <span
            className="text-sm font-semibold"
            style={{ color: "var(--aubergine)" }}
          >
            {basket.title}
          </span>
          {hasPleaseVerify ? (
            <span className="text-xs" style={{ color: "#6B2A11" }}>
              Contains items marked &lsquo;please verify&rsquo;
            </span>
          ) : null}
        </div>
        <span
          className="rounded-full px-3 py-1 text-xs font-semibold"
          style={{
            background: "var(--clay-light)",
            color: "var(--clay)",
            fontVariantNumeric: "tabular-nums",
          }}
        >
          £{basket.total.toFixed(2)}
        </span>
      </div>
      <Button variant="secondary" onClick={onBuildSimilar}>
        Build similar
      </Button>
    </div>
  );
}

function ProductImage({
  imageUrl,
  alt,
  price,
  retailer,
}: {
  imageUrl?: string | null;
  alt: string;
  price: number;
  retailer: string;
}) {
  return (
    <div
      className="relative flex h-40 w-full items-center justify-center overflow-hidden rounded-lg"
      style={{
        background: "var(--oat)",
        border: "1px solid var(--border)",
      }}
    >
      {imageUrl ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={imageUrl}
          alt={alt}
          className="h-full w-full object-cover"
        />
      ) : (
        <span aria-hidden="true" className="text-4xl">
          🛒
        </span>
      )}
      <span
        className="absolute left-3 top-3"
      >
        <RetailerBadge retailer={retailer} />
      </span>
      <span
        className="absolute right-3 top-3 rounded-full px-3 py-1 text-xs font-semibold"
        style={{
          background: "var(--aubergine)",
          color: "var(--cream)",
          fontVariantNumeric: "tabular-nums",
        }}
      >
        £{price.toFixed(2)}
      </span>
    </div>
  );
}
