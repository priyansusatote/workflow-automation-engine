"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "framer-motion";
import { Eye, EyeOff, Loader2, ArrowRight } from "lucide-react";
import { NeurexLogo } from "@/components/ui/neurex-logo";
import Link from "next/link";
import { useSignup } from "@/hooks/use-auth";
import { signupSchema, type SignupFormValues } from "@/lib/validation";
import { PasswordStrength } from "@/components/auth/password-strength";

export default function SignupPage() {
  const [showPassword, setShowPassword] = useState(false);
  const signup = useSignup();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
  });

  const password = watch("password", "");

  const onSubmit = (data: SignupFormValues) => {
    signup.mutate(data);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: [0.25, 0.1, 0.25, 1] }}
    >
      {/* Logo */}
      <div className="flex items-center justify-center gap-3 mb-10">
        <NeurexLogo size={40} />
        <span
          className="text-2xl font-bold tracking-tight"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Neurex
        </span>
      </div>

      {/* Card */}
      <div
        className="p-8 rounded-2xl"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
          boxShadow: "var(--shadow-lg)",
        }}
      >
        <div className="mb-8">
          <h1
            className="text-xl font-semibold mb-2"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Create your account
          </h1>
          <p
            className="text-sm"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            Start building AI-powered workflows
          </p>
        </div>

        {/* Error banner */}
        {signup.isError && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            className="mb-6 p-3 rounded-lg text-sm"
            style={{
              backgroundColor: "var(--neurex-error-subtle)",
              color: "var(--neurex-error)",
              border: "1px solid hsl(0, 84%, 64%, 0.2)",
            }}
          >
            Account creation failed. This email may already be in use.
          </motion.div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          {/* Email */}
          <div>
            <label
              className="block text-xs font-medium mb-2"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Email
            </label>
            <input
              {...register("email")}
              type="email"
              placeholder="you@company.com"
              autoComplete="email"
              className="neurex-input w-full"
              style={{
                borderColor: errors.email
                  ? "var(--neurex-error)"
                  : undefined,
              }}
            />
            {errors.email && (
              <p
                className="mt-1.5 text-xs"
                style={{ color: "var(--neurex-error)" }}
              >
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Password */}
          <div>
            <label
              className="block text-xs font-medium mb-2"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Password
            </label>
            <div className="relative">
              <input
                {...register("password")}
                type={showPassword ? "text" : "password"}
                placeholder="Create a strong password"
                autoComplete="new-password"
                className="neurex-input w-full pr-10"
                style={{
                  borderColor: errors.password
                    ? "var(--neurex-error)"
                    : undefined,
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2"
                style={{ color: "var(--neurex-text-tertiary)" }}
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
            </div>
            {errors.password && (
              <p
                className="mt-1.5 text-xs"
                style={{ color: "var(--neurex-error)" }}
              >
                {errors.password.message}
              </p>
            )}
            <PasswordStrength password={password} />
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={signup.isPending}
            className="neurex-btn-primary w-full flex items-center justify-center gap-2 h-11"
            style={{ opacity: signup.isPending ? 0.7 : 1 }}
          >
            {signup.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <>
                Create account
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Footer */}
        <p
          className="mt-6 text-center text-sm"
          style={{ color: "var(--neurex-text-tertiary)" }}
        >
          Already have an account?{" "}
          <Link
            href="/login"
            className="font-medium transition-colors hover:underline"
            style={{ color: "var(--neurex-accent)" }}
          >
            Sign in
          </Link>
        </p>
      </div>

      {/* Subtle branding */}
      <p
        className="mt-8 text-center text-xs"
        style={{ color: "var(--neurex-text-ghost)" }}
      >
        Neurex — AI-Powered Workflow Orchestration
      </p>
    </motion.div>
  );
}
