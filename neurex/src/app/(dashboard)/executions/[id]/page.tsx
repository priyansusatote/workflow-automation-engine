"use client";

import React from "react";
import { motion } from "framer-motion";
import {
  ArrowLeft,
  CheckCircle2,
  XCircle,
  Loader2,
  Clock,
  Play,
  RefreshCw,
  ExternalLink,
  RotateCcw,
} from "lucide-react";
import Link from "next/link";
import { formatDistanceToNow } from "@/lib/utils";
import type { ExecutionStatus } from "@/types/execution";
import { ExecutionDataProvider, useExecutionData } from "@/providers/execution-data-provider";
import { ExecutionTimeline } from "@/components/execution/execution-timeline";
import {
  ExecutionStatsBar,
  ExecutionProgressBar,
} from "@/components/execution/execution-stats";
import { useResumeExecution } from "@/hooks/use-executions";

// ─── Status Config ──────────────────────────
const statusConfig: Record<
  ExecutionStatus,
  { color: string; bg: string; icon: React.ElementType; label: string }
> = {
  RUNNING: { color: "var(--neurex-running)", bg: "var(--neurex-running-subtle)", icon: Loader2, label: "Running" },
  SUCCESS: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)", icon: CheckCircle2, label: "Completed" },
  FAILED: { color: "var(--neurex-error)", bg: "var(--neurex-error-subtle)", icon: XCircle, label: "Failed" },
  WAITING: { color: "var(--neurex-warning)", bg: "var(--neurex-warning-subtle)", icon: Clock, label: "Waiting" },
};

