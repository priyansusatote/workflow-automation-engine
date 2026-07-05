// ============================================
// Workflow Types — Mirrors backend WorkflowRequest/Response
// ============================================

export type WorkflowStatus = "ACTIVE" | "INACTIVE";

export interface WorkflowRequest {
  name: string;
  description: string;
}

export interface WorkflowResponse {
  id: string;
  name: string;
  description: string;
  status: WorkflowStatus;
  userId: string;
  createdAt: string;
  updatedAt: string;
}
