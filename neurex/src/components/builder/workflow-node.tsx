"use client";

import { memo, useState, useCallback } from "react";
import { Handle, Position, type NodeProps, useReactFlow } from "@xyflow/react";
import { motion } from "framer-motion";
import { GripVertical, Settings, Trash2, Copy } from "lucide-react";
import { getNodeConfig } from "./node-registry";

export interface WorkflowNodeData {
  type: string;
  label: string;
  config: Record<string, unknown>;
  [key: string]: unknown;
}

function WorkflowNodeComponent({ id, data, selected }: NodeProps) {
  const nodeData = data as unknown as WorkflowNodeData;
  const config = getNodeConfig(nodeData.type);
  const Icon = config.icon;
  const { deleteElements, setNodes } = useReactFlow();
  const [showActions, setShowActions] = useState(false);

  const handleDelete = useCallback(() => {
    deleteElements({ nodes: [{ id }] });
  }, [id, deleteElements]);

  const handleDuplicate = useCallback(() => {
    setNodes((nds) => {
      const sourceNode = nds.find((n) => n.id === id);
      if (!sourceNode) return nds;
      const newId = `${nodeData.type}_${Date.now()}`;
      return [
        ...nds,
        {
          ...sourceNode,
          id: newId,
          position: {
            x: sourceNode.position.x + 40,
            y: sourceNode.position.y + 60,
          },
          selected: false,
          data: { ...sourceNode.data },
        },
      ];
    });
  }, [id, nodeData.type, setNodes]);

  return (
    <motion.div
      initial={{ scale: 0.9, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ type: "spring", stiffness: 400, damping: 25 }}
      className="relative group"
      onMouseEnter={() => setShowActions(true)}
      onMouseLeave={() => setShowActions(false)}
    >
      {/* Selection glow ring — animated pulse */}
      {selected && (
        <>
          {/* Outer breathing ring */}
          <motion.div
            className="absolute -inset-[3px] rounded-[15px] pointer-events-none"
            animate={{ opacity: [0.2, 0.4, 0.2] }}
            transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
            style={{
              border: `1.5px solid ${config.color}`,
              boxShadow: `0 0 16px ${config.color}40`,
            }}
          />
          {/* Inner glow */}
          <div
            className="absolute -inset-[1px] rounded-[13px] pointer-events-none"
            style={{
              background: `linear-gradient(135deg, ${config.color}15, transparent 60%)`,
            }}
          />
        </>
      )}

      {/* Node body */}
      <div
        className="relative w-[220px] rounded-xl overflow-hidden transition-all duration-200"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: `1px solid ${selected ? config.color : "var(--neurex-border-default)"}`,
          boxShadow: selected
            ? `0 0 20px ${config.color}30, var(--shadow-md)`
            : "var(--shadow-sm)",
        }}
      >
        {/* Top accent bar */}
        <div
          className="h-[3px] w-full"
          style={{
            background: `linear-gradient(90deg, ${config.color}, ${config.color}80)`,
          }}
        />

        {/* Header */}
        <div className="px-3 pt-2.5 pb-2 flex items-center gap-2.5">
          <div
            className="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0"
            style={{ backgroundColor: config.colorSubtle }}
          >
            <Icon className="w-3.5 h-3.5" style={{ color: config.color }} />
          </div>
          <div className="min-w-0 flex-1">
            <p
              className="text-[13px] font-semibold leading-tight truncate"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              {nodeData.label || config.label}
            </p>
            <p
              className="text-[10px] leading-tight mt-0.5 truncate"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              {config.type}
            </p>
          </div>
        </div>

        {/* Config preview */}
        {nodeData.config && Object.keys(nodeData.config).length > 0 && (
          <div
            className="px-3 pb-2.5"
            style={{ borderTop: `1px solid var(--neurex-border-default)` }}
          >
            <div className="pt-2 space-y-1">
              {Object.entries(nodeData.config)
                .slice(0, 2)
                .map(([key, val]) => {
                  const strVal =
                    typeof val === "string"
                      ? val
                      : typeof val === "object"
                      ? JSON.stringify(val)
                      : String(val);
                  if (!strVal || strVal === "{}" || strVal === "[]") return null;
                  return (
                    <div key={key} className="flex items-center gap-1.5">
                      <span
                        className="text-[10px] flex-shrink-0"
                        style={{ color: "var(--neurex-text-ghost)" }}
                      >
                        {key}
                      </span>
                      <span
                        className="text-[10px] font-mono truncate flex-1 text-right"
                        style={{ color: "var(--neurex-text-tertiary)" }}
                      >
                        {strVal.length > 24 ? strVal.substring(0, 24) + "…" : strVal}
                      </span>
                    </div>
                  );
                })}
            </div>
          </div>
        )}
      </div>

      {/* Hover actions */}
      {showActions && (
        <div
          className="absolute -top-8 left-1/2 -translate-x-1/2 flex items-center gap-1 px-1.5 py-1 rounded-lg"
          style={{
            backgroundColor: "var(--neurex-bg-overlay)",
            border: "1px solid var(--neurex-border-default)",
            boxShadow: "var(--shadow-md)",
          }}
        >
          <button
            onClick={handleDuplicate}
            className="p-1 rounded hover:bg-[var(--neurex-bg-subtle)] transition-colors"
            title="Duplicate"
          >
            <Copy className="w-3 h-3" style={{ color: "var(--neurex-text-tertiary)" }} />
          </button>
          <button
            onClick={handleDelete}
            className="p-1 rounded hover:bg-[var(--neurex-error-subtle)] transition-colors"
            title="Delete"
          >
            <Trash2 className="w-3 h-3" style={{ color: "var(--neurex-error)" }} />
          </button>
        </div>
      )}

      {/* Handles */}
      <Handle
        type="target"
        position={Position.Top}
        className="!w-3 !h-3 !rounded-full !border-2 transition-all duration-200"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          borderColor: selected ? config.color : "var(--neurex-border-focus)",
          top: -6,
        }}
      />
      <Handle
        type="source"
        position={Position.Bottom}
        className="!w-3 !h-3 !rounded-full !border-2 transition-all duration-200"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          borderColor: selected ? config.color : "var(--neurex-border-focus)",
          bottom: -6,
        }}
      />
    </motion.div>
  );
}

export const WorkflowNode = memo(WorkflowNodeComponent);
