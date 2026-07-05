// ============================================
// AI Types — Mirrors backend AI DTOs
// ============================================

export interface GenerateWorkflowRequest {
  prompt: string;
}

export interface GenerateAndSaveRequest {
  workflowName: string;
  prompt: string;
}

export interface GeneratedWorkflowResponse {
  workflowId: string;
  workflowName: string;
  workflow: Record<string, unknown>;
}
