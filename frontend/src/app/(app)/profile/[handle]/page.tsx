"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Avatar } from "@/shared/ui/Avatar";
import { DietaryCertaintyBadge } from "@/shared/ui/DietaryCertaintyBadge";
import { Tag } from "@/shared/ui/Tag";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import { FollowButton } from "@/features/social/FollowButton";
import { useProfile } from "@/lib/api/useProfile";
import {
  MOCK_BASKET_APPROVED,
  MOCK_BASKET_MIXED_DIETARY,
} from "@/lib/mock/baskets";
import type { components } from "@/types/api.generated";
import type { DietaryTag } from "@/lib/dietary";
import { DIETARY_UI } from "@/lib/dietary";

type Profile = components["schemas"]["ProfileDto"];
type Basket = components["schemas"]["BasketDto"];

type TabKey = "baskets" | "reviews" | "saved";

const TABS: { key: TabKey; label: string }[] = [
  { key: "baskets", label: "Baskets" },
  { key: "reviews", label: "Reviews" },
  { key: "saved", label: "Saved" },
];

const MOCK_PROFILE_BASKETS: Basket[] = [
  MOCK_BASKET_MIXED_DIETARY,
  MOCK_BASKET_APPROVED,
];

const MOCK_TASTE_TAGS = ["halal", "family", "budget-first"];

export default function ProfilePage() {
  const params = useParams<{ handle: string }>();
  const handle = params?.handle ?? "";
  const { data, isLoading, error } = useProfile(handle);
  const [tab, setTab] = useState<TabKey>("baskets");

  if (isLoading && !data)
    return (
      <div className="flex flex-col gap-4">
        <div className="h-10 w-40 rounded" style={{ background: "var(--oat)" }} />
        <div className="h-32 rounded-2xl" style={{ background: "var(--oat)" }} />
      </div>
    );
  if (error?.notFound || !data) return <ProfileNotFound />;

  return (
    <div className="flex flex-col gap-5">
      <PageHeader title="Profile" leftSlot={<BackButton />} />
      <ProfileHero profile={data} />
      <ProfileStats profile={data} />
      <Tabs tab={tab} onChange={setTab} />
      <TabPanel tab={tab} />
      <TasteTags tags={MOCK_TASTE_TAGS} />
    </div>
  );
}

function ProfileHero({ profile }: { profile: Profile }) {
  return (
    <div className="flex items-start gap-4">
      <div style={{ width: 52, height: 52 }}>
        <Avatar name={profile.displayName} size="lg" variant="ink" />
      </div>
      <div className="flex flex-1 flex-col gap-1">
        <h1
          className="text-2xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          {profile.displayName}
        </h1>
        <span className="text-xs" style={{ color: "var(--muted)" }}>
          @{profile.handle}
          {profile.location ? ` · ${profile.location}` : ""}
        </span>
        {profile.bio ? (
          <p className="text-sm" style={{ color: "var(--charcoal)" }}>
            {profile.bio}
          </p>
        ) : null}
      </div>
      <FollowButton handle={profile.handle} isSelf={profile.isSelf} />
    </div>
  );
}

