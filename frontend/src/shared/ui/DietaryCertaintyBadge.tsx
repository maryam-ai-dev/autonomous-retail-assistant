import { type CSSProperties } from "react";

export type DietaryTag =
  | "HALAL_VERIFIED"
  | "HALAL_LIKELY"
  | "HALAL_UNKNOWN"
  | "ORGANIC_VERIFIED"
  | "ORGANIC_LIKELY"
  | "ORGANIC_UNKNOWN";

type DietaryColour = "sage" | "amber" | "muted";

type DietaryUI = { label: string; colour: DietaryColour };

export const DIETARY_UI: Record<DietaryTag, DietaryUI> = {
  HALAL_VERIFIED: { label: "Verified halal", colour: "sage" },
  HALAL_LIKELY: { label: "Likely suitable — please verify", colour: "amber" },
  HALAL_UNKNOWN: { label: "Unknown — check manually", colour: "muted" },
  ORGANIC_VERIFIED: { label: "Certified organic", colour: "sage" },
  ORGANIC_LIKELY: { label: "Likely organic — please verify", colour: "amber" },
  ORGANIC_UNKNOWN: { label: "Organic status unknown", colour: "muted" },
};

const COLOUR_STYLE: Record<DietaryColour, CSSProperties> = {
  sage: { background: "var(--sage-light)", color: "#35502B" },
  amber: { background: "var(--amber-light)", color: "#6B2A11" },
  muted: { background: "var(--oat)", color: "var(--muted)" },
};

type DietaryCertaintyBadgeProps = {
  tag: DietaryTag;
};

export function DietaryCertaintyBadge({ tag }: DietaryCertaintyBadgeProps) {
  const ui = DIETARY_UI[tag];
  if (!ui) return null;
  return (
    <span
      className="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium"
      style={COLOUR_STYLE[ui.colour]}
    >
      {ui.label}
    </span>
  );
}
