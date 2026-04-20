import { type CSSProperties } from "react";

type RetailerStyle = { bg: string; fg: string; label: string };

const RETAILER_STYLE: Record<string, RetailerStyle> = {
  TESCO: { bg: "#E8F5E8", fg: "#1A5C1A", label: "Tesco" },
  SAINSBURYS: { bg: "#FFF0DC", fg: "#8A4F10", label: "Sainsbury's" },
  BOOTS: { bg: "#D8E8F5", fg: "#1A3F60", label: "Boots" },
  ARGOS: { bg: "#F5DDD3", fg: "#7A2A14", label: "Argos" },
  ASOS: { bg: "#1C1814", fg: "#FBF5EC", label: "ASOS" },
  ASDA: { bg: "#E8F0FF", fg: "#1A3A8A", label: "Asda" },
  MORRISONS: { bg: "#FFF5E0", fg: "#7A4A00", label: "Morrisons" },
};

const GENERIC = { bg: "var(--oat)", fg: "var(--muted)" };

type RetailerBadgeProps = {
  retailer: string;
  label?: string;
};

export function RetailerBadge({ retailer, label }: RetailerBadgeProps) {
  const known = RETAILER_STYLE[retailer.toUpperCase()];
  const { bg, fg } = known ?? GENERIC;
  const display = label ?? known?.label ?? retailer;
  const style: CSSProperties = { background: bg, color: fg };
  return (
    <span
      className="inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide"
      style={style}
    >
      {display}
    </span>
  );
}
