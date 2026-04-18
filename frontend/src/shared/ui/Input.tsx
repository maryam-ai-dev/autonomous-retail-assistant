"use client";

import {
  forwardRef,
  useId,
  type CSSProperties,
  type InputHTMLAttributes,
} from "react";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
  description?: string;
};

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, error, description, id, className = "", style, ...rest },
  ref,
) {
  const autoId = useId();
  const inputId = id ?? autoId;
  const descId = description ? `${inputId}-desc` : undefined;
  const errorId = error ? `${inputId}-err` : undefined;
  const describedBy = [descId, errorId].filter(Boolean).join(" ") || undefined;
  const inputStyle: CSSProperties = {
    background: "var(--oat)",
    border: `1px solid ${error ? "var(--amber)" : "var(--border)"}`,
    color: "var(--charcoal)",
    ...style,
  };
  const classes = [
    "w-full rounded-xl px-4 py-3 text-sm outline-none",
    "focus-visible:ring-2 focus-visible:ring-[var(--clay)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--cream)]",
    className,
  ]
    .filter(Boolean)
    .join(" ");
  return (
    <div className="flex flex-col gap-1.5">
      <label
        htmlFor={inputId}
        className="text-xs font-medium uppercase tracking-wide"
        style={{ color: "var(--muted)" }}
      >
        {label}
      </label>
      {description ? (
        <p id={descId} className="text-sm" style={{ color: "var(--muted)" }}>
          {description}
        </p>
      ) : null}
      <input
        ref={ref}
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        className={classes}
        style={inputStyle}
        {...rest}
      />
      {error ? (
        <p id={errorId} role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
          {error}
        </p>
      ) : null}
    </div>
  );
});
