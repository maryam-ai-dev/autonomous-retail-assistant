"use client";

import { Button } from "@/shared/ui/Button";
import type { components } from "@/types/api.generated";

type Basket = components["schemas"]["BasketDto"];

type BasketApproveBarProps = {
  basket: Basket;
  onApprove?: () => void;
  approving?: boolean;
};

export function BasketApproveBar({
  basket,
  onApprove,
  approving,
}: BasketApproveBarProps) {
  const unresolvedCount = (basket.items ?? []).filter(
    (item) => item.substitutionFlag !== null && item.substitutionFlag !== undefined,
  ).length;
  const blocked = unresolvedCount > 0;
  const label = blocked
    ? `Resolve ${unresolvedCount} item${unresolvedCount > 1 ? "s" : ""} to approve`
    : approving
      ? "Approving…"
      : "Approve basket";

  return (
    <div
      className="sticky bottom-20 z-30 flex flex-col gap-1 rounded-2xl p-4"
      style={{
        background: "var(--cream)",
        border: "1px solid var(--border)",
        boxShadow: "0 -1px 8px rgba(28,24,20,0.05)",
      }}
    >
      <Button
        variant="primary"
        fullWidth
        disabled={blocked || approving}
        onClick={() => {
          if (!blocked && onApprove) onApprove();
        }}
      >
        {label}
      </Button>
      {blocked ? (
        <p
          role="status"
          className="text-center text-xs"
          style={{ color: "var(--muted)" }}
        >
          Swap or accept flagged items before approving.
        </p>
      ) : null}
    </div>
  );
}
