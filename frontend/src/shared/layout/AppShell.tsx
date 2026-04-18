import { type ReactNode } from "react";

type AppShellProps = { children: ReactNode };

export function AppShell({ children }: AppShellProps) {
  return (
    <div
      className="min-h-screen pb-16"
      style={{ background: "var(--cream)" }}
    >
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-full focus:px-4 focus:py-2 focus:text-sm focus:font-semibold"
        style={{
          background: "var(--clay)",
          color: "var(--cream)",
          outline: "2px solid var(--clay)",
          outlineOffset: 2,
        }}
      >
        Skip to content
      </a>
      <div id="main-content">{children}</div>
    </div>
  );
}
