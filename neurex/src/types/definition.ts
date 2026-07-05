// ============================================
// Definition Types — Mirrors backend WorkflowDefinitionRequest
// ============================================

export interface WorkflowNode {
  id: string;
  type: string;
  config?: Record<string, unknown>;
}

export interface WorkflowEdge {
  from: string;
  to: string;
  condition?: string;
}

export interface WorkflowDefinitionRequest {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
}
