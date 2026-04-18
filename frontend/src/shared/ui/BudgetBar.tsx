import { type CSSProperties } from "react";

type BudgetBarProps = {
  value: number;
  max: number;
  ariaLabel?: string;
};

export function BudgetBar({ value, max, ariaLabel = "Budget spent" }: BudgetBarProps) {
  const safeMax = Math.max(max, 0);
  const clamped = Math.max(0, Math.min(value, safeMax));
  const pct = safeMax === 0 ? 0 : (clamped / safeMax) * 100;
  const over = value > max;
  const fill: CSSProperties = {
    width: `${Math.min(pct, 100)}%`,
    background: over ? "var(--amber)" : "var(--sage)",
    transition: "width 300ms ease-out, background 200ms ease-out",
  };
  return (
    <div
      role="progressbar"
      aria-valuenow={Math.round(clamped)}
      aria-valuemin={0}
      aria-valuemax={Math.round(safeMax)}
      aria-label={ariaLabel}
      className="h-2 w-full overflow-hidden rounded-full"
      style={{ background: "var(--oat)" }}
    >
      <div className="h-full" style={fill} />
    </div>
  );
}
