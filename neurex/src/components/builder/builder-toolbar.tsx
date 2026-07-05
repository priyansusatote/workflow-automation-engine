"use client";

import { memo } from "react";
import {
  Save,
  Play,
  CheckCircle,
  Undo2,
  Redo2,
  ZoomIn,
  ZoomOut,
  Maximize2,
  Loader2,
} from "lucide-react";
import { useReactFlow } from "@xyflow/react";

interface BuilderToolbarProps {
  workflowName: string;
  isSaving: boolean;
  isValidating: boolean;
  isExecuting: boolean;
  onSave: () => void;
  onValidate: () => void;
  onExecute: () => void;
  hasUnsavedChanges: boolean;
}

function BuilderToolbarComponent({
  workflowName,
  isSaving,
  isValidating,
  isExecuting,
  onSave,
  onValidate,
  onExecute,
  hasUnsavedChanges,
}: BuilderToolbarProps) {
  const { zoomIn, zoomOut, fitView } = useReactFlow();

  return (
    <div
      className="h-12 flex items-center justify-between px-4 flex-shrink-0"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        borderBottom: "1px solid var(--neurex-border-default)",
      }}
    >
      {/* Left — workflow name */}
      <div className="flex items-center gap-3">
        <h2
          className="text-sm font-semibold"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          {workflowName}
        </h2>
        {hasUnsavedChanges && (
          <span
            className="w-2 h-2 rounded-full"
            style={{ backgroundColor: "var(--neurex-warning)" }}
            title="Unsaved changes"
          />
        )}
      </div>

      {/* Center — zoom controls */}
      <div
        className="flex items-center gap-0.5 rounded-lg px-1 py-0.5"
        style={{
          backgroundColor: "var(--neurex-bg-overlay)",
          border: "1px solid var(--neurex-border-default)",
        }}
      >
        <button
          onClick={() => zoomOut()}
          className="p-1.5 rounded transition-colors hover:bg-[var(--neurex-bg-subtle)]"
          title="Zoom out"
        >
          <ZoomOut className="w-3.5 h-3.5" style={{ color: "var(--neurex-text-tertiary)" }} />
        </button>
        <button
          onClick={() => fitView({ padding: 0.2, duration: 400 })}
          className="p-1.5 rounded transition-colors hover:bg-[var(--neurex-bg-subtle)]"
          title="Fit view"
        >
          <Maximize2 className="w-3.5 h-3.5" style={{ color: "var(--neurex-text-tertiary)" }} />
        </button>
        <button
          onClick={() => zoomIn()}
          className="p-1.5 rounded transition-colors hover:bg-[var(--neurex-bg-subtle)]"
          title="Zoom in"
        >
          <ZoomIn className="w-3.5 h-3.5" style={{ color: "var(--neurex-text-tertiary)" }} />
        </button>
      </div>

      {/* Right — actions */}
      <div className="flex items-center gap-2">
        <button
          onClick={onValidate}
          disabled={isValidating}
          className="neurex-btn-ghost flex items-center gap-1.5 text-xs !px-3 !py-1.5"
        >
          {isValidating ? (
            <Loader2 className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <CheckCircle className="w-3.5 h-3.5" />
          )}
          Validate
        </button>
        <button
          onClick={onSave}
          disabled={isSaving}
          className="neurex-btn-ghost flex items-center gap-1.5 text-xs !px-3 !py-1.5"
        >
          {isSaving ? (
            <Loader2 className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <Save className="w-3.5 h-3.5" />
          )}
          Save
        </button>
        <button
          onClick={onExecute}
          disabled={isExecuting}
          className="neurex-btn-primary flex items-center gap-1.5 text-xs !px-3 !py-1.5"
        >
          {isExecuting ? (
            <Loader2 className="w-3.5 h-3.5 animate-spin" />
          ) : (
            <Play className="w-3.5 h-3.5" />
          )}
          Execute
        </button>
      </div>
    </div>
  );
}

export const BuilderToolbar = memo(BuilderToolbarComponent);
