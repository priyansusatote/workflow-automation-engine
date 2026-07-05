"use client";

import { useEffect } from "react";
import { AlertTriangle, RefreshCw, Home } from "lucide-react";
import Link from "next/link";
import { NeurexLogo } from "@/components/ui/neurex-logo";

export default function DashboardError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("[DashboardError]", error);
  }, [error]);

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] p-8">
      <div
        className="w-full max-w-lg rounded-2xl p-10 text-center"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
          boxShadow: "var(--shadow-lg)",
        }}
      >
        <div
          className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-6"
          style={{ backgroundColor: "var(--neurex-error-subtle)" }}
        >
          <AlertTriangle
            className="w-8 h-8"
            style={{ color: "var(--neurex-error)" }}
          />
        </div>

        <h2
          className="text-xl font-semibold mb-2"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Something went wrong
        </h2>
        <p
          className="text-sm mb-2 leading-relaxed"
          style={{ color: "var(--neurex-text-tertiary)" }}
        >
          An unexpected error occurred while loading this page.
        </p>

        {error.digest && (
          <p
            className="text-xs font-mono mb-6"
            style={{ color: "var(--neurex-text-ghost)" }}
          >
            Error ID: {error.digest}
          </p>
        )}

        {error.message && (
          <div
            className="text-left mb-6 p-3 rounded-lg font-mono text-xs break-all max-h-24 overflow-y-auto"
            style={{
              backgroundColor: "var(--neurex-bg-base)",
              color: "var(--neurex-error)",
              border: "1px solid hsl(0, 84%, 64%, 0.15)",
            }}
          >
            {error.message}
          </div>
        )}

        <div className="flex items-center justify-center gap-3">
          <button
            onClick={reset}
            className="neurex-btn-primary flex items-center gap-2 text-sm"
          >
            <RefreshCw className="w-4 h-4" />
            Try Again
          </button>
          <Link
            href="/dashboard"
            className="neurex-btn-ghost flex items-center gap-2 text-sm"
          >
            <Home className="w-4 h-4" />
            Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
