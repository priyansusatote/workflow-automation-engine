"use client";

import { useCallback, useRef, useState, useMemo } from "react";
import {
  ReactFlow,
  Background,
  MiniMap,
  Controls,
  addEdge,
  useNodesState,
  useEdgesState,
  ReactFlowProvider,
  BackgroundVariant,
  ConnectionLineType,
  type Connection,
  type Node,
  type Edge,
  type ReactFlowInstance,
  type NodeMouseHandler,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";

import { WorkflowNode } from "./workflow-node";
import { AnimatedEdge } from "./animated-edge";
import { NodePalette } from "./node-palette";
import { BuilderToolbar } from "./builder-toolbar";
import { ContextMenu, useNodeContextMenu, useCanvasContextMenu } from "./context-menu";
import { NODE_TYPES } from "./node-registry";
import type { WorkflowNodeData } from "./workflow-node";

// ─── Register custom types ─────────────────
const nodeTypes = { workflowNode: WorkflowNode };
const edgeTypes = { animated: AnimatedEdge };

// ─── Default edge options ───────────────────
const defaultEdgeOptions = {
  type: "animated",
  animated: true,
};

interface BuilderCanvasProps {
  workflowId: string;
  workflowName: string;
  initialNodes: Node[];
  initialEdges: Edge[];
  onSave: (nodes: Node[], edges: Edge[]) => void;
  onValidate: () => void;
  onExecute: () => void;
  isSaving: boolean;
  isValidating: boolean;
  isExecuting: boolean;
}

// ─── Context Menu State ─────────────────────
interface ContextMenuState {
  x: number;
  y: number;
  type: "node" | "canvas";
  nodeId?: string;
}

function BuilderCanvasInner({
  workflowId,
  workflowName,
  initialNodes,
  initialEdges,
  onSave,
  onValidate,
  onExecute,
  isSaving,
  isValidating,
  isExecuting,
}: BuilderCanvasProps) {
  const reactFlowWrapper = useRef<HTMLDivElement>(null);
  const [reactFlowInstance, setReactFlowInstance] =
    useState<ReactFlowInstance | null>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);
  const { getNodeItems } = useNodeContextMenu();
  const { getCanvasItems } = useCanvasContextMenu();

  // ── Connect handler ──
  const onConnect = useCallback(
    (params: Connection) => {
      setEdges((eds) =>
        addEdge({ ...params, type: "animated" }, eds)
      );
      setHasUnsavedChanges(true);
    },
    [setEdges]
  );

  // ── Track changes ──
  const onNodesChangeWrapped = useCallback(
    (changes: Parameters<typeof onNodesChange>[0]) => {
      onNodesChange(changes);
      const hasRealChange = changes.some(
        (c) => c.type !== "select" && c.type !== "dimensions"
      );
      if (hasRealChange) setHasUnsavedChanges(true);
    },
    [onNodesChange]
  );

  const onEdgesChangeWrapped = useCallback(
    (changes: Parameters<typeof onEdgesChange>[0]) => {
      onEdgesChange(changes);
      setHasUnsavedChanges(true);
    },
    [onEdgesChange]
  );

  // ── Drop handler (from palette) ──
  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = "move";
  }, []);

  const onDrop = useCallback(
    (event: React.DragEvent) => {
      event.preventDefault();

      const nodeType = event.dataTransfer.getData(
        "application/neurex-node-type"
      );
      if (!nodeType || !reactFlowInstance || !reactFlowWrapper.current) return;

      const config = NODE_TYPES[nodeType];
      if (!config) return;

      const bounds = reactFlowWrapper.current.getBoundingClientRect();
      const position = reactFlowInstance.screenToFlowPosition({
        x: event.clientX - bounds.left,
        y: event.clientY - bounds.top,
      });

      const newId = `${nodeType}_${Date.now()}`;
      const newNode: Node = {
        id: newId,
        type: "workflowNode",
        position,
        data: {
          type: nodeType,
          label: config.label,
          config: { ...config.defaultConfig },
        } as WorkflowNodeData,
      };

      setNodes((nds) => [...nds, newNode]);
      setHasUnsavedChanges(true);
    },
    [reactFlowInstance, setNodes]
  );

  // ── Save handler ──
  const handleSave = useCallback(() => {
    onSave(nodes, edges);
    setHasUnsavedChanges(false);
  }, [nodes, edges, onSave]);

  // ── Node actions (for context menu) ──
  const handleDeleteNode = useCallback(
    (nodeId: string) => {
      setNodes((nds) => nds.filter((n) => n.id !== nodeId));
      setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));
      setHasUnsavedChanges(true);
    },
    [setNodes, setEdges]
  );

  const handleDuplicateNode = useCallback(
    (nodeId: string) => {
      setNodes((nds) => {
        const source = nds.find((n) => n.id === nodeId);
        if (!source) return nds;
        const nodeData = source.data as unknown as WorkflowNodeData;
        const newId = `${nodeData.type}_${Date.now()}`;
        return [
          ...nds,
          {
            ...source,
            id: newId,
            position: {
              x: source.position.x + 40,
              y: source.position.y + 60,
            },
            selected: false,
            data: { ...source.data },
          },
        ];
      });
      setHasUnsavedChanges(true);
    },
    [setNodes]
  );

  // ── Context menu handlers ──
  const onNodeContextMenu: NodeMouseHandler = useCallback(
    (event, node) => {
      event.preventDefault();
      setContextMenu({
        x: (event as unknown as MouseEvent).clientX,
        y: (event as unknown as MouseEvent).clientY,
        type: "node",
        nodeId: node.id,
      });
    },
    []
  );

  const onPaneContextMenu = useCallback(
    (event: React.MouseEvent | MouseEvent) => {
      event.preventDefault();
      setContextMenu({
        x: (event as MouseEvent).clientX,
        y: (event as MouseEvent).clientY,
        type: "canvas",
      });
    },
    []
  );

  const closeContextMenu = useCallback(() => {
    setContextMenu(null);
  }, []);

  // ── Keyboard shortcuts ──
  const onKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      const ctrl = event.ctrlKey || event.metaKey;

      // Ctrl+S — Save
      if (ctrl && event.key === "s") {
        event.preventDefault();
        handleSave();
        return;
      }

      // Ctrl+A — Select all
      if (ctrl && event.key === "a") {
        event.preventDefault();
        setNodes((nds) => nds.map((n) => ({ ...n, selected: true })));
        setEdges((eds) => eds.map((e) => ({ ...e, selected: true })));
        return;
      }

      // Ctrl+D — Duplicate selected
      if (ctrl && event.key === "d") {
        event.preventDefault();
        const selected = nodes.filter((n) => n.selected);
        if (selected.length > 0) {
          handleDuplicateNode(selected[0].id);
        }
        return;
      }

      // Escape — Deselect all
      if (event.key === "Escape") {
        setNodes((nds) => nds.map((n) => ({ ...n, selected: false })));
        setEdges((eds) => eds.map((e) => ({ ...e, selected: false })));
        closeContextMenu();
        return;
      }

      // F — Fit view
      if (event.key === "f" && !ctrl && !event.shiftKey) {
        reactFlowInstance?.fitView({ padding: 0.3, duration: 300 });
        return;
      }
    },
    [handleSave, handleDuplicateNode, setNodes, setEdges, nodes, closeContextMenu, reactFlowInstance]
  );

  // ── Probing connection validation ──
  const isValidConnection = useCallback(
    (connection: Edge | Connection) => {
      if (connection.source === connection.target) return false;
      const exists = edges.some(
        (e) =>
          e.source === connection.source && e.target === connection.target
      );
      return !exists;
    },
    [edges]
  );

  // ── Context menu items ──
  const contextMenuItems = useMemo(() => {
    if (!contextMenu) return [];

    if (contextMenu.type === "node" && contextMenu.nodeId) {
      return getNodeItems(
        contextMenu.nodeId,
        () => handleDuplicateNode(contextMenu.nodeId!),
        () => handleDeleteNode(contextMenu.nodeId!),
      );
    }

    return getCanvasItems(
      () => reactFlowInstance?.fitView({ padding: 0.3, duration: 300 }),
      () => reactFlowInstance?.zoomIn({ duration: 200 }),
      () => reactFlowInstance?.zoomOut({ duration: 200 }),
    );
  }, [contextMenu, getNodeItems, getCanvasItems, handleDuplicateNode, handleDeleteNode, reactFlowInstance]);

  return (
    <div
      className="flex flex-col h-full"
      onKeyDown={onKeyDown}
      tabIndex={0}
    >
      <BuilderToolbar
        workflowName={workflowName}
        isSaving={isSaving}
        isValidating={isValidating}
        isExecuting={isExecuting}
        onSave={handleSave}
        onValidate={onValidate}
        onExecute={onExecute}
        hasUnsavedChanges={hasUnsavedChanges}
      />

      <div className="flex flex-1 min-h-0">
        <NodePalette />

        <div ref={reactFlowWrapper} className="flex-1">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChangeWrapped}
            onEdgesChange={onEdgesChangeWrapped}
            onConnect={onConnect}
            onInit={setReactFlowInstance}
            onDragOver={onDragOver}
            onDrop={onDrop}
            onNodeContextMenu={onNodeContextMenu}
            onPaneContextMenu={onPaneContextMenu}
            onPaneClick={closeContextMenu}
            onNodeClick={closeContextMenu}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            defaultEdgeOptions={defaultEdgeOptions}
            isValidConnection={isValidConnection}
            connectionLineType={ConnectionLineType.SmoothStep}
            connectionLineStyle={{
              stroke: "var(--neurex-accent)",
              strokeWidth: 2,
              strokeDasharray: "6 3",
            }}
            fitView
            fitViewOptions={{ padding: 0.3 }}
            snapToGrid
            snapGrid={[16, 16]}
            deleteKeyCode={["Delete", "Backspace"]}
            multiSelectionKeyCode="Shift"
            proOptions={{ hideAttribution: true }}
            style={{
              backgroundColor: "var(--neurex-bg-base)",
            }}
          >
            <Background
              variant={BackgroundVariant.Dots}
              gap={24}
              size={1}
              color="var(--neurex-border-default)"
            />
            <MiniMap
              nodeStrokeWidth={3}
              maskColor="hsla(228, 14%, 7%, 0.85)"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
                borderRadius: 8,
              }}
              pannable
              zoomable
            />
          </ReactFlow>
        </div>
      </div>

      {/* Context Menu */}
      {contextMenu && (
        <ContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          items={contextMenuItems}
          onClose={closeContextMenu}
        />
      )}
    </div>
  );
}

// Wrap in provider for external access
export function BuilderCanvas(props: BuilderCanvasProps) {
  return (
    <ReactFlowProvider>
      <BuilderCanvasInner {...props} />
    </ReactFlowProvider>
  );
}
