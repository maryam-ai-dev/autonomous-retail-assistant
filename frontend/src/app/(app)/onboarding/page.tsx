"use client";

import { useCallback, useEffect, useState, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/shared/ui/Button";
import {
  BrandsStep,
  BudgetQualityStep,
  DietaryStep,
  HouseholdStep,
  PriorityModeStep,
} from "@/features/onboarding/steps";
import {
  DEFAULT_ANSWERS,
  sanitizeAnswers,
  type OnboardingAnswers,
} from "@/features/onboarding/types";

const STORAGE_KEY = "aisleon_onboarding_state";
const TOTAL_STEPS = 7;
const SKIP_STEP = 6;

type OnboardingState = {
  step: number;
  answers: OnboardingAnswers;
};

const DEFAULT_STATE: OnboardingState = {
  step: 1,
  answers: DEFAULT_ANSWERS,
};

function readFromStorage(): OnboardingState | null {
  try {
    if (typeof window === "undefined") return null;
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<OnboardingState>;
    const step =
      typeof parsed?.step === "number" && parsed.step >= 1 && parsed.step <= TOTAL_STEPS
        ? parsed.step
        : 1;
    return {
      step,
      answers: sanitizeAnswers(parsed?.answers),
    };
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

const STEP_TITLES: Record<number, string> = {
  1: "What matters most?",
  2: "Who are you shopping for?",
  3: "Any dietary preferences?",
  4: "Deal-hunter or trust-buyer?",
  5: "Any brands you love?",
  6: "Any values you care about?",
  7: "What's your first basket?",
};

export default function OnboardingPage() {
  const router = useRouter();
  const [state, setState] = useState<OnboardingState>(DEFAULT_STATE);

  useEffect(() => {
    const restored = readFromStorage();
    if (restored) setState(restored);
  }, []);

  useEffect(() => {
    writeToStorage(state);
  }, [state]);

  const updateAnswers = useCallback(
    <K extends keyof OnboardingAnswers>(key: K, value: OnboardingAnswers[K]) => {
      setState((prev) => ({
        ...prev,
        answers: { ...prev.answers, [key]: value },
      }));
    },
    [],
  );

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

  const stepContent: ReactNode = (() => {
    switch (state.step) {
      case 1:
        return (
          <PriorityModeStep
            value={state.answers.priority}
            onChange={(next) => updateAnswers("priority", next)}
          />
        );
      case 2:
        return (
          <HouseholdStep
            value={state.answers.household}
            onChange={(next) => updateAnswers("household", next)}
          />
        );
      case 3:
        return (
          <DietaryStep
            value={state.answers.dietary}
            onChange={(next) => updateAnswers("dietary", next)}
          />
        );
      case 4:
        return (
          <BudgetQualityStep
            value={state.answers.budgetQuality}
            onChange={(next) => updateAnswers("budgetQuality", next)}
          />
        );
      case 5:
        return (
          <BrandsStep
            value={state.answers.brands}
            onChange={(next) => updateAnswers("brands", next)}
          />
        );
      default:
        return (
          <p className="text-sm" style={{ color: "var(--muted)" }}>
            Step content will appear here.
          </p>
        );
    }
  })();

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
          {STEP_TITLES[state.step] ?? `Step ${state.step}`}
        </h2>
        {stepContent}
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
