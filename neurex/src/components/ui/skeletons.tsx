"use client";

import { cn } from "@/lib/utils";

/** Base skeleton with shimmer animation */
function Skeleton({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "rounded-lg animate-pulse",
        className
      )}
      style={{ backgroundColor: "var(--neurex-bg-subtle)" }}
      {...props}
    />
  );
}

/* ─────────────────────────────────────────────
   Dashboard Skeleton
   ───────────────────────────────────────────── */
export function DashboardSkeleton() {
  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="space-y-2">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-4 w-72" />
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div
            key={i}
            className="rounded-xl p-5"
            style={{
              backgroundColor: "var(--neurex-bg-elevated)",
              border: "1px solid var(--neurex-border-default)",
            }}
          >
            <Skeleton className="h-8 w-8 rounded-lg mb-4" />
            <Skeleton className="h-8 w-16 mb-2" />
            <Skeleton className="h-3 w-24" />
          </div>
        ))}
      </div>

      {/* Two-column grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {[1, 2].map((col) => (
          <div
            key={col}
            className="rounded-xl p-5 space-y-4"
            style={{
              backgroundColor: "var(--neurex-bg-elevated)",
              border: "1px solid var(--neurex-border-default)",
            }}
          >
            <div className="flex items-center justify-between">
              <Skeleton className="h-5 w-36" />
              <Skeleton className="h-4 w-16" />
            </div>
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="flex items-center gap-3">
                <Skeleton className="h-9 w-9 rounded-lg flex-shrink-0" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-4 w-3/4" />
                  <Skeleton className="h-3 w-1/2" />
                </div>
                <Skeleton className="h-5 w-16 rounded-full" />
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Workflow List Skeleton
   ───────────────────────────────────────────── */
export function WorkflowListSkeleton() {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <Skeleton className="h-8 w-40" />
          <Skeleton className="h-4 w-64" />
        </div>
        <Skeleton className="h-10 w-36 rounded-xl" />
      </div>

      {/* Search bar */}
      <Skeleton className="h-10 w-full max-w-sm rounded-xl" />

      {/* Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {Array.from({ length: 6 }).map((_, i) => (
          <div
            key={i}
            className="rounded-xl p-5 space-y-4"
            style={{
              backgroundColor: "var(--neurex-bg-elevated)",
              border: "1px solid var(--neurex-border-default)",
            }}
          >
            <div className="flex items-center gap-3">
              <Skeleton className="h-10 w-10 rounded-lg flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-3/4" />
                <Skeleton className="h-3 w-1/2" />
              </div>
            </div>
            <Skeleton className="h-3 w-full" />
            <div className="flex items-center justify-between">
              <Skeleton className="h-5 w-16 rounded-full" />
              <Skeleton className="h-3 w-20" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Execution List Skeleton
   ───────────────────────────────────────────── */
export function ExecutionListSkeleton() {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="space-y-2">
        <Skeleton className="h-8 w-40" />
        <Skeleton className="h-4 w-56" />
      </div>

      {/* Table header */}
      <div
        className="rounded-xl overflow-hidden"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
        }}
      >
        <div
          className="flex items-center gap-4 px-5 py-3"
          style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
        >
          {["w-32", "w-40", "w-20", "w-24", "w-28"].map((w, i) => (
            <Skeleton key={i} className={`h-3 ${w}`} />
          ))}
        </div>
        {/* Rows */}
        {Array.from({ length: 5 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-4 px-5 py-4"
            style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
          >
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-4 w-40" />
            <Skeleton className="h-5 w-20 rounded-full" />
            <Skeleton className="h-4 w-24" />
            <Skeleton className="h-4 w-28" />
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Workflow Detail Skeleton
   ───────────────────────────────────────────── */
export function WorkflowDetailSkeleton() {
  return (
    <div className="space-y-6">
      {/* Back link */}
      <Skeleton className="h-4 w-32" />

      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Skeleton className="h-12 w-12 rounded-xl" />
          <div className="space-y-2">
            <Skeleton className="h-6 w-56" />
            <Skeleton className="h-4 w-36" />
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Skeleton className="h-9 w-28 rounded-xl" />
          <Skeleton className="h-9 w-24 rounded-xl" />
        </div>
      </div>

      {/* Description */}
      <Skeleton className="h-16 w-full rounded-xl" />

      {/* Section */}
      <div
        className="rounded-xl p-5 space-y-4"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
        }}
      >
        <Skeleton className="h-5 w-40 mb-4" />
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="flex items-center gap-3">
            <Skeleton className="h-8 w-8 rounded-lg" />
            <div className="flex-1 space-y-1.5">
              <Skeleton className="h-4 w-48" />
              <Skeleton className="h-3 w-32" />
            </div>
            <Skeleton className="h-5 w-16 rounded-full" />
          </div>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   Generic Page Skeleton
   ───────────────────────────────────────────── */
export function PageSkeleton() {
  return (
    <div className="space-y-6">
      <Skeleton className="h-8 w-48" />
      <Skeleton className="h-4 w-80" />
      <div className="space-y-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-20 w-full rounded-xl" />
        ))}
      </div>
    </div>
  );
}

export { Skeleton };
