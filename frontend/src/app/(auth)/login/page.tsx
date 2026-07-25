"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AxiosError } from "axios";

import { login } from "@/lib/api/auth-actions";
import {
  Field,
  FieldLabel,
  FieldError,
  FieldGroup,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const loginSchema = z.object({
  email: z
    .string()
    .min(1, "Email is required")
    .email("Enter a valid email address"),
  password: z.string().min(1, "Password is required"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  async function onSubmit(values: LoginFormValues) {
    setServerError(null);
    try {
      await login(values);
      router.push("/");
    } catch (err) {
      if (err instanceof AxiosError && err.response?.data?.message) {
        // Backend's ErrorResponse.message is already human-readable —
        // e.g. "The email address or password you entered is incorrect."
        setServerError(err.response.data.message);
      } else {
        setServerError("Something went wrong. Please try again.");
      }
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="w-full max-w-[400px]">
        {/* ── Brand mark ─────────────────────────────────────── */}
        <div className="flex flex-col items-center mb-8">
          <div className="h-10 w-10 rounded-md bg-primary flex items-center justify-center mb-3">
            <span className="text-primary-foreground font-bold text-lg">P</span>
          </div>
          <h1 className="text-xl font-semibold text-text-primary">
            Sign in to your account
          </h1>
          <p className="text-sm text-text-secondary mt-1">
            Manage your properties, tenants, and leases
          </p>
        </div>

        {/* ── Login card ──────────────────────────────────────── */}
        <div className="bg-surface border border-border rounded-lg shadow-sm p-6">
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <FieldGroup>
              {serverError && (
                <div
                  role="alert"
                  className="rounded-md bg-danger-tint border border-danger/20 px-3 py-2 text-sm text-danger"
                >
                  {serverError}
                </div>
              )}

              <Field data-invalid={!!errors.email}>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  placeholder="you@company.com"
                  aria-invalid={!!errors.email}
                  {...register("email")}
                />
                <FieldError errors={errors.email ? [errors.email] : undefined} />
              </Field>

              <Field data-invalid={!!errors.password}>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  placeholder="••••••••"
                  aria-invalid={!!errors.password}
                  {...register("password")}
                />
                <FieldError errors={errors.password ? [errors.password] : undefined} />
              </Field>

              <Button type="submit" className="w-full mt-2" disabled={isSubmitting}>
                {isSubmitting ? "Signing in..." : "Sign in"}
              </Button>
            </FieldGroup>
          </form>
        </div>

        <p className="text-center text-xs text-text-muted mt-6">
          Don&apos;t have an account?{" "}
          <a href="#" className="text-text-link hover:underline">
            Register your organization
          </a>
        </p>
      </div>
    </div>
  );
}