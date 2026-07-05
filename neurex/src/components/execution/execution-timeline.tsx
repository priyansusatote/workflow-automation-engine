"use client";

import { motion, AnimatePresence } from "framer-motion";
import {
  CheckCircle2,
  XCircle,
  Loader2,
  Clock,
  ChevronDown,
  ChevronRight,
  Terminal,
  FileJson,
  AlertTriangle,
} from "lucide-react";
import { useState } from "react";
import type { TaskExecutionResponse, ExecutionStatus } from "@/types/execution";

// ─── Status Config ──────────────────────────
const statusConfig: Record<
  ExecutionStatus,
  { color: string; bg: string; icon: React.ElementType; label: string; glow?: string }
> = {
  RUNNING: {
    color: "var(--neurex-running)",
    bg: "var(--neurex-running-subtle)",
    icon: Loader2,
    label: "Running",
    glow: "0 0 12px hsl(217, 91%, 60%, 0.4)",
  },
  SUCCESS: {
    color: "var(--neurex-success)",
    bg: "var(--neurex-success-subtle)",
    icon: CheckCircle2,
    label: "Completed",
  },
  FAILED: {
    color: "var(--neurex-error)",
    bg: "var(--neurex-error-subtle)",
    icon: XCircle,
    label: "Failed",
    glow: "0 0 12px hsl(0, 84%, 64%, 0.3)",
  },
  WAITING: {
    color: "var(--neurex-warning)",
    bg: "var(--neurex-warning-subtle)",
    icon: Clock,
    label: "Waiting",
    glow: "0 0 12px hsl(38, 92%, 58%, 0.3)",
  },
};

