import { type ReactNode } from "react";

type EmptyStateProps = {
  title?: string;
  message: string;
  action?: ReactNode;
};

export function EmptyState({ title, message, action }: EmptyStateProps) {
  return (
    <div
      className="flex flex-col items-center gap-2 py-8 text-center"
      role="status"
    >
      {title ? (
        <h3
          className="text-base font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          {title}
        </h3>
      ) : null}
      <p className="text-sm italic" style={{ color: "var(--muted)" }}>
        {message}
      </p>
      {action ?? null}
    </div>
  );
}

type ErrorCardProps = {
  title?: string;
  message: string;
  onRetry?: () => void;
  retryLabel?: string;
};

export function ErrorCard({
  title = "Something went wrong",
  message,
  onRetry,
  retryLabel = "Try again",
}: ErrorCardProps) {
  return (
    <div
      role="alert"
      className="flex flex-col gap-3 rounded-2xl p-4"
      style={{
        background: "var(--amber-light)",
        border: "1px solid var(--amber)",
        color: "#6B2A11",
      }}
    >
      <div className="flex flex-col gap-1">
        <strong className="text-sm">{title}</strong>
        <p className="text-sm">{message}</p>
      </div>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="self-start rounded-full px-4 py-2 text-xs font-semibold"
          style={{
            background: "var(--cream)",
            border: "1px solid #6B2A11",
            color: "#6B2A11",
            minHeight: 44,
          }}
        >
          {retryLabel}
        </button>
      ) : null}
    </div>
  );
}

type SkeletonRowProps = {
  count?: number;
  height?: number;
};

export function SkeletonRows({ count = 2, height = 64 }: SkeletonRowProps) {
  return (
    <div
      role="status"
      aria-live="polite"
      className="flex flex-col gap-2"
    >
      {Array.from({ length: count }).map((_, idx) => (
        <div
          key={idx}
          className="rounded-xl"
          style={{
            background: "var(--oat)",
            height,
          }}
        />
      ))}
      <span className="sr-only">Loading…</span>
    </div>
  );
}