// ─── Inner Content (uses ExecutionDataProvider) ─
function ExecutionDetailContent() {
  const { execution, tasks, isLoading, isLive, refresh } = useExecutionData();
  const resume = useResumeExecution();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-32">
        <Loader2
          className="w-6 h-6 animate-spin"
          style={{ color: "var(--neurex-accent)" }}
        />
      </div>
    );
  }

  if (!execution) {
    return (
      <div className="flex flex-col items-center justify-center py-32">
        <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
          Execution not found
        </p>
        <Link href="/executions" className="neurex-btn-ghost mt-4 text-sm">
          Back to executions
        </Link>
      </div>
    );
  }

  const sc = statusConfig[execution.status];
  const StatusIcon = sc.icon;
  const execIdShort = execution.executionId?.substring(0, 12) ?? "—";

  return (
    <div>
      {/* ── Breadcrumb ── */}
      <Link
        href="/executions"
        className="inline-flex items-center gap-1.5 text-sm mb-6 transition-colors"
        style={{ color: "var(--neurex-text-tertiary)" }}
        onMouseEnter={(e) => {
          e.currentTarget.style.color = "var(--neurex-text-primary)";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.color = "var(--neurex-text-tertiary)";
        }}
      >
        <ArrowLeft className="w-4 h-4" />
        All executions
      </Link>

      {/* ── Header ── */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="flex items-start justify-between mb-6"
      >
        <div className="flex items-center gap-4">
          <div
            className="w-12 h-12 rounded-xl flex items-center justify-center relative"
            style={{
              backgroundColor: sc.bg,
              boxShadow:
                execution.status === "RUNNING"
                  ? "0 0 20px hsl(217, 91%, 60%, 0.3)"
                  : undefined,
            }}
          >
            <StatusIcon
              className={`w-5 h-5 ${execution.status === "RUNNING" ? "animate-spin" : ""}`}
              style={{ color: sc.color }}
            />
            {isLive && (
              <motion.div
                className="absolute inset-0 rounded-xl"
                animate={{ scale: [1, 1.2, 1], opacity: [0.5, 0, 0.5] }}
                transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                style={{ border: `1px solid ${sc.color}` }}
              />
            )}
          </div>
          <div>
            <div className="flex items-center gap-3">
              <h1
                className="text-xl font-bold tracking-tight font-mono"
                style={{ color: "var(--neurex-text-primary)" }}
              >
                {execIdShort}
              </h1>
              <span
                className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
                style={{ backgroundColor: sc.bg, color: sc.color }}
              >
                {isLive && (
                  <span
                    className="w-1.5 h-1.5 rounded-full animate-pulse"
                    style={{ backgroundColor: sc.color }}
                  />
                )}
                {sc.label}
              </span>
            </div>
            <div className="flex items-center gap-4 mt-1">
              <span
                className="text-xs"
                style={{ color: "var(--neurex-text-ghost)" }}
              >
                Started {formatDistanceToNow(execution.createdAt)}
              </span>
              {execution.updatedAt && execution.status !== "RUNNING" && (
                <span
                  className="text-xs"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  Updated {formatDistanceToNow(execution.updatedAt)}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          {isLive && (
            <span
              className="flex items-center gap-1.5 text-xs mr-2"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              <RefreshCw className="w-3 h-3 animate-spin" />
              Live
            </span>
          )}
          {execution.status === "WAITING" && (
            <button
              onClick={() => resume.mutate(execution.executionId)}
              disabled={resume.isPending}
              className="neurex-btn-primary flex items-center gap-2 text-sm"
            >
              {resume.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Play className="w-4 h-4" />
              )}
              Resume
            </button>
          )}
          <button
            onClick={refresh}
            className="neurex-btn-ghost flex items-center gap-2 text-sm"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Refresh
          </button>
          <Link
            href={`/workflows/${execution.workflowId}`}
            className="neurex-btn-ghost flex items-center gap-2 text-sm"
          >
            <ExternalLink className="w-3.5 h-3.5" />
            Workflow
          </Link>
        </div>
      </motion.div>

      {/* ── Error Banner ── */}
      {execution.errorMessage && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="p-4 rounded-xl mb-6"
          style={{
            backgroundColor: "var(--neurex-error-subtle)",
            border: "1px solid hsl(0, 84%, 64%, 0.2)",
          }}
        >
          <p
            className="text-sm font-medium mb-1"
            style={{ color: "var(--neurex-error)" }}
          >
            Execution Error
          </p>
          <pre
            className="text-xs overflow-x-auto"
            style={{
              color: "var(--neurex-text-secondary)",
              fontFamily: "var(--font-mono)",
            }}
          >
            {execution.errorMessage}
          </pre>
        </motion.div>
      )}

      {/* ── Stats Bar ── */}
      <ExecutionStatsBar execution={execution} tasks={tasks || []} />

      {/* ── Progress Bar ── */}
      <ExecutionProgressBar tasks={tasks || []} isLive={isLive} />

      {/* ── Main Content: Details + Timeline ── */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Details Card */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="rounded-xl p-5"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <h3
            className="text-sm font-semibold mb-4"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Execution Details
          </h3>
          <div className="space-y-3">
            {[
              {
                label: "Execution ID",
                value: execIdShort + "...",
              },
              {
                label: "Workflow ID",
                value: (execution.workflowId?.substring(0, 12) ?? "—") + "...",
              },
              { label: "Status", value: execution.status },
              {
                label: "Created",
                value: execution.createdAt
                  ? new Date(execution.createdAt).toLocaleString()
                  : "—",
              },
              {
                label: "Updated",
                value: execution.updatedAt
                  ? new Date(execution.updatedAt).toLocaleString()
                  : "—",
              },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center justify-between">
                <span
                  className="text-xs"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  {label}
                </span>
                <span
                  className="text-xs font-mono text-right max-w-[60%] truncate"
                  style={{ color: "var(--neurex-text-secondary)" }}
                >
                  {value}
                </span>
              </div>
            ))}
          </div>
        </motion.div>

        {/* Task Timeline */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="md:col-span-2 rounded-xl"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <div
            className="flex items-center justify-between px-5 py-4"
            style={{
              borderBottom: "1px solid var(--neurex-border-default)",
            }}
          >
            <h3
              className="text-sm font-semibold"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Execution Pipeline
            </h3>
            {isLive && (
              <span
                className="flex items-center gap-1.5 text-xs"
                style={{ color: "var(--neurex-running)" }}
              >
                <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ backgroundColor: "var(--neurex-running)" }} />
                Auto-refreshing
              </span>
            )}
          </div>
          <div className="p-5">
            <ExecutionTimeline tasks={tasks || []} isLive={isLive} />
          </div>
        </motion.div>
      </div>
    </div>
  );
}

// ─── Page Wrapper with Provider ─────────────
export default function ExecutionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = React.use(params);

  return (
    <ExecutionDataProvider executionId={id}>
      <ExecutionDetailContent />
    </ExecutionDataProvider>
  );
}
