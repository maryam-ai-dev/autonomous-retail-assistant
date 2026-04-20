export const PRIORITY_MODES = [
  { key: "price", label: "Best price" },
  { key: "quality", label: "Top quality" },
  { key: "speed", label: "Fastest time" },
  { key: "novelty", label: "Something new" },
] as const;

export type PriorityKey = (typeof PRIORITY_MODES)[number]["key"];

export const HOUSEHOLDS = [
  { key: "solo", label: "Just me" },
  { key: "couple", label: "Couple" },
  { key: "family", label: "Family with kids" },
  { key: "shared", label: "Shared house" },
] as const;

export type HouseholdKey = (typeof HOUSEHOLDS)[number]["key"];

export type DietaryAnswers = {
  halal: boolean;
  vegan: boolean;
  vegetarian: boolean;
  glutenFree: boolean;
  dairyFree: boolean;
  organic: boolean;
};

export type OnboardingAnswers = {
  priority: PriorityKey | null;
  household: HouseholdKey | null;
  dietary: DietaryAnswers;
  budgetQuality: number;
  brands: string[];
  values: string[];
  intentText: string;
};

export const DEFAULT_ANSWERS: OnboardingAnswers = {
  priority: null,
  household: null,
  dietary: {
    halal: false,
    vegan: false,
    vegetarian: false,
    glutenFree: false,
    dairyFree: false,
    organic: false,
  },
  budgetQuality: 50,
  brands: [],
  values: [],
  intentText: "",
};

export function sanitizeAnswers(raw: unknown): OnboardingAnswers {
  const a = (raw ?? {}) as Partial<OnboardingAnswers>;
  const dietary = (a.dietary ?? {}) as Partial<DietaryAnswers>;
  return {
    priority: a.priority ?? null,
    household: a.household ?? null,
    dietary: {
      halal: Boolean(dietary.halal),
      vegan: Boolean(dietary.vegan),
      vegetarian: Boolean(dietary.vegetarian),
      glutenFree: Boolean(dietary.glutenFree),
      dairyFree: Boolean(dietary.dairyFree),
      organic: Boolean(dietary.organic),
    },
    budgetQuality:
      typeof a.budgetQuality === "number" ? a.budgetQuality : 50,
    brands: Array.isArray(a.brands) ? a.brands.filter((b): b is string => typeof b === "string") : [],
    values: Array.isArray(a.values) ? a.values.filter((v): v is string => typeof v === "string") : [],
    intentText: typeof a.intentText === "string" ? a.intentText : "",
  };
}

export const VALUE_CHIPS = [
  { key: "british", label: "British-grown" },
  { key: "fairtrade", label: "Fairtrade" },
  { key: "organic", label: "Certified organic" },
  { key: "cruelty_free", label: "Cruelty-free" },
  { key: "recyclable", label: "Minimal packaging" },
  { key: "local", label: "Local / independent" },
  { key: "seasonal", label: "Seasonal" },
  { key: "plant_based", label: "Plant-based" },
] as const;

export const BRAND_CHIPS = [
  "Boots own",
  "No7",
  "Sanctuary Spa",
  "Superdrug own",
  "Inika Organic",
  "PHB Ethical Beauty",
  "The Body Shop",
  "L'Occitane",
  "ASOS DESIGN",
  "Topshop",
  "New Look",
  "Nike",
  "Adidas",
  "Dr. Martens",
  "Converse",
  "JOHN LEWIS",
  "IKEA",
  "Dyson",
  "Russell Hobbs",
  "Argos own",
] as const;
