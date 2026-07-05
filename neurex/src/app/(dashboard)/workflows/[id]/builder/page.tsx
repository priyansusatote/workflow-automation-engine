"use client";

import React, { useMemo, useCallback, useState } from "react";
import { useRouter } from "next/navigation";
import { Loader2, ArrowLeft } from "lucide-react";
import { useToast } from "@/components/ui/toast";
import Link from "next/link";
import type { Node, Edge } from "@xyflow/react";
import { BuilderCanvas } from "@/components/builder/builder-canvas";
import {
  useWorkflow,
  useWorkflowDefinition,
  useSaveDefinition,
  useValidateWorkflow,
  useExecuteWorkflow,
} from "@/hooks/use-workflows";
import type { WorkflowNodeData } from "@/components/builder/workflow-node";
import type { WorkflowDefinitionRequest } from "@/types/definition";
import { ExecuteModal } from "@/components/modals/execute-modal";

/** Convert backend definition → React Flow nodes/edges */
function definitionToFlow(def: WorkflowDefinitionRequest | null): {
  nodes: Node[];
  edges: Edge[];
} {
  if (!def || !def.nodes) {
    return { nodes: [], edges: [] };
  }

  const nodes: Node[] = def.nodes.map((n, i) => ({
    id: n.id,
    type: "workflowNode",
    position: {
      x: 250 + (i % 3) * 300,
      y: 100 + Math.floor(i / 3) * 200,
    },
    data: {
      type: n.type,
      label: n.id,
      config: n.config || {},
    } as WorkflowNodeData,
  }));

  const edges: Edge[] = (def.edges || []).map((e, i) => ({
    id: `e-${e.from}-${e.to}`,
    source: e.from,
    target: e.to,
    type: "animated",
    label: e.condition || undefined,
  }));

  return { nodes, edges };
}

/** Convert React Flow nodes/edges → backend definition */
function flowToDefinition(
  nodes: Node[],
  edges: Edge[]
): WorkflowDefinitionRequest {
  return {
    nodes: nodes.map((n) => ({
      id: n.id,
      type: (n.data as WorkflowNodeData).type,
      config: (n.data as WorkflowNodeData).config || {},
    })),
    edges: edges.map((e) => ({
      from: e.source,
      to: e.target,
      condition: (e.label as string) || undefined,
    })),
  };
}

export default function WorkflowBuilderPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = React.use(params);
  const router = useRouter();
  const { data: workflow, isLoading: wfLoading } = useWorkflow(id);
  const { data: rawDefinition, isLoading: defLoading } = useWorkflowDefinition(id);
  const saveDef = useSaveDefinition(id);
  const validate = useValidateWorkflow();
  const execute = useExecuteWorkflow();
  const [showExecuteModal, setShowExecuteModal] = useState(false);
  const { success: toastSuccess, error: toastError, warning: toastWarning } = useToast();

  // Parse definition — backend returns string, might need to parse
  const parsedDefinition = useMemo(() => {
    if (!rawDefinition) return null;
    if (typeof rawDefinition === "string") {
      try {
        return JSON.parse(rawDefinition) as WorkflowDefinitionRequest;
      } catch {
        return null;
      }
    }
    return rawDefinition as WorkflowDefinitionRequest;
  }, [rawDefinition]);

  const { nodes: initialNodes, edges: initialEdges } = useMemo(
    () => definitionToFlow(parsedDefinition),
    [parsedDefinition]
  );

  // ── Handlers ──
  const handleSave = useCallback(
    (nodes: Node[], edges: Edge[]) => {
      const definition = flowToDefinition(nodes, edges);
      saveDef.mutate(definition);
    },
    [saveDef]
  );

  const handleValidate = useCallback(() => {
    validate.mutate(id, {
      onSuccess: (result) => {
        if (result.valid) {
          toastSuccess("Workflow is valid!");
        } else {
          toastWarning(`Validation errors: ${result.errors.join(", ")}`);
        }
      },
      onError: (err: Error) => {
        toastError(`Validation failed: ${err.message}`);
      },
    });
  }, [id, validate, toastSuccess, toastWarning, toastError]);

  const handleExecute = useCallback(() => {
    setShowExecuteModal(true);
  }, []);

  const handleExecuteConfirm = useCallback(
    (inputData: Record<string, unknown>) => {
      execute.mutate(
        { workflowId: id, inputData },
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
    [id, execute, router, toastError]
  );

  // ── Loading ──
  if (wfLoading || defLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="flex flex-col items-center gap-3">
          <Loader2
            className="w-6 h-6 animate-spin"
            style={{ color: "var(--neurex-accent)" }}
          />
          <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
            Loading builder...
          </p>
        </div>
      </div>
    );
  }

  if (!workflow) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <p className="text-sm" style={{ color: "var(--neurex-text-tertiary)" }}>
          Workflow not found
        </p>
        <Link href="/workflows" className="neurex-btn-ghost text-sm">
          Back to workflows
        </Link>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      <BuilderCanvas
        workflowId={id}
        workflowName={workflow.name}
        initialNodes={initialNodes}
        initialEdges={initialEdges}
        onSave={handleSave}
        onValidate={handleValidate}
        onExecute={handleExecute}
        isSaving={saveDef.isPending}
        isValidating={validate.isPending}
        isExecuting={execute.isPending}
      />
      <ExecuteModal
        isOpen={showExecuteModal}
        onClose={() => setShowExecuteModal(false)}
        onExecute={handleExecuteConfirm}
        isPending={execute.isPending}
        workflowName={workflow.name}
      />
    </div>
  );
}
