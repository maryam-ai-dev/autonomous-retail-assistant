"use client";

import { useEffect, useState } from "react";
import { Button } from "@/shared/ui/Button";
import { RetailerBadge } from "@/shared/ui/RetailerBadge";
import type { components } from "@/types/api.generated";
import {
  fetchCheckoutLinks,
  resolveCheckoutUrl,
  shareBasket,
} from "./checkout";

type Basket = components["schemas"]["BasketDto"];

type CheckoutActionsProps = {
  basket: Basket;
};

export function CheckoutActions({ basket }: CheckoutActionsProps) {
  const [links, setLinks] = useState<Record<string, string>>({});
  const [shareState, setShareState] = useState<
    | { kind: "idle" }
    | { kind: "sharing" }
    | { kind: "shared"; copied: boolean; url: string }
    | { kind: "error"; message: string }
  >({ kind: "idle" });

  useEffect(() => {
    let cancelled = false;
    fetchCheckoutLinks(basket.id).then((result) => {
      if (!cancelled) setLinks(result);
    });
    return () => {
      cancelled = true;
    };
  }, [basket.id]);

  const uniqueRetailers = Array.from(
    new Set((basket.items ?? []).map((item) => item.retailer)),
  );

  async function handleShare() {
    setShareState({ kind: "sharing" });
    const result = await shareBasket(basket.id);
    if (!result) {
      setShareState({ kind: "error", message: "Couldn't share just now." });
      return;
    }
    const copied = await copyToClipboard(result.shareUrl);
    setShareState({ kind: "shared", copied, url: result.shareUrl });
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-col gap-2">
        <h2
          className="text-lg font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          Checkout
        </h2>
        <p className="text-xs" style={{ color: "var(--muted)" }}>
          Tap a retailer to open their site in a new tab.
        </p>
      </div>
      <div className="flex flex-col gap-2">
        {uniqueRetailers.map((retailer) => {
          const url = resolveCheckoutUrl(retailer, links);
          return (
            <a
              key={retailer}
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-between rounded-2xl p-4"
              style={{
                background: "var(--oat)",
                border: "1px solid var(--border)",
                minHeight: 44,
              }}
            >
              <span className="flex items-center gap-2">
                <RetailerBadge retailer={retailer} />
                <span
                  className="text-sm font-semibold"
                  style={{ color: "var(--aubergine)" }}
                >
                  Continue to {retailer.toLowerCase()}
                </span>
              </span>
              <span
                aria-hidden="true"
                className="text-sm"
                style={{ color: "var(--clay)" }}
              >
                ↗
              </span>
            </a>
          );
        })}
      </div>
      <div className="flex flex-col gap-2">
        <Button
          variant="secondary"
          fullWidth
          onClick={handleShare}
          disabled={shareState.kind === "sharing"}
        >
          {shareState.kind === "sharing" ? "Sharing…" : "Share basket"}
        </Button>
        {shareState.kind === "shared" ? (
          <p
            role="status"
            className="text-xs"
            style={{ color: "var(--muted)" }}
          >
            {shareState.copied
              ? "Link copied to clipboard."
              : `Copy this link: ${shareState.url}`}
          </p>
        ) : null}
        {shareState.kind === "error" ? (
          <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
            {shareState.message}
          </p>
        ) : null}
      </div>
    </div>
  );
}

async function copyToClipboard(value: string): Promise<boolean> {
  try {
    if (typeof navigator !== "undefined" && navigator.clipboard) {
      await navigator.clipboard.writeText(value);
      return true;
    }
  } catch {
    // fall through
  }
  return false;
}
