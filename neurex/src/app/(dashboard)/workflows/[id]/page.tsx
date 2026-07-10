"use client";

import React, { useState, useCallback } from "react";
import { motion } from "framer-motion";
import { useToast } from "@/components/ui/toast";
import {
  ArrowLeft,
  Play,
  Power,
  PowerOff,
  Edit3,
  Trash2,
  Loader2,
  CheckCircle2,
  XCircle,
  Clock,
  Zap,
  GitBranch,
  Activity,
  Copy,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  useWorkflow,
  useDeleteWorkflow,
  useActivateWorkflow,
  useDeactivateWorkflow,
  useExecuteWorkflow,
  useCreateWorkflow,
} from "@/hooks/use-workflows";
import { useWorkflowExecutions } from "@/hooks/use-executions";
import { formatDistanceToNow } from "@/lib/utils";
import type { ExecutionStatus } from "@/types/execution";
import { ExecuteModal } from "@/components/modals/execute-modal";
import { WorkflowDetailSkeleton } from "@/components/ui/skeletons";

const statusConfig: Record<
  ExecutionStatus,
  { color: string; bg: string; icon: React.ElementType }
> = {
  RUNNING: { color: "var(--neurex-running)", bg: "var(--neurex-running-subtle)", icon: Loader2 },
  SUCCESS: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)", icon: CheckCircle2 },
  FAILED: { color: "var(--neurex-error)", bg: "var(--neurex-error-subtle)", icon: XCircle },
  WAITING: { color: "var(--neurex-warning)", bg: "var(--neurex-warning-subtle)", icon: Clock },
};

