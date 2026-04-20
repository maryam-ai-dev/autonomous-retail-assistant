import { type CSSProperties } from "react";
import { DIETARY_UI, type DietaryColour, type DietaryTag } from "@/lib/dietary";

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
      title={ui.tooltip}
    >
      {ui.label}
    </span>
  );
}
