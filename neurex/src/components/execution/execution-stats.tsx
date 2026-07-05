"use client";

import { motion } from "framer-motion";
import {
  Clock,
  CheckCircle2,
  XCircle,
  Loader2,
  Timer,
  Hash,
  GitBranch,
} from "lucide-react";
import type {
  WorkflowExecutionResponse,
  TaskExecutionResponse,
  ExecutionStatus,
} from "@/types/execution";

function formatDuration(startedAt: string, endedAt?: string | null): string {
  const start = new Date(startedAt).getTime();
  const end = endedAt ? new Date(endedAt).getTime() : Date.now();
  const ms = end - start;
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`;
  return `${Math.floor(ms / 60_000)}m ${Math.floor((ms % 60_000) / 1000)}s`;
}

interface StatCardProps {
  icon: React.ElementType;
  label: string;
  value: string;
  accent?: string;
  delay?: number;
}

function StatCard({ icon: Icon, label, value, accent, delay = 0 }: StatCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, delay }}
      className="flex items-center gap-3 p-3.5 rounded-xl"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
    >
      <div
        className="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0"
        style={{
          backgroundColor: accent
            ? `${accent}18`
            : "var(--neurex-bg-overlay)",
        }}
      >
        <Icon
          className="w-4 h-4"
          style={{
            color: accent || "var(--neurex-text-ghost)",
          }}
        />
      </div>
      <div>
        <p
          className="text-[10px] uppercase tracking-wider font-medium"
          style={{ color: "var(--neurex-text-ghost)" }}
        >
          {label}
        </p>
        <p
          className="text-sm font-semibold font-mono"
          style={{ color: accent || "var(--neurex-text-primary)" }}
        >
          {value}
        </p>
      </div>
    </motion.div>
  );
}

export function ExecutionStatsBar({
  execution,
  tasks,
}: {
  execution: WorkflowExecutionResponse;
  tasks: TaskExecutionResponse[];
}) {
  const completed = tasks.filter((t) => t.status === "SUCCESS").length;
  const failed = tasks.filter((t) => t.status === "FAILED").length;
  const running = tasks.filter((t) => t.status === "RUNNING").length;
  const total = tasks.length;

  const statusColors: Record<ExecutionStatus, string> = {
    RUNNING: "hsl(217, 91%, 60%)",
    SUCCESS: "hsl(152, 69%, 53%)",
    FAILED: "hsl(0, 84%, 64%)",
    WAITING: "hsl(38, 92%, 58%)",
  };

  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
      <StatCard
        icon={Timer}
        label="Duration"
        value={formatDuration(execution.createdAt, execution.updatedAt)}
        accent={statusColors[execution.status]}
        delay={0}
      />
      <StatCard
        icon={Hash}
        label="Tasks"
        value={`${completed}/${total}`}
        accent={total > 0 && completed === total ? statusColors.SUCCESS : undefined}
        delay={0.05}
      />
      <StatCard
        icon={
          failed > 0
            ? XCircle
            : running > 0
            ? Loader2
            : CheckCircle2
        }
        label="Status"
        value={execution.status}
        accent={statusColors[execution.status]}
        delay={0.1}
      />
      <StatCard
        icon={GitBranch}
        label="Workflow"
        value={execution.workflowId?.substring(0, 8) || "—"}
        delay={0.15}
      />
    </div>
  );
}

// ─── Progress Bar ───────────────────────────
export function ExecutionProgressBar({
  tasks,
  isLive,
}: {
  tasks: TaskExecutionResponse[];
  isLive: boolean;
}) {
  if (tasks.length === 0) return null;

  const completed = tasks.filter((t) => t.status === "SUCCESS").length;
  const failed = tasks.filter((t) => t.status === "FAILED").length;
  const total = tasks.length;
  const percentage = Math.round(((completed + failed) / total) * 100);

  return (
    <div className="mb-6">
      <div className="flex items-center justify-between mb-2">
        <span
          className="text-xs font-medium"
          style={{ color: "var(--neurex-text-ghost)" }}
        >
          Pipeline Progress
        </span>
        <span
          className="text-xs font-mono"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          {percentage}%
        </span>
      </div>
      <div
        className="h-1.5 rounded-full overflow-hidden"
        style={{ backgroundColor: "var(--neurex-bg-overlay)" }}
      >
        <motion.div
          className="h-full rounded-full"
          initial={{ width: 0 }}
          animate={{ width: `${percentage}%` }}
          transition={{ duration: 0.6, ease: "easeOut" }}
          style={{
            background:
              failed > 0
                ? "linear-gradient(90deg, var(--neurex-success), var(--neurex-error))"
                : isLive
                ? "linear-gradient(90deg, var(--neurex-accent), var(--neurex-running))"
                : "linear-gradient(90deg, var(--neurex-accent), var(--neurex-success))",
            boxShadow: isLive ? "0 0 8px var(--neurex-accent-glow)" : "none",
          }}
        />
      </div>
    </div>
  );
}
