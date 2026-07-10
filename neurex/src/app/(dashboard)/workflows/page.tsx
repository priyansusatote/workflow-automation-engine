"use client";

import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Plus,
  Search,
  Zap,
  MoreVertical,
  Trash2,
  Edit3,
  Play,
  Power,
  PowerOff,
  Loader2,
  Workflow,
  X,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  useWorkflows,
  useCreateWorkflow,
  useDeleteWorkflow,
  useActivateWorkflow,
  useDeactivateWorkflow,
  useExecuteWorkflow,
} from "@/hooks/use-workflows";
import { formatDistanceToNow } from "@/lib/utils";
import type { WorkflowResponse, WorkflowStatus } from "@/types/workflow";

/* ─────────── Status Badge ─────────── */
function StatusBadge({ status }: { status: WorkflowStatus }) {
  const config: Record<WorkflowStatus, { color: string; bg: string }> = {
    ACTIVE: { color: "var(--neurex-success)", bg: "var(--neurex-success-subtle)" },
    INACTIVE: { color: "var(--neurex-text-ghost)", bg: "var(--neurex-bg-overlay)" },
  };
  const s = config[status];
  return (
    <span
      className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
      style={{ backgroundColor: s.bg, color: s.color }}
    >
      {status === "ACTIVE" && (
        <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: s.color }} />
      )}
      {status}
    </span>
  );
}

/* ─────────── Create Workflow Modal ─────────── */
function CreateWorkflowModal({
  isOpen,
  onClose,
}: {
  isOpen: boolean;
  onClose: () => void;
}) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const router = useRouter();
  const create = useCreateWorkflow();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    create.mutate(
      { name: name.trim(), description: description.trim() },
      {
        onSuccess: (workflow) => {
          onClose();
          setName("");
          setDescription("");
          router.push(`/workflows/${workflow.id}`);
        },
      }
    );
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50"
            style={{ backgroundColor: "hsla(228, 14%, 4%, 0.7)" }}
            onClick={onClose}
          />
          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
          >
            <div
              className="w-full max-w-lg rounded-2xl p-6"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
                boxShadow: "0 24px 80px hsla(228, 14%, 0%, 0.6)",
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between mb-6">
                <h2
                  className="text-lg font-semibold"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Create Workflow
                </h2>
                <button
                  onClick={onClose}
                  className="p-1 rounded-lg transition-colors"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label
                    className="block text-xs font-medium mb-2"
                    style={{ color: "var(--neurex-text-secondary)" }}
                  >
                    Name
                  </label>
                  <input
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="My Workflow"
                    autoFocus
                    className="neurex-input w-full"
                  />
                </div>
                <div>
                  <label
                    className="block text-xs font-medium mb-2"
                    style={{ color: "var(--neurex-text-secondary)" }}
                  >
                    Description
                  </label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="What does this workflow do?"
                    rows={3}
                    className="neurex-input w-full resize-none"
                  />
                </div>
                <div className="flex justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={onClose}
                    className="neurex-btn-ghost text-sm"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={!name.trim() || create.isPending}
                    className="neurex-btn-primary flex items-center gap-2 text-sm"
                    style={{ opacity: !name.trim() || create.isPending ? 0.5 : 1 }}
                  >
                    {create.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <Plus className="w-4 h-4" />
                    )}
                    Create
                  </button>
                </div>
              </form>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

/* ─────────── Context Menu ─────────── */
function WorkflowMenu({
  workflow,
  onClose,
}: {
  workflow: WorkflowResponse;
  onClose: () => void;
}) {
  const deleteWf = useDeleteWorkflow();
  const activate = useActivateWorkflow();
  const deactivate = useDeactivateWorkflow();
  const execute = useExecuteWorkflow();

  const actions = [
    ...(workflow.status === "ACTIVE"
      ? [
          {
            label: "Execute",
            icon: Play,
            color: "var(--neurex-success)",
            action: () => {
              execute.mutate({ workflowId: workflow.id });
              onClose();
            },
          },
          {
            label: "Deactivate",
            icon: PowerOff,
            color: "var(--neurex-warning)",
            action: () => {
              deactivate.mutate(workflow.id);
              onClose();
            },
          },
        ]
      : [
          {
            label: "Activate",
            icon: Power,
            color: "var(--neurex-success)",
            action: () => {
              activate.mutate(workflow.id);
              onClose();
            },
          },
        ]),
    {
      label: "Delete",
      icon: Trash2,
      color: "var(--neurex-error)",
      action: () => {
        if (confirm("Delete this workflow? This cannot be undone.")) {
          deleteWf.mutate(workflow.id);
        }
        onClose();
      },
    },
  ];

  return (
    <>
      <div className="fixed inset-0 z-40" onClick={onClose} />
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.95 }}
        className="absolute right-0 top-full mt-1 z-50 w-44 rounded-xl py-1.5 overflow-hidden"
        style={{
          backgroundColor: "var(--neurex-bg-overlay)",
          border: "1px solid var(--neurex-border-default)",
          boxShadow: "var(--shadow-lg)",
        }}
      >
        {actions.map((action) => (
          <button
            key={action.label}
            onClick={action.action}
            className="w-full flex items-center gap-2.5 px-3.5 py-2 text-sm transition-colors text-left"
            style={{ color: action.color }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = "var(--neurex-bg-elevated)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "transparent";
            }}
          >
            <action.icon className="w-3.5 h-3.5" />
            {action.label}
          </button>
        ))}
      </motion.div>
    </>
  );
}

