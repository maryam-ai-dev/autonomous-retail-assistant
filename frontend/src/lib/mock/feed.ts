import type { components } from "@/types/api.generated";

type FeedPage = components["schemas"]["FeedPageDto"];

export const MOCK_FEED_PAGE: FeedPage = {
  posts: [
    {
      id: "po_001",
      type: "PRODUCT",
      authorHandle: "layla",
      authorDisplayName: "Layla",
      caption: "Good halal mince find at Sainsbury's this week.",
      product: {
        retailer: "SAINSBURYS",
        name: "British halal beef mince 500g",
        brand: "Sainsbury's",
        price: 4.75,
        imageUrl: null,
        dietaryTags: ["HALAL_LIKELY"],
      },
      basket: null,
      reactions: [{ type: "TRIED_THIS", count: 4 }],
      commentCount: 2,
      createdAt: "2026-04-12T08:30:00Z",
    },
    {
      id: "po_002",
      type: "BASKET",
      authorHandle: "amira",
      authorDisplayName: "Amira",
      caption: "Weekly £65 halal shop — cut £6 from last week.",
      product: null,
      basket: {
        basketId: "bk_approved_020",
        title: "Halal skincare bundle",
        total: 64.3,
        itemThumbnails: [],
      },
      reactions: [
        { type: "TRIED_THIS", count: 12 },
        { type: "BETTER_ALT", count: 1 },
      ],
      commentCount: 5,
      createdAt: "2026-04-11T19:00:00Z",
    },
  ],
  nextCursor: null,
  hasMore: false,
};
