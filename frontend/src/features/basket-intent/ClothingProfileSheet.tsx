"use client";

import { useState } from "react";
import { BottomSheet } from "@/shared/ui/BottomSheet";
import { Button } from "@/shared/ui/Button";
import { Input } from "@/shared/ui/Input";
import {
  saveClothingProfile,
  type ClothingProfilePayload,
} from "./submit";

type ClothingProfileSheetProps = {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
};

export function ClothingProfileSheet({
  open,
  onClose,
  onSaved,
}: ClothingProfileSheetProps) {
  const [profile, setProfile] = useState<ClothingProfilePayload>({
    topSize: "",
    bottomSize: "",
    shoeSize: "",
    fit: "regular",
  });
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function handleSubmit() {
    setSaving(true);
    setError(null);
    const result = await saveClothingProfile(profile);
    setSaving(false);
    if (result.ok) {
      onSaved();
    } else {
      setError(result.message ?? "Could not save — please try again.");
    }
  }

  return (
    <BottomSheet
      open={open}
      onClose={onClose}
      title="Add your clothing profile"
    >
      <div className="flex flex-col gap-4">
        <p className="text-sm" style={{ color: "var(--muted)" }}>
          We need your sizes before building a fashion basket.
        </p>
        <Input
          label="Top size"
          value={profile.topSize}
          onChange={(e) => setProfile({ ...profile, topSize: e.target.value })}
          placeholder="e.g. M or UK 12"
        />
        <Input
          label="Bottom size"
          value={profile.bottomSize}
          onChange={(e) =>
            setProfile({ ...profile, bottomSize: e.target.value })
          }
          placeholder="e.g. 32 or UK 10"
        />
        <Input
          label="Shoe size"
          value={profile.shoeSize}
          onChange={(e) =>
            setProfile({ ...profile, shoeSize: e.target.value })
          }
          placeholder="e.g. UK 7"
        />
        <div className="flex flex-col gap-1.5">
          <label
            htmlFor="clothing-fit"
            className="text-xs font-medium uppercase tracking-wide"
            style={{ color: "var(--muted)" }}
          >
            Fit
          </label>
          <select
            id="clothing-fit"
            value={profile.fit}
            onChange={(e) => setProfile({ ...profile, fit: e.target.value })}
            className="w-full rounded-xl px-4 py-3 text-sm outline-none"
            style={{
              background: "var(--oat)",
              border: "1px solid var(--border)",
              color: "var(--charcoal)",
            }}
          >
            <option value="fitted">Fitted</option>
            <option value="regular">Regular</option>
            <option value="loose">Loose</option>
          </select>
        </div>
        {error ? (
          <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
            {error}
          </p>
        ) : null}
        <div className="flex gap-3">
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <div className="flex-1" />
          <Button
            variant="primary"
            onClick={handleSubmit}
            disabled={
              saving ||
              !profile.topSize.trim() ||
              !profile.bottomSize.trim() ||
              !profile.shoeSize.trim()
            }
          >
            {saving ? "Saving…" : "Save and continue"}
          </Button>
        </div>
      </div>
    </BottomSheet>
  );
}
