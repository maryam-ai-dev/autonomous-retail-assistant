"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Avatar } from "@/shared/ui/Avatar";
import { BottomSheet } from "@/shared/ui/BottomSheet";
import { Button } from "@/shared/ui/Button";
import { Input } from "@/shared/ui/Input";
import { Toggle } from "@/shared/ui/Toggle";
import { BackButton, PageHeader } from "@/shared/layout/PageHeader";
import apiClient from "@/core/api/client";
import { logout } from "@/features/auth/services/auth-service";
import {
  DEFAULT_NOTIFICATIONS,
  saveNotifications,
  type NotificationPreferences,
} from "@/features/settings/notifications";
import { useCurrentUser } from "@/lib/api/useCurrentUser";

const USE_MOCKS = process.env.NEXT_PUBLIC_USE_MOCKS === "true";

export default function SettingsPage() {
  const { data } = useCurrentUser();
  const [displayName, setDisplayName] = useState<string>(
    data?.displayName ?? "",
  );
  const [savedDisplayName, setSavedDisplayName] = useState<string>(
    data?.displayName ?? "",
  );
  const [isPublic, setIsPublic] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);

  async function saveAccount() {
    setSaving(true);
    setError(null);
    try {
      if (!USE_MOCKS) {
        await apiClient.put(
          "/api/profile/me",
          { displayName },
          { timeout: 10_000 },
        );
      }
      setSavedDisplayName(displayName);
    } catch (err) {
      setDisplayName(savedDisplayName);
      setError(err instanceof Error ? err.message : "Couldn't save");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Settings" leftSlot={<BackButton />} />

      <section className="flex flex-col gap-3">
        <h2
          className="text-sm font-semibold uppercase tracking-wide"
          style={{ color: "var(--muted)" }}
        >
          Account
        </h2>
        <div className="flex items-center gap-3">
          <Avatar
            name={displayName || "You"}
            size="lg"
            variant="ink"
          />
          <div className="flex flex-col">
            <span
              className="text-sm font-semibold"
              style={{ color: "var(--aubergine)" }}
            >
              {displayName || "You"}
            </span>
            <span className="text-xs" style={{ color: "var(--muted)" }}>
              @{data?.handle ?? "you"}
            </span>
          </div>
        </div>
        <Input
          label="Display name"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
        />
        <Input
          label="Email"
          value={"—"}
          readOnly
          aria-readonly
          description="Email changes aren't supported in this build."
        />
        {error ? (
          <p role="alert" className="text-xs" style={{ color: "var(--amber)" }}>
            {error}
          </p>
        ) : null}
        <Button
          variant="primary"
          onClick={saveAccount}
          disabled={saving || displayName === savedDisplayName}
        >
          {saving ? "Saving…" : "Save changes"}
        </Button>
      </section>

      <section className="flex flex-col gap-3">
        <h2
          className="text-sm font-semibold uppercase tracking-wide"
          style={{ color: "var(--muted)" }}
        >
          Taste profile
        </h2>
        <p className="text-sm" style={{ color: "var(--muted)" }}>
          Re-run onboarding to update your priorities, household, dietary
          choices, brands, and values.
        </p>
        <Button
          variant="secondary"
          onClick={() => window.location.assign("/onboarding")}
        >
          Edit taste profile
        </Button>
      </section>

      <section className="flex flex-col gap-3">
        <h2
          className="text-sm font-semibold uppercase tracking-wide"
          style={{ color: "var(--muted)" }}
        >
          Privacy
        </h2>
        <Toggle
          label="Public profile"
          description="Allow anyone to view your profile and shared baskets."
          checked={isPublic}
          onChange={setIsPublic}
        />
      </section>

      <NotificationSection />

      <section className="flex flex-col gap-3">
        <h2
          className="text-sm font-semibold uppercase tracking-wide"
          style={{ color: "var(--amber)" }}
        >
          Danger zone
        </h2>
        <Button variant="ghost" onClick={() => setDeleteOpen(true)}>
          Delete account
        </Button>
      </section>

      <DeleteAccountSheet
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
      />
    </div>
  );
}

function NotificationSection() {
  const [prefs, setPrefs] = useState<NotificationPreferences>(DEFAULT_NOTIFICATIONS);

  const items: {
    key: keyof NotificationPreferences;
    label: string;
    description?: string;
  }[] = [
    { key: "newFollower", label: "New follower" },
    { key: "basketComment", label: "Basket comment" },
    { key: "helpfulReaction", label: "Helpful reaction" },
    {
      key: "weeklyBudgetSummary",
      label: "Weekly budget summary",
      description: "A once-a-week snapshot of your spending",
    },
  ];

  async function handleToggle(
    key: keyof NotificationPreferences,
    next: boolean,
  ) {
    const previous = prefs[key];
    setPrefs((p) => ({ ...p, [key]: next }));
    const result = await saveNotifications({ ...prefs, [key]: next });
    if (!result.ok) {
      setPrefs((p) => ({ ...p, [key]: previous }));
    }
  }

  return (
    <section className="flex flex-col gap-3">
      <h2
        className="text-sm font-semibold uppercase tracking-wide"
        style={{ color: "var(--muted)" }}
      >
        Notifications
      </h2>
      <div className="flex flex-col gap-3">
        {items.map((item) => (
          <Toggle
            key={item.key}
            label={item.label}
            description={item.description}
            checked={prefs[item.key]}
            onChange={(next) => handleToggle(item.key, next)}
          />
        ))}
      </div>
    </section>
  );
}

function DeleteAccountSheet({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const router = useRouter();
  const [confirmation, setConfirmation] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const canDelete = confirmation === "DELETE";

  async function handleDelete() {
    if (!canDelete || submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      if (!USE_MOCKS) {
        await apiClient.delete("/api/profile/me", { timeout: 15_000 });
      }
      logout();
      router.push("/login");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Couldn't delete account");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <BottomSheet open={open} onClose={onClose} title="Delete account">
      <div className="flex flex-col gap-4">
        <p className="text-sm" style={{ color: "var(--muted)" }}>
          This permanently removes your account, baskets, posts, and saves.
          Type <strong>DELETE</strong> to confirm.
        </p>
        <Input
          label="Type DELETE to confirm"
          value={confirmation}
          onChange={(e) => setConfirmation(e.target.value)}
          placeholder="DELETE"
        />
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
            onClick={handleDelete}
            disabled={!canDelete || submitting}
          >
            {submitting ? "Deleting…" : "Delete account"}
          </Button>
        </div>
      </div>
    </BottomSheet>
  );
}
