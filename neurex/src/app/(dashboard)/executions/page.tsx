"use client";

import { useState } from "react";
import { motion } from "framer-motion";
import {
  CheckCircle2,
  XCircle,
  Loader2,
  Clock,
  ChevronLeft,
  ChevronRight,
  Play,
  Search,
} from "lucide-react";
import Link from "next/link";
import { useExecutions } from "@/hooks/use-executions";
import { formatDistanceToNow } from "@/lib/utils";
import type { ExecutionStatus } from "@/types/execution";
import { ExecutionListSkeleton } from "@/components/ui/skeletons";

const statusConfig: Record<
  ExecutionStatus,
  { color: string; bg: string; icon: React.ElementType }
> = {
  RUNNING: { color: "var(--neurex-running)", bg: "var(--neurex-running-subtle)", icon: Loader2 },
  SUCCESS: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)", icon: CheckCircle2 },
  FAILED: { color: "var(--neurex-error)", bg: "var(--neurex-error-subtle)", icon: XCircle },
  WAITING: { color: "var(--neurex-warning)", bg: "var(--neurex-warning-subtle)", icon: Clock },
};

export default function ExecutionsPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<ExecutionStatus | "ALL">("ALL");
  const { data, isLoading } = useExecutions(page, 15);

  const filtered =
    statusFilter === "ALL"
      ? data?.content
      : data?.content?.filter((e) => e.status === statusFilter);

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1
          className="text-2xl font-bold tracking-tight"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Executions
        </h1>
        <p className="text-sm mt-1" style={{ color: "var(--neurex-text-secondary)" }}>
          Monitor workflow execution history and status
        </p>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-2 mb-6 flex-wrap">
        {(["ALL", "RUNNING", "SUCCESS", "FAILED", "WAITING"] as const).map((status) => {
          const isActive = statusFilter === status;
          return (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className="px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
              style={{
                backgroundColor: isActive
                  ? status === "ALL"
                    ? "var(--neurex-accent-subtle)"
                    : statusConfig[status as ExecutionStatus]?.bg
                  : "var(--neurex-bg-elevated)",
                color: isActive
                  ? status === "ALL"
                    ? "var(--neurex-accent)"
                    : statusConfig[status as ExecutionStatus]?.color
                  : "var(--neurex-text-tertiary)",
                border: `1px solid ${isActive ? "transparent" : "var(--neurex-border-default)"}`,
              }}
            >
              {status}
            </button>
          );
        })}
      </div>

      {/* Table */}
      {isLoading ? (
        <ExecutionListSkeleton />
      ) : filtered && filtered.length > 0 ? (
        <div
          className="rounded-xl overflow-hidden"
          style={{
            background: "var(--glass-bg)",
            backdropFilter: "blur(var(--glass-blur))",
            WebkitBackdropFilter: "blur(var(--glass-blur))",
            border: "1px solid var(--glass-border)",
            borderTop: "1px solid var(--glass-border-top)",
            boxShadow: "0 8px 32px rgba(0, 0, 0, 0.2), var(--glass-inner-glow)",
          }}
        >
          {/* Header row */}
          <div
            className="grid grid-cols-12 gap-4 px-5 py-3 text-xs font-medium uppercase tracking-wider"
            style={{
              color: "var(--neurex-text-ghost)",
              borderBottom: "1px solid var(--glass-border)",
              letterSpacing: "0.05em",
            }}
          >
            <div className="col-span-3">Execution ID</div>
            <div className="col-span-3">Workflow</div>
            <div className="col-span-2">Status</div>
            <div className="col-span-2">Started</div>
            <div className="col-span-2">Updated</div>
          </div>

          {/* Data rows */}
          {filtered.filter((e) => e.executionId).map((exec, i) => {
            const sc = statusConfig[exec.status];
            const Icon = sc.icon;
            const rowBg = exec.status === "RUNNING" ? "var(--neurex-row-running)"
              : exec.status === "FAILED" ? "var(--neurex-row-failed)"
              : exec.status === "SUCCESS" ? "var(--neurex-row-success)"
              : "transparent";
            const borderColor = sc.color;
            return (
              <Link
                key={exec.executionId ?? `exec-${i}`}
                href={`/executions/${exec.executionId}`}
                className="grid grid-cols-12 gap-4 px-5 py-3.5 items-center transition-all duration-200 relative"
                style={{
                  borderBottom: "1px solid var(--glass-border)",
                  borderLeft: `3px solid ${borderColor}`,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = rowBg;
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = "transparent";
                }}
              >
                <div className="col-span-3 flex items-center gap-2 min-w-0">
                  <div
                    className="w-7 h-7 rounded-md flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: sc.bg }}
                  >
                    <Icon
                      className={`w-3.5 h-3.5 ${exec.status === "RUNNING" ? "animate-spin" : ""}`}
                      style={{ color: sc.color }}
                    />
                  </div>
                  <span
                    className="text-sm font-mono truncate"
                    style={{ color: "var(--neurex-text-primary)" }}
                  >
                    {exec.executionId?.substring(0, 8) ?? "—"}...
                  </span>
                </div>
                <div className="col-span-3 text-sm truncate" style={{ color: "var(--neurex-text-secondary)" }}>
                  {exec.workflowName || exec.workflowId?.substring(0, 8)}
                </div>
                <div className="col-span-2">
                  <span
                    className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium ${exec.status === "RUNNING" ? "animate-running-pulse" : ""}`}
                    style={{ backgroundColor: sc.bg, color: sc.color }}
                  >
                    {exec.status === "RUNNING" && (
                      <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ backgroundColor: sc.color }} />
                    )}
                    {exec.status}
                  </span>
                </div>
                <div className="col-span-2 text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
                  {formatDistanceToNow(exec.createdAt)}
                </div>
                <div className="col-span-2 text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
                  {formatDistanceToNow(exec.updatedAt)}
                </div>
              </Link>
            );
          })}
        </div>
      ) : (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center justify-center py-20 rounded-xl"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <div
            className="w-14 h-14 rounded-2xl flex items-center justify-center mb-5"
            style={{ backgroundColor: "var(--neurex-running-subtle)" }}
          >
            <Play className="w-6 h-6" style={{ color: "var(--neurex-running)" }} />
          </div>
          <h3
            className="text-lg font-semibold mb-2"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            No executions yet
          </h3>
          <p className="text-sm" style={{ color: "var(--neurex-text-secondary)" }}>
            Execute a workflow to see results here
          </p>
        </motion.div>
      )}

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between mt-4">
          <p className="text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
            Page {data.number + 1} of {data.totalPages} · {data.totalElements} total
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={data.first}
              className="neurex-btn-ghost p-2"
              style={{ opacity: data.first ? 0.3 : 1 }}
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={data.last}
              className="neurex-btn-ghost p-2"
              style={{ opacity: data.last ? 0.3 : 1 }}
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
