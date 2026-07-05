"use client";

import { motion } from "framer-motion";
import {
  Workflow,
  Play,
  CheckCircle2,
  XCircle,
  Clock,
  TrendingUp,
  BarChart3,
  ArrowRight,
  Loader2,
  AlertTriangle,
  Zap,
  Plus,
} from "lucide-react";
import Link from "next/link";
import { useDashboardStats } from "@/hooks/use-dashboard";
import { useWorkflows } from "@/hooks/use-workflows";
import { useExecutions } from "@/hooks/use-executions";
import { formatDistanceToNow } from "@/lib/utils";
import { DashboardSkeleton } from "@/components/ui/skeletons";

/* ─────────── Status Badge ─────────── */
function StatusBadge({ status }: { status: string }) {
  const config: Record<string, { color: string; bg: string }> = {
    RUNNING: { color: "var(--neurex-running)", bg: "var(--neurex-running-subtle)" },
    SUCCESS: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)" },
    FAILED: { color: "var(--neurex-error)", bg: "var(--neurex-error-subtle)" },
    WAITING: { color: "var(--neurex-warning)", bg: "var(--neurex-warning-subtle)" },
    ACTIVE: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)" },
    INACTIVE: { color: "var(--neurex-text-ghost)", bg: "var(--neurex-bg-overlay)" },
  };

  const s = config[status] || config.INACTIVE;

  return (
    <span
      className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
      style={{ backgroundColor: s.bg, color: s.color }}
    >
      {(status === "RUNNING") && (
        <span
          className="w-1.5 h-1.5 rounded-full animate-pulse"
          style={{ backgroundColor: s.color }}
        />
      )}
      {status}
    </span>
  );
}

/* ─────────── Stat Card ─────────── */
function StatCard({
  label,
  value,
  icon: Icon,
  accent,
  accentBg,
  subtitle,
  delay = 0,
}: {
  label: string;
  value: number | string;
  icon: React.ElementType;
  accent: string;
  accentBg: string;
  subtitle?: string;
  delay?: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay }}
      className="p-5 rounded-xl"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
    >
      <div className="flex items-start justify-between mb-3">
        <div
          className="w-9 h-9 rounded-lg flex items-center justify-center"
          style={{ backgroundColor: accentBg }}
        >
          <Icon className="w-4 h-4" style={{ color: accent }} />
        </div>
      </div>
      <p
        className="text-2xl font-bold tracking-tight"
        style={{ color: "var(--neurex-text-primary)" }}
      >
        {value}
      </p>
      <p className="text-xs mt-1" style={{ color: "var(--neurex-text-tertiary)" }}>
        {label}
      </p>
      {subtitle && (
        <p className="text-xs mt-0.5" style={{ color: accent }}>
          {subtitle}
        </p>
      )}
    </motion.div>
  );
}

/* ─────────── Empty State ─────────── */
function EmptyState() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col items-center justify-center py-20 rounded-xl"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
    >
      <div
        className="w-14 h-14 rounded-2xl flex items-center justify-center mb-5"
        style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
      >
        <BarChart3 className="w-6 h-6" style={{ color: "var(--neurex-accent)" }} />
      </div>
      <h3
        className="text-lg font-semibold mb-2"
        style={{ color: "var(--neurex-text-primary)" }}
      >
        Welcome to Neurex
      </h3>
      <p
        className="text-sm mb-6 max-w-md text-center"
        style={{ color: "var(--neurex-text-secondary)" }}
      >
        Create and run workflows to populate your dashboard with
        execution metrics, success rates, and workflow health data.
      </p>
      <div className="flex items-center gap-3">
        <Link href="/workflows" className="neurex-btn-primary flex items-center gap-2">
          Create your first workflow
        </Link>
        <Link href="/ai/generate" className="neurex-btn-ghost flex items-center gap-2">
          Generate with AI
        </Link>
      </div>
    </motion.div>
  );
}



