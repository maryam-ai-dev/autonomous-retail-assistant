"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { register } from "@/features/auth/services/auth-service";
import { Button } from "@/shared/ui/Button";
import { Input } from "@/shared/ui/Input";

function slugify(input: string): string {
  return input
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function deriveUsername(displayName: string, email: string): string {
  const fromName = slugify(displayName);
  if (fromName) return fromName;
  const localPart = email.split("@")[0] ?? "";
  return slugify(localPart) || "user";
}

function classifyError(message: string): {
  emailError: string | null;
  passwordError: string | null;
  formError: string | null;
} {
  const lower = message.toLowerCase();
  if (lower.includes("email") && (lower.includes("exists") || lower.includes("duplicate") || lower.includes("taken"))) {
    return {
      emailError: "This email is already in use.",
      passwordError: null,
      formError: null,
    };
  }
  if (lower.includes("password") && (lower.includes("weak") || lower.includes("short") || lower.includes("invalid"))) {
    return {
      emailError: null,
      passwordError: message,
      formError: null,
    };
  }
  return { emailError: null, passwordError: null, formError: message };
}

export default function SignupForm() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const username = deriveUsername(displayName, email);
      await register(email, password, username, displayName || username);
      router.push("/onboarding");
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : "Registration failed. Please try again.";
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  const { emailError, passwordError, formError } = error
    ? classifyError(error)
    : { emailError: null, passwordError: null, formError: null };

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="w-full max-w-sm space-y-5 rounded-2xl p-6"
      style={{
        background: "var(--oat)",
        border: "1px solid var(--border)",
      }}
    >
      <h1
        className="text-center text-3xl font-semibold italic"
        style={{
          fontFamily: "var(--font-fraunces)",
          color: "var(--aubergine)",
        }}
      >
        Join Aisleon
      </h1>

      <Input
        label="Email"
        id="signup-email"
        type="email"
        autoComplete="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        error={emailError ?? undefined}
      />

      <Input
        label="Display name"
        id="signup-display-name"
        type="text"
        autoComplete="name"
        required
        value={displayName}
        onChange={(e) => setDisplayName(e.target.value)}
      />

      <Input
        label="Password"
        id="signup-password"
        type="password"
        autoComplete="new-password"
        required
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        error={passwordError ?? undefined}
      />

      {formError ? (
        <p
          role="alert"
          className="text-xs"
          style={{ color: "var(--amber)" }}
        >
          {formError}
        </p>
      ) : null}

      <Button type="submit" variant="primary" fullWidth disabled={loading}>
        {loading ? "Creating account…" : "Create account"}
      </Button>

      <p className="text-center text-sm" style={{ color: "var(--muted)" }}>
        Already have an account?{" "}
        <Link
          href="/login"
          className="font-semibold underline"
          style={{ color: "var(--clay)" }}
        >
          Sign in
        </Link>
      </p>
    </form>
  );
}
