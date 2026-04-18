"use client";

import { useRouter } from "next/navigation";
import { type ReactNode } from "react";

type PageHeaderProps = {
  title: string;
  leftSlot?: ReactNode;
  rightSlot?: ReactNode;
};

export function PageHeader({ title, leftSlot, rightSlot }: PageHeaderProps) {
  return (
    <header className="flex items-center gap-3 pb-4">
      <div className="flex w-10 items-center">{leftSlot}</div>
      <h1
        className="flex-1 text-center text-xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        {title}
      </h1>
      <div className="flex w-10 items-center justify-end">{rightSlot}</div>
    </header>
  );
}

type BackButtonProps = { onClick?: () => void };

export function BackButton({ onClick }: BackButtonProps) {
  const router = useRouter();
  return (
    <button
      type="button"
      aria-label="Go back"
      onClick={onClick ?? (() => router.back())}
      className="flex h-10 w-10 items-center justify-center rounded-full"
      style={{ color: "var(--aubergine)" }}
    >
      <span aria-hidden="true">←</span>
    </button>
  );
}
