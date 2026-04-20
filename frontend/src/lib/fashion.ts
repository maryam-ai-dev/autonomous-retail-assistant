/**
 * Sprint F35.3 — fashion presentation helpers (size badges, style tags).
 * Display-only logic — no persistence.
 */

const SIZE_DEPENDENT_SUBCATEGORIES = new Set([
  "TOPS",
  "BOTTOMS",
  "DRESSES",
  "OUTERWEAR",
  "FOOTWEAR",
  "SPORTSWEAR",
  "UNDERWEAR",
]);

const FASHION_SUBCATEGORIES = new Set([
  ...SIZE_DEPENDENT_SUBCATEGORIES,
  "ACCESSORIES",
]);

export function isFashionSubcategory(subcategory: string | null | undefined): boolean {
  return Boolean(subcategory && FASHION_SUBCATEGORIES.has(subcategory));
}

export function isSizeDependentSubcategory(
  subcategory: string | null | undefined,
): boolean {
  return Boolean(subcategory && SIZE_DEPENDENT_SUBCATEGORIES.has(subcategory));
}

/**
 * Pretty-print the size pill: "UK 6" for footwear, "Size M" for everything
 * else size-dependent. Returns null when there's no badge to render.
 */
export function formatSizeBadge(
  subcategory: string | null | undefined,
  sizeText: string | null | undefined,
): string | null {
  if (!isSizeDependentSubcategory(subcategory)) return null;
  if (!sizeText || !sizeText.trim()) return null;
  if (subcategory === "FOOTWEAR") return `UK ${sizeText.trim()}`;
  return `Size ${sizeText.trim()}`;
}

/**
 * Subcategory → up to two style descriptors. Inferred from the subcategory
 * mix of an all-fashion basket post; deduplicated and capped at 3 chips.
 */
const STYLE_TAGS: Record<string, string[]> = {
  DRESSES: ["occasion", "going out"],
  SPORTSWEAR: ["active", "gym"],
  OUTERWEAR: ["everyday", "layering"],
  FOOTWEAR: ["everyday"],
  TOPS: ["casual"],
  BOTTOMS: ["casual"],
  UNDERWEAR: ["essentials"],
  ACCESSORIES: ["finishing"],
};

export function styleTagsForBasket(subcategoryMix: string[] | undefined): string[] {
  if (!subcategoryMix || subcategoryMix.length === 0) return [];
  const seen = new Set<string>();
  for (const subcat of subcategoryMix) {
    const tags = STYLE_TAGS[subcat] ?? [];
    for (const t of tags) {
      seen.add(t);
      if (seen.size >= 3) break;
    }
    if (seen.size >= 3) break;
  }
  return Array.from(seen).slice(0, 3);
}

/** True when every subcategory in the mix is fashion. Used to gate style tags. */
export function isAllFashion(
  category: string | null | undefined,
  subcategoryMix: string[] | undefined,
): boolean {
  if (category === "FASHION") return true;
  if (!subcategoryMix || subcategoryMix.length === 0) return false;
  return subcategoryMix.every((s) => FASHION_SUBCATEGORIES.has(s));
}
