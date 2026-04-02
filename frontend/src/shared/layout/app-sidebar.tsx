"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { logout } from "@/features/auth/services/auth-service";

const NAV_ITEMS = [
  { href: "/search", label: "Search" },
  { href: "/recommendations", label: "Recommendations" },
  { href: "/cart", label: "Cart" },
  { href: "/approvals", label: "Approvals" },
  { href: "/trust", label: "Trust Center" },
  { href: "/audit-log", label: "Audit Log" },
  { href: "/preferences", label: "Preferences" },
  { href: "/profile", label: "Profile" },
  { href: "/robotics-simulation", label: "Robotics" },
];

export default function AppSidebar() {
  const pathname = usePathname();

  return (
    <aside className="flex h-full w-60 flex-col border-r border-gray-200 bg-gray-50">
      <div className="px-4 py-5">
        <h2 className="text-lg font-bold">Aisleon</h2>
      </div>

      <nav className="flex-1 space-y-1 px-2">
        {NAV_ITEMS.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`block rounded px-3 py-2 text-sm font-medium ${
                isActive
                  ? "bg-blue-100 text-blue-700"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-gray-200 p-4">
        <button
          onClick={logout}
          className="w-full rounded px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
        >
          Log out
        </button>
      </div>
    </aside>
  );
}
