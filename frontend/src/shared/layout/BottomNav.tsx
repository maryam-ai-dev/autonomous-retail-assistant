"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { type CSSProperties } from "react";

type NavItem = {
  label: string;
  href: string;
  match: (pathname: string) => boolean;
};

const NAV: NavItem[] = [
  {
    label: "Home",
    href: "/home",
    match: (p) => p === "/" || p.startsWith("/home") || p.startsWith("/basket"),
  },
  {
    label: "Social",
    href: "/social",
    match: (p) => p.startsWith("/social"),
  },
  {
    label: "Budget",
    href: "/budget",
    match: (p) => p.startsWith("/budget"),
  },
  {
    label: "You",
    href: "/profile/me",
    match: (p) => p.startsWith("/profile") || p.startsWith("/settings"),
  },
];

export function BottomNav() {
  const pathname = usePathname() ?? "";
  return (
    <nav
      role="navigation"
      aria-label="Main navigation"
      className="fixed inset-x-0 bottom-0 z-40"
      style={{
        background: "var(--cream)",
        borderTop: "1px solid var(--border)",
      }}
    >
      <ul className="mx-auto flex w-full max-w-md items-stretch justify-around">
        {NAV.map((item) => {
          const active = item.match(pathname);
          const style: CSSProperties = {
            color: active ? "var(--clay)" : "var(--muted)",
            minHeight: 44,
            minWidth: 44,
          };
          return (
            <li key={item.href} className="flex-1">
              <Link
                href={item.href}
                aria-current={active ? "page" : undefined}
                className="flex h-full flex-col items-center justify-center gap-0.5 py-3 text-[11px] font-semibold uppercase tracking-wide"
                style={style}
              >
                <span>{item.label}</span>
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