function ProfileStats({ profile }: { profile: Profile }) {
  const stats: { label: string; value: number }[] = [
    { label: "Baskets", value: profile.basketsCount },
    { label: "Followers", value: profile.followersCount },
    { label: "Following", value: profile.followingCount },
  ];
  return (
    <div className="grid grid-cols-3 gap-2">
      {stats.map((s) => (
        <div
          key={s.label}
          className="flex flex-col gap-1 rounded-xl p-3"
          style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
        >
          <span
            className="text-[11px] uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            {s.label}
          </span>
          <span
            className="text-base font-semibold"
            style={{
              color: "var(--aubergine)",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            {s.value}
          </span>
        </div>
      ))}
    </div>
  );
}

function Tabs({ tab, onChange }: { tab: TabKey; onChange: (next: TabKey) => void }) {
  return (
    <div
      role="tablist"
      aria-label="Profile sections"
      className="flex items-center gap-1 rounded-full p-1"
      style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
    >
      {TABS.map((t) => {
        const active = t.key === tab;
        return (
          <button
            key={t.key}
            type="button"
            role="tab"
            aria-selected={active}
            aria-controls={`panel-${t.key}`}
            id={`tab-${t.key}`}
            onClick={() => onChange(t.key)}
            className="flex-1 rounded-full px-3 py-2 text-xs font-semibold"
            style={{
              background: active ? "var(--clay)" : "transparent",
              color: active ? "var(--cream)" : "var(--aubergine)",
              minHeight: 44,
            }}
          >
            {t.label}
          </button>
        );
      })}
    </div>
  );
}

function TabPanel({ tab }: { tab: TabKey }) {
  return (
    <div
      id={`panel-${tab}`}
      role="tabpanel"
      aria-labelledby={`tab-${tab}`}
    >
      {tab === "baskets" ? <BasketGrid baskets={MOCK_PROFILE_BASKETS} /> : null}
      {tab === "reviews" ? (
        <EmptyState message="No reviews yet." />
      ) : null}
      {tab === "saved" ? (
        <EmptyState message="No saved baskets yet." />
      ) : null}
    </div>
  );
}

function BasketGrid({ baskets }: { baskets: Basket[] }) {
  if (baskets.length === 0) {
    return <EmptyState message="No shared baskets yet." />;
  }
  return (
    <ul className="grid grid-cols-2 gap-3">
      {baskets.map((basket) => {
        const dietary = highlightDietary(basket);
        return (
          <li
            key={basket.id}
            className="flex flex-col gap-2 rounded-xl p-3"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
            }}
          >
            <Link
              href={`/basket/${encodeURIComponent(basket.id)}`}
              className="flex flex-col gap-1"
            >
              <span
                className="text-sm font-semibold leading-tight"
                style={{ color: "var(--aubergine)" }}
              >
                {basket.intentText}
              </span>
              <span
                className="text-xs"
                style={{
                  color: "var(--muted)",
                  fontVariantNumeric: "tabular-nums",
                }}
              >
                £{basket.totalCost.toFixed(2)} · {basket.items.length} items
              </span>
            </Link>
            {dietary.length > 0 ? (
              <div className="flex flex-wrap gap-1.5">
                {dietary.map((tag) => (
                  <DietaryCertaintyBadge key={tag} tag={tag} />
                ))}
              </div>
            ) : null}
          </li>
        );
      })}
    </ul>
  );
}

function TasteTags({ tags }: { tags: string[] }) {
  if (tags.length === 0) return null;
  return (
    <section aria-label="Taste profile" className="flex flex-col gap-2">
      <h2
        className="text-sm font-semibold"
        style={{ color: "var(--aubergine)" }}
      >
        Taste
      </h2>
      <div className="flex flex-wrap gap-2">
        {tags.map((t) => (
          <Tag key={t} variant="sage">
            {t}
          </Tag>
        ))}
      </div>
    </section>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <p
      className="py-6 text-center text-sm italic"
      style={{ color: "var(--muted)" }}
    >
      {message}
    </p>
  );
}

function ProfileNotFound() {
  return (
    <div className="flex flex-col items-center gap-4 pt-10 text-center">
      <h1
        className="text-2xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        That profile doesn&apos;t exist
      </h1>
      <Link
        href="/home"
        className="text-sm font-semibold underline"
        style={{ color: "var(--clay)" }}
      >
        Go home
      </Link>
    </div>
  );
}

function highlightDietary(basket: Basket): DietaryTag[] {
  const seen = new Set<DietaryTag>();
  for (const item of basket.items) {
    for (const tag of item.dietaryTags ?? []) {
      if (tag in DIETARY_UI) seen.add(tag as DietaryTag);
    }
  }
  return Array.from(seen).slice(0, 2);
}
