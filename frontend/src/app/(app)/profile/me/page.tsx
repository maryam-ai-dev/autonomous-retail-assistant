"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useCurrentUser } from "@/lib/api/useCurrentUser";

export default function MyProfileRedirect() {
  const router = useRouter();
  const { data, isLoading, error } = useCurrentUser();

  useEffect(() => {
    if (isLoading) return;
    if (data?.handle) {
      router.replace(`/profile/${encodeURIComponent(data.handle)}`);
    } else if (error) {
      router.replace("/login?returnTo=/profile/me");
    }
  }, [data, isLoading, error, router]);

  return (
    <div className="flex items-center justify-center py-10">
      <span
        className="text-sm"
        style={{ color: "var(--muted)" }}
      >
        Taking you to your profile…
      </span>
    </div>
  );
}
