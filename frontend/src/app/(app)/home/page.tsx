"use client";

import { Suspense, useState, type FormEvent } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Button } from "@/shared/ui/Button";
import { useActiveBasket } from "@/lib/api/useActiveBasket";
import { useCurrentUser } from "@/lib/api/useCurrentUser";

export default function HomePage() {
  return (
    <Suspense fallback={<HomeSkeleton />}>
      <HomeContent />
    </Suspense>
  );
}

function HomeSkeleton() {
  return (
    <div className="flex flex-col gap-5">
      <div className="h-4" />
      <div
        className="h-10 w-40 rounded"
        style={{ background: "var(--oat)" }}
      />
      <div
        className="h-40 rounded-2xl"
        style={{ background: "var(--oat)" }}
      />
    </div>
  );
}

function HomeContent() {
  const user = useCurrentUser();
  const activeBasket = useActiveBasket();
  const params = useSearchParams();
  const prefilledIntent = params?.get("intent") ?? "";
  const [intent, setIntent] = useState<string>(prefilledIntent);

  const greetingName = user.data?.displayName ?? "there";
  const greeting = `Hi ${greetingName}`;

  const basket = activeBasket.data;

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    // full submission wiring lives in sprint 13
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <span />
        <span className="text-[11px]" style={{ color: "var(--muted)" }}>
          {greeting}
        </span>
      </div>

      <h1
        className="text-3xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        Your basket
      </h1>

      {basket ? (
        <Link
          href={`/basket/${encodeURIComponent(basket.id)}`}
          className="flex items-center justify-between rounded-2xl p-5"
          style={{
            background: "var(--oat)",
            border: "1px solid var(--border)",
          }}
        >
          <div className="flex flex-col gap-1">
            <span
              className="text-xs font-semibold uppercase tracking-wide"
              style={{ color: "var(--clay)" }}
            >
              Draft in progress
            </span>
            <span
              className="text-base font-medium"
              style={{ color: "var(--aubergine)" }}
            >
              {basket.intentText}
            </span>
            <span className="text-sm" style={{ color: "var(--muted)" }}>
              £{basket.totalCost.toFixed(2)} of £{basket.budget.toFixed(2)}
            </span>
          </div>
          <span aria-hidden="true" style={{ color: "var(--clay)" }}>→</span>
        </Link>
      ) : (
        <form
          onSubmit={handleSubmit}
          className="flex flex-col gap-3 rounded-2xl p-5"
          style={{
            background: "var(--oat)",
            border: "1px solid var(--border)",
          }}
        >
          <label
            htmlFor="home-intent"
            className="text-xs font-medium uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            What do you need this week?
          </label>
          <textarea
            id="home-intent"
            value={intent}
            onChange={(e) => setIntent(e.target.value)}
            rows={3}
            placeholder="Weekly groceries under £70, halal"
            className="w-full rounded-xl px-4 py-3 text-sm outline-none italic placeholder:italic"
            style={{
              background: "var(--cream)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
            }}
          />
          <Button type="submit" variant="primary" fullWidth disabled>
            Build my basket
          </Button>
          <p className="text-xs" style={{ color: "var(--muted)" }}>
            We&apos;ll search across UK retailers for things that fit your
            budget and taste.
          </p>
        </form>
      )}

      {!basket && !activeBasket.isLoading ? (
        <p className="text-sm italic" style={{ color: "var(--muted)" }}>
          Tell us what you need and we&apos;ll do the hunting.
        </p>
      ) : null}
    </div>
  );
}
