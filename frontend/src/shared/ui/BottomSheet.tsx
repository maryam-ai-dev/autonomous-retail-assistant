"use client";

import {
  useCallback,
  useEffect,
  useId,
  useRef,
  type KeyboardEvent,
  type ReactNode,
} from "react";
import { createPortal } from "react-dom";

type BottomSheetProps = {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
};

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

function focusableIn(root: HTMLElement): HTMLElement[] {
  return Array.from(root.querySelectorAll<HTMLElement>(FOCUSABLE));
}

export function BottomSheet({ open, onClose, title, children }: BottomSheetProps) {
  const id = useId();
  const sheetRef = useRef<HTMLDivElement | null>(null);
  const returnFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!open) return;
    returnFocusRef.current = document.activeElement as HTMLElement | null;
    const node = sheetRef.current;
    if (node) {
      const [first] = focusableIn(node);
      first?.focus();
    }
    return () => {
      returnFocusRef.current?.focus?.();
    };
  }, [open]);

  const onKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (!open) return;
      if (e.key === "Escape") {
        e.stopPropagation();
        onClose();
        return;
      }
      if (e.key === "Tab" && sheetRef.current) {
        const items = focusableIn(sheetRef.current);
        if (items.length === 0) {
          e.preventDefault();
          return;
        }
        const first = items[0]!;
        const last = items[items.length - 1]!;
        const active = document.activeElement;
        if (e.shiftKey && active === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && active === last) {
          e.preventDefault();
          first.focus();
        }
      }
    },
    [onClose, open],
  );

  if (!open || typeof document === "undefined") return null;

  return createPortal(
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={`${id}-title`}
      className="fixed inset-0 z-50 flex items-end justify-center"
      onKeyDown={onKeyDown}
    >
      <button
        type="button"
        aria-label="Close"
        onClick={onClose}
        className="absolute inset-0 h-full w-full"
        style={{ background: "rgba(28,24,20,0.35)" }}
      />
      <div
        ref={sheetRef}
        className="relative w-full max-w-md rounded-t-3xl p-6 shadow-lg"
        style={{ background: "var(--cream)", borderTop: "1px solid var(--border)" }}
      >
        <h2
          id={`${id}-title`}
          className="mb-4 text-xl font-semibold"
          style={{ color: "var(--aubergine)" }}
        >
          {title}
        </h2>
        {children}
      </div>
    </div>,
    document.body,
  );
}
