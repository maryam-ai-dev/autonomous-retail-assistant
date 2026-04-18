import type { components } from "@/types/api.generated";
import { DIETARY_UI, type DietaryTag, type DietaryUI } from "@/lib/dietary";

type BasketDto = components["schemas"]["BasketDto"];
type BasketItemDto = components["schemas"]["BasketItemDto"];

export type BasketItemViewModel = {
  id: string;
  name: string;
  brand: string;
  retailer: BasketItemDto["retailer"];
  imageUrl: string | null;
  price: number;
  quantity: number;
  whyThis: string;
  dietaryBadges: DietaryUI[];
  substitutionFlag: BasketItemDto["substitutionFlag"];
  normalizationWarnings: string[];
};

export type BasketViewModel = {
  id: string;
  status: BasketDto["status"];
  intentText: string;
  budget: number;
  totalCost: number;
  items: BasketItemViewModel[];
  retailersUsed: BasketDto["retailersUsed"];
  normalizationWarnings: string[];
  createdAt: string;
  updatedAt: string;
  hasUnresolvedFlags: boolean;
  overBudget: boolean;
};

function dietaryBadgesFor(tags: BasketItemDto["dietaryTags"]): DietaryUI[] {
  return tags
    .filter((tag): tag is DietaryTag => tag in DIETARY_UI)
    .map((tag) => DIETARY_UI[tag]);
}

export function toBasketItemViewModel(item: BasketItemDto): BasketItemViewModel {
  return {
    id: item.id,
    name: item.name,
    brand: item.brand ?? "",
    retailer: item.retailer,
    imageUrl: item.imageUrl ?? null,
    price: item.price,
    quantity: item.quantity,
    whyThis: item.whyThis,
    dietaryBadges: dietaryBadgesFor(item.dietaryTags),
    substitutionFlag: item.substitutionFlag ?? null,
    normalizationWarnings: item.normalizationWarnings,
  };
}

export function toBasketViewModel(basket: BasketDto): BasketViewModel {
  const items = basket.items.map(toBasketItemViewModel);
  return {
    id: basket.id,
    status: basket.status,
    intentText: basket.intentText,
    budget: basket.budget,
    totalCost: basket.totalCost,
    items,
    retailersUsed: basket.retailersUsed,
    normalizationWarnings: basket.normalizationWarnings,
    createdAt: basket.createdAt,
    updatedAt: basket.updatedAt,
    hasUnresolvedFlags: items.some((item) => item.substitutionFlag !== null),
    overBudget: basket.totalCost > basket.budget,
  };
}
