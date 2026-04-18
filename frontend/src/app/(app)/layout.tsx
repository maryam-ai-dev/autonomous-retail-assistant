import { AppShell } from "@/shared/layout/AppShell";
import { BottomNav } from "@/shared/layout/BottomNav";

export default function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AppShell>
      <main className="mx-auto w-full max-w-md px-4 pt-6">{children}</main>
      <BottomNav />
    </AppShell>
  );
}
