export type DietaryTag =
  | "HALAL_VERIFIED"
  | "HALAL_LIKELY"
  | "HALAL_UNKNOWN"
  | "ORGANIC_VERIFIED"
  | "ORGANIC_LIKELY"
  | "ORGANIC_UNKNOWN";

export type DietaryColour = "sage" | "amber" | "muted";

export type DietaryUI = { label: string; colour: DietaryColour; tooltip: string };

// Sprint B12.4: tooltip copy reframed for non-food scope (cosmetics,
// haircare, fragrance, supplements). Food halal is out of scope — owned by
// NourishOS.
export const DIETARY_UI: Record<DietaryTag, DietaryUI> = {
  HALAL_VERIFIED: {
    label: "Verified halal",
    colour: "sage",
    tooltip:
      "This brand or product holds a recognised halal certification.",
  },
  HALAL_LIKELY: {
    label: "Likely halal — please verify",
    colour: "amber",
    tooltip:
      "The brand appears on our curated halal-cosmetics list. Double-check the current product if you need certainty.",
  },
  HALAL_UNKNOWN: {
    label: "Halal status unknown — check manually",
    colour: "muted",
    tooltip:
      "Halal status could not be verified. For cosmetics and supplements, check the ingredients list for alcohol or animal-derived ingredients.",
  },
  ORGANIC_VERIFIED: {
    label: "Certified organic",
    colour: "sage",
    tooltip: "Carries a recognised organic certification.",
  },
  ORGANIC_LIKELY: {
    label: "Likely organic — please verify",
    colour: "amber",
    tooltip: "Brand-level evidence only; check the product itself if this matters.",
  },
  ORGANIC_UNKNOWN: {
    label: "Organic status unknown",
    colour: "muted",
    tooltip: "No reliable organic signal on this product.",
  },
};
