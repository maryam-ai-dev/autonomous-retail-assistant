import { type CSSProperties } from "react";

type AvatarSize = "sm" | "md" | "lg";
type AvatarVariant = "clay" | "sage" | "ink" | "amber";

type AvatarProps = {
  name: string;
  size?: AvatarSize;
  variant?: AvatarVariant;
};

const SIZE: Record<AvatarSize, { dimension: number; fontSize: number }> = {
  sm: { dimension: 32, fontSize: 13 },
  md: { dimension: 40, fontSize: 15 },
  lg: { dimension: 52, fontSize: 18 },
};

const VARIANT: Record<AvatarVariant, { bg: string; fg: string }> = {
  clay: { bg: "var(--clay)", fg: "var(--cream)" },
  sage: { bg: "var(--sage)", fg: "var(--cream)" },
  ink: { bg: "var(--ink)", fg: "var(--cream)" },
  amber: { bg: "var(--amber)", fg: "var(--cream)" },
};

function initialsFor(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return (parts[0]![0]! + parts[parts.length - 1]![0]!).toUpperCase();
}

export function Avatar({ name, size = "md", variant = "clay" }: AvatarProps) {
  const { dimension, fontSize } = SIZE[size];
  const { bg, fg } = VARIANT[variant];
  const style: CSSProperties = {
    width: dimension,
    height: dimension,
    fontSize,
    background: bg,
    color: fg,
  };
  return (
    <span
      aria-label={`${name}'s avatar`}
      className="inline-flex items-center justify-center rounded-full font-semibold select-none"
      style={style}
    >
      {initialsFor(name)}
    </span>
  );
}
