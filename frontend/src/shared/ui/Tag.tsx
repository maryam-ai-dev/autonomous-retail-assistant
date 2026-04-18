import { type CSSProperties, type ReactNode } from "react";

type TagVariant = "sage" | "amber" | "clay" | "ink";

const VARIANT: Record<TagVariant, { bg: string; fg: string }> = {
  sage: { bg: "var(--sage-light)", fg: "#35502B" },
  amber: { bg: "var(--amber-light)", fg: "#6B2A11" },
  clay: { bg: "var(--clay-light)", fg: "#5A2310" },
  ink: { bg: "var(--ink-light)", fg: "#1A2E47" },
};

type TagProps = {
  variant?: TagVariant;
  children: ReactNode;
};

export function Tag({ variant = "clay", children }: TagProps) {
  const { bg, fg } = VARIANT[variant];
  const style: CSSProperties = { background: bg, color: fg };
  return (
    <span
      className="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium tracking-wide"
      style={style}
    >
      {children}
    </span>
  );
}
