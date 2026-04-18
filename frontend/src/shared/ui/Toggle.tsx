"use client";

import { type CSSProperties } from "react";

type ToggleProps = {
  label: string;
  checked: boolean;
  onChange: (next: boolean) => void;
  disabled?: boolean;
  description?: string;
};

export function Toggle({
  label,
  checked,
  onChange,
  disabled,
  description,
}: ToggleProps) {
  const trackStyle: CSSProperties = {
    background: checked ? "var(--sage)" : "var(--oat)",
    transition: "background 200ms ease-out",
    border: "1px solid var(--border)",
  };
  const thumbStyle: CSSProperties = {
    transform: `translateX(${checked ? 20 : 0}px)`,
    transition: "transform 180ms ease-out",
    background: "var(--cream)",
  };
  return (
    <div
      className="flex items-start justify-between gap-3"
      style={{ opacity: disabled ? 0.5 : 1 }}
    >
      <span className="flex flex-col">
        <span className="text-sm font-medium" style={{ color: "var(--charcoal)" }}>
          {label}
        </span>
        {description ? (
          <span className="text-xs" style={{ color: "var(--muted)" }}>
            {description}
          </span>
        ) : null}
      </span>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        aria-label={label}
        disabled={disabled}
        onClick={() => onChange(!checked)}
        className="relative inline-flex h-7 w-12 shrink-0 items-center rounded-full focus:outline-none focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--clay)]"
        style={trackStyle}
      >
        <span
          className="ml-0.5 inline-block h-5 w-5 rounded-full shadow"
          style={thumbStyle}
        />
      </button>
    </div>
  );
}
