/**
 * STUB — this file is normally produced by `pnpm generate:types` from the
 * backend's OpenAPI document at `http://localhost:8080/api/openapi.json`.
 *
 * It is committed here only because the backend is not yet running. Once the
 * backend's OpenAPI endpoint is reachable, re-run `pnpm generate:types` and the
 * real generated output will replace this file wholesale. Do not treat hand
 * edits here as durable.
 */

export type RetailerKey =
  | "TESCO"
  | "SAINSBURYS"
  | "BOOTS"
  | "ARGOS"
  | "ASDA"
  | "MORRISONS"
  | "OCADO";

export type BasketStatus = "DRAFT" | "APPROVED" | "CHECKED_OUT";

export type DietaryTagEnum =
  | "HALAL_VERIFIED"
  | "HALAL_LIKELY"
  | "HALAL_UNKNOWN"
  | "VEGAN"
  | "VEGETARIAN"
  | "GLUTEN_FREE"
  | "DAIRY_FREE"
  | "ORGANIC_VERIFIED"
  | "ORGANIC_LIKELY"
  | "ORGANIC_UNKNOWN"
  | "PLANT_BASED";

export type ReactionType =
  | "TRIED_THIS"
  | "BETTER_ALT"
  | "WOULDNT_RECOMMEND";

export type SubstitutionFlagReason =
  | "BRAND_CHANGED"
  | "SIZE_CHANGED"
  | "PRICE_INCREASED";

export type CircuitState = "CLOSED" | "OPEN" | "HALF_OPEN";

export interface components {
  schemas: {
    BasketItemDto: {
      id: string;
      productId: string;
      name: string;
      brand?: string | null;
      retailer: RetailerKey;
      imageUrl?: string | null;
      price: number;
      quantity: number;
      dietaryTags: DietaryTagEnum[];
      substitutionFlag?: {
        reason: SubstitutionFlagReason;
        message: string;
      } | null;
      normalizationWarnings: string[];
      whyThis: string;
    };
    BasketDto: {
      id: string;
      status: BasketStatus;
      intentText: string;
      budget: number;
      totalCost: number;
      items: components["schemas"]["BasketItemDto"][];
      retailersUsed: RetailerKey[];
      normalizationWarnings: string[];
      createdAt: string;
      updatedAt: string;
    };
    PostDto: {
      id: string;
      type: "PRODUCT" | "BASKET";
      authorHandle: string;
      authorDisplayName: string;
      caption: string;
      product?: {
        retailer: RetailerKey;
        name: string;
        brand?: string | null;
        price: number;
        imageUrl?: string | null;
        dietaryTags: DietaryTagEnum[];
      } | null;
      basket?: {
        basketId: string;
        title: string;
        total: number;
        itemThumbnails: string[];
        dietarySummary?: {
          hasHalalVerified: boolean;
          hasHalalLikely: boolean;
          hasHalalUnknown: boolean;
        } | null;
        tags?: string[];
      } | null;
      reactions: { type: ReactionType; count: number }[];
      commentCount: number;
      createdAt: string;
    };
    FeedPageDto: {
      posts: components["schemas"]["PostDto"][];
      nextCursor: string | null;
      hasMore: boolean;
    };
    ProfileDto: {
      handle: string;
      displayName: string;
      avatarInitials: string;
      bio?: string | null;
      location?: string | null;
      basketsCount: number;
      followersCount: number;
      followingCount: number;
      isSelf: boolean;
    };
    BudgetSummaryDto: {
      month: string;
      spent: number;
      savedVsFullPrice: number;
      basketsCount: number;
      avgBasket: number;
      byRetailer: Partial<Record<RetailerKey, number>>;
      insights: string[];
    };
    SharedBasketDto: {
      shareId: string;
      basket: components["schemas"]["BasketDto"];
      poster: components["schemas"]["ProfileDto"];
      title: string;
      description?: string | null;
      tags: string[];
      estimatedSaving: number;
    };
    ConnectorStatusDto: {
      retailer: RetailerKey;
      healthy: boolean;
      disabled: boolean;
      circuitState: CircuitState;
      lastSuccessAt: string | null;
      lastFailureAt: string | null;
      lastFailureReason: string | null;
      recentResultCount: number;
      staleCacheUsageCount: number;
      apifyConnector: boolean;
    };
  };
}
