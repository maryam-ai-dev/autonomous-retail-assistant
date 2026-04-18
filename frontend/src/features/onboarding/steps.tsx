"use client";

import { DietaryCertaintyBadge } from "@/shared/ui/DietaryCertaintyBadge";
import { Toggle } from "@/shared/ui/Toggle";
import {
  BRAND_CHIPS,
  HOUSEHOLDS,
  PRIORITY_MODES,
  type DietaryAnswers,
  type HouseholdKey,
  type PriorityKey,
} from "./types";

type ValueStepProps<T> = {
  value: T;
  onChange: (next: T) => void;
};

export function PriorityModeStep({
  value,
  onChange,
}: ValueStepProps<PriorityKey | null>) {
  return (
    <div className="grid grid-cols-2 gap-3" role="radiogroup" aria-label="Priority mode">
      {PRIORITY_MODES.map((mode) => {
        const selected = value === mode.key;
        return (
          <button
            key={mode.key}
            type="button"
            role="radio"
            aria-checked={selected}
            onClick={() => onChange(mode.key)}
            className="flex h-24 items-center justify-center rounded-2xl p-3 text-sm font-medium"
            style={{
              background: selected ? "var(--clay-light)" : "var(--cream)",
              border: `2px solid ${selected ? "var(--clay)" : "var(--border)"}`,
              color: "var(--aubergine)",
              minHeight: 44,
            }}
          >
            {mode.label}
          </button>
        );
      })}
    </div>
  );
}

export function HouseholdStep({
  value,
  onChange,
}: ValueStepProps<HouseholdKey | null>) {
  return (
    <div className="flex flex-col gap-3" role="radiogroup" aria-label="Household">
      {HOUSEHOLDS.map((h) => {
        const selected = value === h.key;
        return (
          <button
            key={h.key}
            type="button"
            role="radio"
            aria-checked={selected}
            onClick={() => onChange(h.key)}
            className="flex items-center justify-between rounded-2xl p-4 text-left"
            style={{
              background: selected ? "var(--clay-light)" : "var(--cream)",
              border: `2px solid ${selected ? "var(--clay)" : "var(--border)"}`,
              color: "var(--aubergine)",
              minHeight: 44,
            }}
          >
            <span className="text-base font-medium">{h.label}</span>
            {selected ? <span aria-hidden="true">✓</span> : null}
          </button>
        );
      })}
    </div>
  );
}

const DIETARY_ITEMS: { key: keyof DietaryAnswers; label: string; description?: string }[] = [
  {
    key: "halal",
    label: "Halal",
    description:
      "We'll show you which items are verified, likely, or unknown",
  },
  { key: "vegan", label: "Vegan" },
  { key: "vegetarian", label: "Vegetarian" },
  { key: "glutenFree", label: "Gluten free" },
  { key: "dairyFree", label: "Dairy free" },
  { key: "organic", label: "Organic" },
];

export function DietaryStep({
  value,
  onChange,
}: ValueStepProps<DietaryAnswers>) {
  return (
    <div className="flex flex-col gap-4">
      {DIETARY_ITEMS.map((item) => (
        <div key={item.key} className="flex flex-col gap-2">
          <Toggle
            label={item.label}
            description={item.description}
            checked={value[item.key]}
            onChange={(next) => onChange({ ...value, [item.key]: next })}
          />
          {item.key === "halal" && value.halal ? (
            <div className="flex flex-wrap gap-2 pl-1">
              <DietaryCertaintyBadge tag="HALAL_VERIFIED" />
              <DietaryCertaintyBadge tag="HALAL_LIKELY" />
              <DietaryCertaintyBadge tag="HALAL_UNKNOWN" />
            </div>
          ) : null}
        </div>
      ))}
    </div>
  );
}

export function BudgetQualityStep({
  value,
  onChange,
}: ValueStepProps<number>) {
  return (
    <div className="flex flex-col gap-3">
      <input
        type="range"
        min={0}
        max={100}
        step={1}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        aria-label="Budget versus quality preference"
        className="w-full"
        style={{ accentColor: "var(--clay)" }}
      />
      <div
        className="grid grid-cols-2 gap-2 text-xs"
        style={{ color: "var(--muted)" }}
      >
        <span>Always hunting for the best deal</span>
        <span className="text-right">Happy to pay more for something I trust</span>
      </div>
    </div>
  );
}

export function BrandsStep({
  value,
  onChange,
}: ValueStepProps<string[]>) {
  const toggleBrand = (brand: string) => {
    const on = value.includes(brand);
    onChange(on ? value.filter((b) => b !== brand) : [...value, brand]);
  };
  return (
    <div className="flex flex-wrap gap-2" role="group" aria-label="Brand preferences">
      {BRAND_CHIPS.map((brand) => {
        const on = value.includes(brand);
        return (
          <button
            key={brand}
            type="button"
            aria-pressed={on}
            onClick={() => toggleBrand(brand)}
            className="inline-flex items-center rounded-full px-3 py-2 text-xs font-medium"
            style={{
              background: on ? "var(--clay-light)" : "var(--cream)",
              border: `1px solid ${on ? "var(--clay)" : "var(--border)"}`,
              color: on ? "var(--clay)" : "var(--aubergine)",
              minHeight: 36,
            }}
          >
            {brand}
          </button>
        );
      })}
    </div>
  );
}