export default function WorkflowDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = React.use(params);
  const router = useRouter();
  const { data: workflow, isLoading } = useWorkflow(id);
  const { data: executions } = useWorkflowExecutions(id);
  const deleteWf = useDeleteWorkflow();
  const activate = useActivateWorkflow();
  const deactivate = useDeactivateWorkflow();
  const execute = useExecuteWorkflow();
  const createWf = useCreateWorkflow();
  const [showExecuteModal, setShowExecuteModal] = useState(false);

  const { error: toastError } = useToast();

  const handleExecute = useCallback(
    (inputData: Record<string, unknown>) => {
      if (!workflow) return;
      execute.mutate(
        { workflowId: workflow.id, inputData },
        {
          onSuccess: (result) => {
            setShowExecuteModal(false);
            router.push(`/executions/${result.executionId}`);
          },
          onError: (err: Error) => {
            toastError(`Execution failed: ${err.message}`);
          },
        }
      );
    },
    [workflow, execute, router, toastError]
  );

  if (isLoading) {
    return <WorkflowDetailSkeleton />;
  }

  if (!workflow) {
    return (
      <div className="flex flex-col items-center justify-center py-32">
        <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
          Workflow not found
        </p>
        <Link href="/workflows" className="neurex-btn-ghost mt-4 text-sm">
          Back to workflows
        </Link>
      </div>
    );
  }

  const handleDelete = () => {
    if (confirm("Delete this workflow? This cannot be undone.")) {
      deleteWf.mutate(workflow.id, {
        onSuccess: () => router.push("/workflows"),
      });
    }
  };

  return (
    <div>
      {/* Back */}
      <Link
        href="/workflows"
        className="inline-flex items-center gap-1.5 text-sm mb-6 transition-colors"
        style={{ color: "var(--neurex-text-tertiary)" }}
      >
        <ArrowLeft className="w-4 h-4" />
        All workflows
      </Link>

      {/* Header */}
      <div className="flex items-start justify-between mb-8">
        <div className="flex items-center gap-4">
          <div
            className="w-12 h-12 rounded-xl flex items-center justify-center"
            style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
          >
            <Zap className="w-5 h-5" style={{ color: "var(--neurex-accent)" }} />
          </div>
          <div>
            <h1
              className="text-2xl font-bold tracking-tight"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              {workflow.name}
            </h1>
            <div className="flex items-center gap-3 mt-1">
              <span
                className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium`}
                style={{
                  backgroundColor:
                    workflow.status === "ACTIVE"
                      ? "var(--neurex-success-subtle)"
                      : "var(--neurex-bg-overlay)",
                  color:
                    workflow.status === "ACTIVE"
                      ? "var(--neurex-success)"
                      : "var(--neurex-text-ghost)",
                }}
              >
                {workflow.status === "ACTIVE" && (
                  <span
                    className="w-1.5 h-1.5 rounded-full"
                    style={{ backgroundColor: "var(--neurex-success)" }}
                  />
                )}
                {workflow.status}
              </span>
              <span className="text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
                Created {formatDistanceToNow(workflow.createdAt)}
              </span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          <Link
            href={`/workflows/${id}/builder`}
            className="neurex-btn-ghost flex items-center gap-2 text-sm"
          >
            <GitBranch className="w-4 h-4" />
            Open Builder
          </Link>
          <button
            onClick={() => {
              if (!workflow) return;
              createWf.mutate(
                { name: `${workflow.name} (Copy)`, description: workflow.description || "" },
                { onSuccess: (created) => router.push(`/workflows/${created.id}`) }
              );
            }}
            disabled={createWf.isPending}
            className="neurex-btn-ghost flex items-center gap-2 text-sm"
          >
            {createWf.isPending ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Copy className="w-4 h-4" />
            )}
            Duplicate
          </button>
          {workflow.status === "ACTIVE" ? (
            <>
              <button
                onClick={() => setShowExecuteModal(true)}
                disabled={execute.isPending}
                className="neurex-btn-primary flex items-center gap-2 text-sm"
              >
                {execute.isPending ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Play className="w-4 h-4" />
                )}
                Execute
              </button>
              <button
                onClick={() => deactivate.mutate(workflow.id)}
                className="neurex-btn-ghost flex items-center gap-2 text-sm"
                style={{ color: "var(--neurex-warning)" }}
              >
                <PowerOff className="w-4 h-4" />
              </button>
            </>
          ) : (
            <button
              onClick={() => activate.mutate(workflow.id)}
              className="neurex-btn-primary flex items-center gap-2 text-sm"
            >
              <Power className="w-4 h-4" />
              Activate
            </button>
          )}
          <button
            onClick={handleDelete}
            className="neurex-btn-ghost p-2"
            style={{ color: "var(--neurex-error)" }}
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Description */}
      {workflow.description && (
        <div
          className="p-4 rounded-xl mb-8"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <p className="text-sm" style={{ color: "var(--neurex-text-secondary)" }}>
            {workflow.description}
          </p>
        </div>
      )}

      {/* Two-column: Info + Timeline */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Details */}
        <div
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
            Details
          </h3>
          <div className="space-y-3">
            {[
              { label: "ID", value: workflow.id.substring(0, 12) + "..." },
              { label: "Status", value: workflow.status },
              { label: "Created", value: formatDistanceToNow(workflow.createdAt) },
              { label: "Updated", value: formatDistanceToNow(workflow.updatedAt) },
            ].map(({ label, value }) => (
              <div key={label} className="flex items-center justify-between">
                <span className="text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
                  {label}
                </span>
                <span
                  className="text-xs font-mono"
                  style={{ color: "var(--neurex-text-secondary)" }}
                >
                  {value}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Execution History (Activity Timeline) */}
        <div
          className="md:col-span-2 rounded-xl"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <div
            className="flex items-center gap-2 px-5 py-4"
            style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
          >
            <Activity className="w-4 h-4" style={{ color: "var(--neurex-text-ghost)" }} />
            <h3
              className="text-sm font-semibold"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Execution History
            </h3>
          </div>
          <div className="p-5">
            {executions?.content && executions.content.length > 0 ? (
              <div className="space-y-1">
                {executions.content.map((exec, i) => {
                  const sc = statusConfig[exec.status];
                  const Icon = sc.icon;
                  return (
                    <Link
                      key={exec.executionId ?? `exec-${i}`}
                      href={`/executions/${exec.executionId}`}
                      className="flex items-center gap-3 p-3 rounded-lg transition-colors"
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = "var(--neurex-bg-overlay)";
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = "transparent";
                      }}
                    >
                      <div
                        className="w-7 h-7 rounded-md flex items-center justify-center flex-shrink-0"
                        style={{ backgroundColor: sc.bg }}
                      >
                        <Icon
                          className={`w-3.5 h-3.5 ${exec.status === "RUNNING" ? "animate-spin" : ""}`}
                          style={{ color: sc.color }}
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p
                          className="text-sm font-mono"
                          style={{ color: "var(--neurex-text-primary)" }}
                        >
                          {exec.executionId?.substring(0, 8) ?? "—"}
                        </p>
                        <p className="text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
                          {formatDistanceToNow(exec.createdAt)}
                        </p>
                      </div>
                      <span
                        className="text-xs font-medium px-2 py-0.5 rounded-full"
                        style={{ backgroundColor: sc.bg, color: sc.color }}
                      >
                        {exec.status}
                      </span>
                    </Link>
                  );
                })}
              </div>
            ) : (
              <div className="text-center py-12">
                <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
                  No executions yet. Execute this workflow to see history here.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Execute Modal */}
      <ExecuteModal
        isOpen={showExecuteModal}
        onClose={() => setShowExecuteModal(false)}
        onExecute={handleExecute}
        isPending={execute.isPending}
        workflowName={workflow.name}
      />
    </div>
  );
}
