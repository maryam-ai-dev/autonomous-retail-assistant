"use client";

import {
  forwardRef,
  type ButtonHTMLAttributes,
  type CSSProperties,
} from "react";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  fullWidth?: boolean;
};

const VARIANT: Record<ButtonVariant, CSSProperties> = {
  primary: {
    background: "var(--clay)",
    color: "var(--cream)",
    border: "1px solid var(--clay)",
  },
  secondary: {
    background: "var(--oat)",
    color: "var(--aubergine)",
    border: "1px solid var(--border)",
  },
  ghost: {
    background: "transparent",
    color: "var(--aubergine)",
    border: "1px solid transparent",
  },
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  {
    variant = "primary",
    fullWidth,
    className = "",
    style,
    disabled,
    children,
    ...rest
  },
  ref,
) {
  const merged: CSSProperties = {
    ...VARIANT[variant],
    minHeight: 44,
    minWidth: 44,
    opacity: disabled ? 0.5 : 1,
    cursor: disabled ? "not-allowed" : "pointer",
    ...style,
  };
  const classes = [
    "inline-flex items-center justify-center gap-2 rounded-full px-5 text-sm font-semibold",
    "transition-[transform,opacity] duration-150 ease-out",
    "focus:outline-none focus-visible:outline-2 focus-visible:outline-offset-2",
    "focus-visible:outline-[var(--clay)]",
    fullWidth ? "w-full" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");
  return (
    <button
      ref={ref}
      disabled={disabled}
      aria-disabled={disabled || undefined}
      className={classes}
      style={merged}
      {...rest}
    >
      {children}
    </button>
  );
});
