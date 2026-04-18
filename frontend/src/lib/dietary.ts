export type DietaryTag =
  | "HALAL_VERIFIED"
  | "HALAL_LIKELY"
  | "HALAL_UNKNOWN"
  | "ORGANIC_VERIFIED"
  | "ORGANIC_LIKELY"
  | "ORGANIC_UNKNOWN";

export type DietaryColour = "sage" | "amber" | "muted";

export type DietaryUI = { label: string; colour: DietaryColour };

export const DIETARY_UI: Record<DietaryTag, DietaryUI> = {
  HALAL_VERIFIED: { label: "Verified halal", colour: "sage" },
  HALAL_LIKELY: { label: "Likely suitable — please verify", colour: "amber" },
  HALAL_UNKNOWN: { label: "Unknown — check manually", colour: "muted" },
  ORGANIC_VERIFIED: { label: "Certified organic", colour: "sage" },
  ORGANIC_LIKELY: { label: "Likely organic — please verify", colour: "amber" },
  ORGANIC_UNKNOWN: { label: "Organic status unknown", colour: "muted" },
};
