"use client";

import { Suspense, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "framer-motion";
import { Eye, EyeOff, Loader2, ArrowRight } from "lucide-react";
import { NeurexLogo } from "@/components/ui/neurex-logo";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useLogin } from "@/hooks/use-auth";
import { loginSchema, type LoginFormValues } from "@/lib/validation";

function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const searchParams = useSearchParams();
  const registered = searchParams.get("registered");
  const login = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormValues) => {
    login.mutate(data);
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
            Welcome back
          </h1>
          <p
            className="text-sm"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            Sign in to your Neurex account
          </p>
        </div>

        {/* Registration success banner */}
        {registered && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            className="mb-6 p-3 rounded-lg text-sm"
            style={{
              backgroundColor: "var(--neurex-success-subtle)",
              color: "var(--neurex-success)",
              border: "1px solid hsl(152, 69%, 53%, 0.2)",
            }}
          >
            Account created successfully. Sign in to continue.
          </motion.div>
        )}

        {/* Error banner */}
        {login.isError && (
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
            Invalid email or password. Please try again.
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
                placeholder="••••••••"
                autoComplete="current-password"
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
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={login.isPending}
            className="neurex-btn-primary w-full flex items-center justify-center gap-2 h-11"
            style={{ opacity: login.isPending ? 0.7 : 1 }}
          >
            {login.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <>
                Sign in
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
          Don&apos;t have an account?{" "}
          <Link
            href="/signup"
            className="font-medium transition-colors hover:underline"
            style={{ color: "var(--neurex-accent)" }}
          >
            Create account
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

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
