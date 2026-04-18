"use client";

import { useState } from "react";
import { Button } from "@/shared/ui/Button";
import { setFollowState } from "./follow";

type FollowButtonProps = {
  handle: string;
  initialFollowing?: boolean;
  isSelf: boolean;
};

export function FollowButton({
  handle,
  initialFollowing = false,
  isSelf,
}: FollowButtonProps) {
  const [following, setFollowing] = useState(initialFollowing);
  const [busy, setBusy] = useState(false);

  if (isSelf) return null;

  async function handleClick() {
    if (busy) return;
    const next = !following;
    setFollowing(next);
    setBusy(true);
    const result = await setFollowState(handle, next);
    if (!result.ok) setFollowing(!next);
    setBusy(false);
  }

  return (
    <Button
      variant={following ? "secondary" : "primary"}
      onClick={handleClick}
      disabled={busy}
      aria-pressed={following}
    >
      {following ? "Following" : "Follow"}
    </Button>
  );
}
