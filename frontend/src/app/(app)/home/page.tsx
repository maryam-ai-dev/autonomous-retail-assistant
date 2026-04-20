"use client";

import { Suspense, useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/shared/ui/Button";
import { useActiveBasket } from "@/lib/api/useActiveBasket";
import { useCurrentUser } from "@/lib/api/useCurrentUser";
import {
  submitBasketIntent,
  type SubmitIntentResult,
} from "@/features/basket-intent/submit";
import { ClothingProfileSheet } from "@/features/basket-intent/ClothingProfileSheet";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];

type SubmitState =
  | { kind: "idle" }
  | { kind: "submitting" }
  | { kind: "success"; basket: Basket }
  | { kind: "clothing-required" }
  | { kind: "error"; message: string; isTimeout: boolean };

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
  const router = useRouter();
  const user = useCurrentUser();
  const activeBasket = useActiveBasket();
  const params = useSearchParams();
  const prefilledIntent = params?.get("intent") ?? "";
  const [intent, setIntent] = useState<string>(prefilledIntent);
  const [submit, setSubmit] = useState<SubmitState>({ kind: "idle" });

  const greetingName = user.data?.displayName ?? "there";
  const greeting = `Hi ${greetingName}`;

  const basket = activeBasket.data;

  async function runSubmit(intentText: string) {
    setSubmit({ kind: "submitting" });
    const result = await submitBasketIntent(intentText);
    applySubmitResult(result);
  }

  function applySubmitResult(result: SubmitIntentResult) {
    if (result.kind === "success") {
      setSubmit({ kind: "success", basket: result.basket });
      return;
    }
    if (result.kind === "clothing-required") {
      setSubmit({ kind: "clothing-required" });
      return;
    }
    setSubmit({
      kind: "error",
      message: result.message,
      isTimeout: result.isTimeout,
    });
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const text = intent.trim();
    if (!text) return;
    void runSubmit(text);
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
      ) : submit.kind === "success" ? (
        <BasketReadyCard
          basket={submit.basket}
          onReview={() =>
            router.push(`/basket/${encodeURIComponent(submit.basket.id)}`)
          }
        />
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
            placeholder="trainers under £60, size 6"
            className="w-full rounded-xl px-4 py-3 text-sm outline-none italic placeholder:italic"
            style={{
              background: "var(--cream)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
            }}
          />
          <Button
            type="submit"
            variant="primary"
            fullWidth
            disabled={submit.kind === "submitting" || !intent.trim()}
          >
            {submit.kind === "submitting" ? "Building…" : "Build my basket"}
          </Button>
          <p className="text-xs" style={{ color: "var(--muted)" }}>
            We&apos;ll search across UK retailers for things that fit your
            budget and taste.
          </p>
        </form>
      )}

      {submit.kind === "submitting" ? <BasketGenerationSkeleton /> : null}

      {submit.kind === "error" ? (
        <div
          role="alert"
          className="flex flex-col gap-3 rounded-2xl p-4"
          style={{
            background: "var(--amber-light)",
            border: "1px solid var(--amber)",
            color: "#6B2A11",
          }}
        >
          <p className="text-sm font-medium">{submit.message}</p>
          <Button variant="secondary" onClick={() => runSubmit(intent.trim())}>
            Try again
          </Button>
        </div>
      ) : null}

      <ClothingProfileSheet
        open={submit.kind === "clothing-required"}
        onClose={() => setSubmit({ kind: "idle" })}
        onSaved={() => void runSubmit(intent.trim())}
      />

      {submit.kind === "idle" && !basket && !activeBasket.isLoading ? (
        <p className="text-sm italic" style={{ color: "var(--muted)" }}>
          Tell us what you need and we&apos;ll do the hunting.
        </p>
      ) : null}
    </div>
  );
}

function BasketGenerationSkeleton() {
  return (
    <div
      role="status"
      aria-live="polite"
      className="flex flex-col gap-3 rounded-2xl p-5"
      style={{ background: "var(--oat)" }}
    >
      <div className="h-4 w-32 rounded" style={{ background: "var(--cream)" }} />
      <div className="h-16 rounded" style={{ background: "var(--cream)" }} />
      <div className="h-16 rounded" style={{ background: "var(--cream)" }} />
      <span className="sr-only">Building your basket…</span>
    </div>
  );
}

function BasketReadyCard({
  basket,
  onReview,
}: {
  basket: Basket;
  onReview: () => void;
}) {
  const warnings = basket.normalizationWarnings ?? [];
  return (
    <div
      className="flex flex-col gap-3 rounded-2xl p-5"
      style={{
        background: "var(--oat)",
        border: "1px solid var(--border)",
      }}
    >
      <span
        className="inline-flex max-w-max items-center gap-2 rounded-full px-3 py-1 text-xs font-medium"
        style={{
          background: "var(--clay-light)",
          color: "var(--clay)",
        }}
      >
        {basket.intentText}
      </span>
      {warnings.length > 0 ? (
        <ul
          className="flex flex-col gap-1 text-xs"
          style={{ color: "#6B2A11" }}
        >
          {warnings.map((warning, idx) => (
            <li key={idx}>⚠ {warning}</li>
          ))}
        </ul>
      ) : null}
      <div className="flex items-center justify-between">
        <div className="flex flex-col">
          <span
            className="text-xs uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Draft ready
          </span>
          <span
            className="text-lg font-semibold"
            style={{ color: "var(--aubergine)" }}
          >
            £{basket.totalCost.toFixed(2)} of £{basket.budget.toFixed(2)}
          </span>
        </div>
        <Button variant="primary" onClick={onReview}>
          Review basket
        </Button>
      </div>
    </div>
  );
}