/* ─────────── Main Dashboard ─────────── */
export default function DashboardPage() {
  const { data: stats, isLoading: statsLoading, error: statsError } = useDashboardStats();
  const { data: workflows, isLoading: wfLoading } = useWorkflows();
  const { data: executions, isLoading: exLoading } = useExecutions(0, 5);

  const isLoading = statsLoading || wfLoading || exLoading;

  if (isLoading) {
    return (
      <div>
        <div className="mb-8">
          <h1
            className="text-2xl font-bold tracking-tight"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Dashboard
          </h1>
          <p className="text-sm mt-1" style={{ color: "var(--neurex-text-secondary)" }}>
            Monitor your workflow infrastructure
          </p>
        </div>
        <DashboardSkeleton />
      </div>
    );
  }

  const hasData = stats && (stats.totalWorkflows > 0 || stats.totalExecutions > 0);

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1
            className="text-2xl font-bold tracking-tight"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Dashboard
          </h1>
          <p className="text-sm mt-1" style={{ color: "var(--neurex-text-secondary)" }}>
            Monitor your workflow infrastructure
          </p>
        </div>
        <Link href="/workflows" className="neurex-btn-primary flex items-center gap-2 text-sm">
          <Plus className="w-4 h-4" />
          New Workflow
        </Link>
      </div>

      {!hasData ? (
        <EmptyState />
      ) : (
        <div className="space-y-6">
          {/* Stats Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard
              label="Total Workflows"
              value={stats!.totalWorkflows}
              icon={Workflow}
              accent="var(--neurex-accent)"
              accentBg="var(--neurex-accent-subtle)"
              subtitle={`${stats!.activeWorkflows} active`}
              delay={0}
            />
            <StatCard
              label="Total Executions"
              value={stats!.totalExecutions}
              icon={Play}
              accent="var(--neurex-running)"
              accentBg="var(--neurex-running-subtle)"
              subtitle={`${stats!.runningExecutions} running`}
              delay={0.05}
            />
            <StatCard
              label="Success Rate"
              value={`${stats!.successRate.toFixed(1)}%`}
              icon={TrendingUp}
              accent="var(--neurex-success)"
              accentBg="var(--neurex-success-subtle)"
              delay={0.1}
            />
            <StatCard
              label="Failed"
              value={stats!.failedExecutions}
              icon={XCircle}
              accent="var(--neurex-error)"
              accentBg="var(--neurex-error-subtle)"
              delay={0.15}
            />
          </div>

          {/* Two column layout */}
          <div className="grid md:grid-cols-2 gap-6">
            {/* Recent Workflows */}
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.2 }}
              className="rounded-xl"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
              }}
            >
              <div
                className="flex items-center justify-between px-5 py-4"
                style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
              >
                <h2
                  className="text-sm font-semibold"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Recent Workflows
                </h2>
                <Link
                  href="/workflows"
                  className="text-xs flex items-center gap-1 transition-colors"
                  style={{ color: "var(--neurex-text-tertiary)" }}
                >
                  View all
                  <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
              <div className="divide-y" style={{ borderColor: "var(--neurex-border-default)" }}>
                {workflows && workflows.length > 0 ? (
                  workflows.slice(0, 5).map((wf) => (
                    <Link
                      key={wf.id}
                      href={`/workflows/${wf.id}`}
                      className="flex items-center justify-between px-5 py-3.5 transition-colors"
                      style={{ borderColor: "var(--neurex-border-default)" }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = "var(--neurex-bg-overlay)";
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = "transparent";
                      }}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div
                          className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                          style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
                        >
                          <Zap className="w-3.5 h-3.5" style={{ color: "var(--neurex-accent)" }} />
                        </div>
                        <div className="min-w-0">
                          <p
                            className="text-sm font-medium truncate"
                            style={{ color: "var(--neurex-text-primary)" }}
                          >
                            {wf.name}
                          </p>
                          <p className="text-xs truncate" style={{ color: "var(--neurex-text-ghost)" }}>
                            {formatDistanceToNow(wf.updatedAt)}
                          </p>
                        </div>
                      </div>
                      <StatusBadge status={wf.status} />
                    </Link>
                  ))
                ) : (
                  <div className="px-5 py-8 text-center">
                    <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
                      No workflows yet
                    </p>
                  </div>
                )}
              </div>
            </motion.div>

            {/* Recent Executions */}
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.25 }}
              className="rounded-xl"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
              }}
            >
              <div
                className="flex items-center justify-between px-5 py-4"
                style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
              >
                <h2
                  className="text-sm font-semibold"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Recent Executions
                </h2>
                <Link
                  href="/executions"
                  className="text-xs flex items-center gap-1 transition-colors"
                  style={{ color: "var(--neurex-text-tertiary)" }}
                >
                  View all
                  <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
              <div className="divide-y" style={{ borderColor: "var(--neurex-border-default)" }}>
                {executions?.content && executions.content.length > 0 ? (
                  executions.content.map((exec, i) => (
                    <Link
                      key={exec.executionId ?? `exec-${i}`}
                      href={`/executions/${exec.executionId}`}
                      className="flex items-center justify-between px-5 py-3.5 transition-colors"
                      style={{ borderColor: "var(--neurex-border-default)" }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = "var(--neurex-bg-overlay)";
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = "transparent";
                      }}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div
                          className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                          style={{
                            backgroundColor:
                              exec.status === "SUCCESS"
                                ? "var(--neurex-success-subtle)"
                                : exec.status === "FAILED"
                                ? "var(--neurex-error-subtle)"
                                : exec.status === "RUNNING"
                                ? "var(--neurex-running-subtle)"
                                : "var(--neurex-warning-subtle)",
                          }}
                        >
                          {exec.status === "SUCCESS" && <CheckCircle2 className="w-3.5 h-3.5" style={{ color: "var(--neurex-success)" }} />}
                          {exec.status === "FAILED" && <XCircle className="w-3.5 h-3.5" style={{ color: "var(--neurex-error)" }} />}
                          {exec.status === "RUNNING" && <Loader2 className="w-3.5 h-3.5 animate-spin" style={{ color: "var(--neurex-running)" }} />}
                          {exec.status === "WAITING" && <Clock className="w-3.5 h-3.5" style={{ color: "var(--neurex-warning)" }} />}
                        </div>
                        <div className="min-w-0">
                          <p
                            className="text-sm font-medium truncate font-mono"
                            style={{ color: "var(--neurex-text-primary)" }}
                          >
                            {exec.executionId?.substring(0, 8) ?? "—"}...
                          </p>
                          <p className="text-xs truncate" style={{ color: "var(--neurex-text-ghost)" }}>
                            {formatDistanceToNow(exec.createdAt)}
                          </p>
                        </div>
                      </div>
                      <StatusBadge status={exec.status} />
                    </Link>
                  ))
                ) : (
                  <div className="px-5 py-8 text-center">
                    <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
                      No executions yet
                    </p>
                  </div>
                )}
              </div>
            </motion.div>
          </div>
        </div>
      )}
    </div>
  );
}