/* ─────────── Workflow Card ─────────── */
function WorkflowCard({ workflow }: { workflow: WorkflowResponse }) {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      className="group relative rounded-xl p-5 transition-all duration-200"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.borderColor = "var(--neurex-border-hover)";
        e.currentTarget.style.boxShadow = "var(--shadow-md)";
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.borderColor = "var(--neurex-border-default)";
        e.currentTarget.style.boxShadow = "none";
      }}
    >
      <div className="flex items-start justify-between mb-3">
        <Link href={`/workflows/${workflow.id}`} className="flex items-center gap-3 min-w-0">
          <div
            className="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0"
            style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
          >
            <Zap className="w-4 h-4" style={{ color: "var(--neurex-accent)" }} />
          </div>
          <div className="min-w-0">
            <p
              className="text-sm font-semibold truncate"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              {workflow.name}
            </p>
            <p className="text-xs truncate" style={{ color: "var(--neurex-text-ghost)" }}>
              {formatDistanceToNow(workflow.updatedAt)}
            </p>
          </div>
        </Link>
        <div className="relative flex items-center gap-2">
          <StatusBadge status={workflow.status} />
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="p-1 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
            style={{ color: "var(--neurex-text-ghost)" }}
          >
            <MoreVertical className="w-4 h-4" />
          </button>
          <AnimatePresence>
            {menuOpen && (
              <WorkflowMenu
                workflow={workflow}
                onClose={() => setMenuOpen(false)}
              />
            )}
          </AnimatePresence>
        </div>
      </div>
      {workflow.description && (
        <p
          className="text-xs leading-relaxed line-clamp-2 pl-12"
          style={{ color: "var(--neurex-text-tertiary)" }}
        >
          {workflow.description}
        </p>
      )}
    </motion.div>
  );
}

/* ─────────── Main Page ─────────── */
export default function WorkflowsPage() {
  const [createOpen, setCreateOpen] = useState(false);
  const [search, setSearch] = useState("");
  const { data: workflows, isLoading } = useWorkflows();

  const filtered = workflows?.filter(
    (wf) =>
      wf.name.toLowerCase().includes(search.toLowerCase()) ||
      wf.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1
            className="text-2xl font-bold tracking-tight"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Workflows
          </h1>
          <p className="text-sm mt-1" style={{ color: "var(--neurex-text-secondary)" }}>
            Create, manage, and execute your workflow automations
          </p>
        </div>
        <button
          onClick={() => setCreateOpen(true)}
          className="neurex-btn-primary flex items-center gap-2 text-sm"
        >
          <Plus className="w-4 h-4" />
          New Workflow
        </button>
      </div>

      {/* Search */}
      <div className="relative mb-6">
        <Search
          className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4"
          style={{ color: "var(--neurex-text-ghost)" }}
        />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search workflows..."
          className="neurex-input w-full pl-10"
          style={{ maxWidth: 400 }}
        />
      </div>

      {/* List */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
          {Array.from({ length: 6 }).map((_, i) => (
            <div
              key={i}
              className="h-40 rounded-xl"
              style={{ backgroundColor: "var(--neurex-bg-elevated)" }}
            />
          ))}
        </div>
      ) : filtered && filtered.length > 0 ? (
        <motion.div layout className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <AnimatePresence>
            {filtered.map((wf) => (
              <WorkflowCard key={wf.id} workflow={wf} />
            ))}
          </AnimatePresence>
        </motion.div>
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
            style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
          >
            <Workflow className="w-6 h-6" style={{ color: "var(--neurex-accent)" }} />
          </div>
          <h3
            className="text-lg font-semibold mb-2"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            {search ? "No matching workflows" : "No workflows yet"}
          </h3>
          <p
            className="text-sm mb-6 text-center"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            {search
              ? `No workflows match "${search}"`
              : "Create your first workflow to get started"}
          </p>
          {!search && (
            <button
              onClick={() => setCreateOpen(true)}
              className="neurex-btn-primary flex items-center gap-2 text-sm"
            >
              <Plus className="w-4 h-4" />
              Create Workflow
            </button>
          )}
        </motion.div>
      )}

      <CreateWorkflowModal
        isOpen={createOpen}
        onClose={() => setCreateOpen(false)}
      />
    </div>
  );
}