// ─── Duration helper ────────────────────────
function formatDuration(startedAt: string, endedAt?: string | null): string {
  const start = new Date(startedAt).getTime();
  const end = endedAt ? new Date(endedAt).getTime() : Date.now();
  const ms = end - start;
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`;
  return `${Math.floor(ms / 60_000)}m ${Math.floor((ms % 60_000) / 1000)}s`;
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
}

// ─── Single Task Card ───────────────────────
function TaskCard({ task, index, isLast }: { task: TaskExecutionResponse; index: number; isLast: boolean }) {
  const [expanded, setExpanded] = useState(false);
  const sc = statusConfig[task.status];
  const Icon = sc.icon;
  const hasOutput = task.outputData && Object.keys(task.outputData).length > 0;
  const hasLog = !!task.logMessage;
  const hasDetails = hasOutput || hasLog;

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3, delay: index * 0.06 }}
      className="relative flex gap-4"
    >
      {/* ── Timeline Spine ── */}
      <div className="flex flex-col items-center" style={{ width: 32 }}>
        {/* Node circle */}
        <motion.div
          className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 relative z-10"
          style={{
            backgroundColor: sc.bg,
            border: `2px solid ${sc.color}`,
            boxShadow: sc.glow || "none",
          }}
          animate={
            task.status === "RUNNING"
              ? { scale: [1, 1.1, 1] }
              : {}
          }
          transition={
            task.status === "RUNNING"
              ? { duration: 1.5, repeat: Infinity, ease: "easeInOut" }
              : {}
          }
        >
          <Icon
            className={`w-3.5 h-3.5 ${task.status === "RUNNING" ? "animate-spin" : ""}`}
            style={{ color: sc.color }}
          />
        </motion.div>
        {/* Connecting line */}
        {!isLast && (
          <div
            className="w-px flex-1 min-h-[20px]"
            style={{
              background:
                task.status === "SUCCESS"
                  ? `linear-gradient(180deg, ${sc.color}, var(--neurex-border-default))`
                  : "var(--neurex-border-default)",
            }}
          />
        )}
      </div>

      {/* ── Card Content ── */}
      <div className="flex-1 pb-4 min-w-0">
        <div
          className="rounded-xl overflow-hidden transition-all duration-200"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: `1px solid ${
              task.status === "RUNNING"
                ? sc.color
                : task.status === "FAILED"
                ? "hsl(0, 84%, 64%, 0.3)"
                : "var(--neurex-border-default)"
            }`,
            boxShadow:
              task.status === "RUNNING"
                ? sc.glow!
                : task.status === "FAILED"
                ? sc.glow!
                : "none",
          }}
        >
          {/* Header */}
          <button
            onClick={() => hasDetails && setExpanded(!expanded)}
            className="w-full flex items-center gap-3 p-3.5 text-left transition-colors"
            style={{ cursor: hasDetails ? "pointer" : "default" }}
            onMouseEnter={(e) => {
              if (hasDetails) e.currentTarget.style.backgroundColor = "var(--neurex-bg-overlay)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "transparent";
            }}
          >
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span
                  className="text-sm font-semibold"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {task.nodeId}
                </span>
                <span
                  className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded font-medium"
                  style={{
                    backgroundColor: "var(--neurex-bg-overlay)",
                    color: "var(--neurex-text-ghost)",
                  }}
                >
                  {task.nodeType}
                </span>
              </div>
              <div className="flex items-center gap-3 mt-1">
                <span
                  className="text-xs"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  {formatTime(task.createdAt)}
                </span>
                {task.status === "SUCCESS" && (
                  <span
                    className="text-xs font-mono"
                    style={{ color: "var(--neurex-text-tertiary)" }}
                  >
                    {formatDuration(task.createdAt)}
                  </span>
                )}
              </div>
            </div>

            {/* Status pill */}
            <span
              className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium flex-shrink-0"
              style={{ backgroundColor: sc.bg, color: sc.color }}
            >
              {task.status === "RUNNING" && (
                <span
                  className="w-1.5 h-1.5 rounded-full animate-pulse"
                  style={{ backgroundColor: sc.color }}
                />
              )}
              {sc.label}
            </span>

            {/* Expand chevron */}
            {hasDetails && (
              <div style={{ color: "var(--neurex-text-ghost)" }}>
                {expanded ? (
                  <ChevronDown className="w-4 h-4" />
                ) : (
                  <ChevronRight className="w-4 h-4" />
                )}
              </div>
            )}
          </button>

          {/* Expanded detail section */}
          <AnimatePresence>
            {expanded && hasDetails && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="overflow-hidden"
              >
                <div
                  className="px-3.5 pb-3.5 space-y-3"
                  style={{ borderTop: "1px solid var(--neurex-border-default)" }}
                >
                  <div className="pt-3" />
                  {/* Log message */}
                  {hasLog && (
                    <div>
                      <div className="flex items-center gap-1.5 mb-1.5">
                        <Terminal
                          className="w-3 h-3"
                          style={{ color: "var(--neurex-text-ghost)" }}
                        />
                        <span
                          className="text-xs font-medium"
                          style={{ color: "var(--neurex-text-ghost)" }}
                        >
                          Log Output
                        </span>
                      </div>
                      <pre
                        className="text-xs p-3 rounded-lg overflow-x-auto leading-relaxed"
                        style={{
                          backgroundColor: "var(--neurex-bg-base)",
                          color:
                            task.status === "FAILED"
                              ? "var(--neurex-error)"
                              : "var(--neurex-text-secondary)",
                          fontFamily: "var(--font-mono)",
                          border: "1px solid var(--neurex-border-default)",
                        }}
                      >
                        {task.logMessage}
                      </pre>
                    </div>
                  )}

                  {/* Output data */}
                  {hasOutput && (
                    <div>
                      <div className="flex items-center gap-1.5 mb-1.5">
                        <FileJson
                          className="w-3 h-3"
                          style={{ color: "var(--neurex-text-ghost)" }}
                        />
                        <span
                          className="text-xs font-medium"
                          style={{ color: "var(--neurex-text-ghost)" }}
                        >
                          Output Data
                        </span>
                      </div>
                      <pre
                        className="text-xs p-3 rounded-lg overflow-x-auto leading-relaxed"
                        style={{
                          backgroundColor: "var(--neurex-bg-base)",
                          color: "var(--neurex-text-secondary)",
                          fontFamily: "var(--font-mono)",
                          border: "1px solid var(--neurex-border-default)",
                          maxHeight: 300,
                        }}
                      >
                        {JSON.stringify(task.outputData, null, 2)}
                      </pre>
                    </div>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </motion.div>
  );
}

// ─── Execution Timeline ─────────────────────
export function ExecutionTimeline({
  tasks,
  isLive,
}: {
  tasks: TaskExecutionResponse[];
  isLive: boolean;
}) {
  if (tasks.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        {isLive ? (
          <>
            <motion.div
              animate={{ scale: [1, 1.1, 1] }}
              transition={{ duration: 1.5, repeat: Infinity }}
            >
              <Loader2
                className="w-6 h-6 animate-spin mb-3"
                style={{ color: "var(--neurex-running)" }}
              />
            </motion.div>
            <p
              className="text-sm"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Waiting for task data...
            </p>
          </>
        ) : (
          <>
            <AlertTriangle
              className="w-6 h-6 mb-3"
              style={{ color: "var(--neurex-text-ghost)" }}
            />
            <p
              className="text-sm"
              style={{ color: "var(--neurex-text-tertiary)" }}
            >
              No task execution data available
            </p>
          </>
        )}
      </div>
    );
  }

  return (
    <div className="space-y-0">
      {tasks.map((task, i) => (
        <TaskCard
          key={task.nodeId ?? `task-${i}`}
          task={task}
          index={i}
          isLast={i === tasks.length - 1}
        />
      ))}
    </div>
  );
}
