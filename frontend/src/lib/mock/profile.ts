import type { components } from "@/types/api.generated";

type Profile = components["schemas"]["ProfileDto"];

export const MOCK_PROFILE: Profile = {
  handle: "layla",
  displayName: "Layla",
  avatarInitials: "L",
  bio: "Weekly shopper, budget-hunter, halal-first.",
  location: "Leeds, UK",
  basketsCount: 12,
  followersCount: 34,
  followingCount: 18,
  isSelf: false,
};
