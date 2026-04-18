"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/shared/ui/Button";

const STORAGE_KEY = "aisleon_onboarding_state";
const TOTAL_STEPS = 7;
const SKIP_STEP = 6;

type OnboardingState = {
  step: number;
  answers: Record<string, unknown>;
};

function readFromStorage(): OnboardingState | null {
  try {
    if (typeof window === "undefined") return null;
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<OnboardingState>;
    if (typeof parsed?.step === "number" && parsed.step >= 1 && parsed.step <= TOTAL_STEPS) {
      return {
        step: parsed.step,
        answers: (parsed.answers as Record<string, unknown>) ?? {},
      };
    }
    return null;
  } catch {
    return null;
  }
}

function writeToStorage(state: OnboardingState): void {
  try {
    if (typeof window === "undefined") return;
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    // sessionStorage unavailable — memory only
  }
}

function clearStorage(): void {
  try {
    if (typeof window === "undefined") return;
    window.sessionStorage.removeItem(STORAGE_KEY);
  } catch {
    // ignore
  }
}

export default function OnboardingPage() {
  const router = useRouter();
  const [state, setState] = useState<OnboardingState>({ step: 1, answers: {} });

  useEffect(() => {
    const restored = readFromStorage();
    if (restored) setState(restored);
  }, []);

  useEffect(() => {
    writeToStorage(state);
  }, [state]);

  const goBack = useCallback(() => {
    setState((prev) => ({ ...prev, step: Math.max(1, prev.step - 1) }));
  }, []);

  const goNext = useCallback(() => {
    setState((prev) =>
      prev.step >= TOTAL_STEPS
        ? prev
        : { ...prev, step: prev.step + 1 },
    );
  }, []);

  const skipStep = useCallback(() => {
    setState((prev) => ({ ...prev, step: Math.min(TOTAL_STEPS, prev.step + 1) }));
  }, []);

  const onComplete = useCallback(() => {
    clearStorage();
    router.push("/home");
  }, [router]);

  const isLast = state.step === TOTAL_STEPS;
  const canSkip = state.step === SKIP_STEP;

  return (
    <div className="mx-auto flex min-h-[70vh] max-w-md flex-col gap-6 py-6">
      <div
        role="progressbar"
        aria-valuenow={state.step}
        aria-valuemin={1}
        aria-valuemax={TOTAL_STEPS}
        aria-label={`Step ${state.step} of ${TOTAL_STEPS}`}
        className="flex items-center justify-center gap-1.5"
      >
        {Array.from({ length: TOTAL_STEPS }, (_, idx) => {
          const stepNumber = idx + 1;
          const active = stepNumber <= state.step;
          return (
            <span
              key={stepNumber}
              aria-hidden="true"
              className="inline-block h-2.5 w-2.5 rounded-full"
              style={{
                background: active ? "var(--clay)" : "transparent",
                border: `1px solid ${active ? "var(--clay)" : "var(--border)"}`,
              }}
            />
          );
        })}
      </div>

      <section
        className="flex flex-1 flex-col gap-4 rounded-2xl p-6"
        style={{ background: "var(--oat)", border: "1px solid var(--border)" }}
        aria-labelledby="onboarding-step-heading"
      >
        <h2
          id="onboarding-step-heading"
          className="text-center text-2xl font-semibold italic"
          style={{
            fontFamily: "var(--font-fraunces)",
            color: "var(--aubergine)",
          }}
        >
          Step {state.step}
        </h2>
        <p className="text-sm" style={{ color: "var(--muted)" }}>
          Step content will appear here.
        </p>
      </section>

      <div className="flex items-center gap-3">
        <Button
          variant="ghost"
          onClick={goBack}
          disabled={state.step === 1}
          aria-label="Back"
        >
          Back
        </Button>
        <div className="flex-1" />
        {canSkip ? (
          <Button variant="secondary" onClick={skipStep} aria-label="Skip step">
            Skip
          </Button>
        ) : null}
        <Button
          variant="primary"
          onClick={isLast ? onComplete : goNext}
          aria-label={isLast ? "Finish" : "Next"}
        >
          {isLast ? "Finish" : "Next"}
        </Button>
      </div>
    </div>
  );
}
