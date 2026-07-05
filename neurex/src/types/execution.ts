// ============================================
// Execution Types — Mirrors backend execution DTOs
// ============================================

export type ExecutionStatus = "RUNNING" | "SUCCESS" | "FAILED" | "WAITING";

/**
 * Matches backend WorkflowExecutionResponse record:
 *   executionId, workflowId, status, errorMessage, createdAt, updatedAt
 */
export interface WorkflowExecutionResponse {
  executionId: string;
  workflowId: string;
  status: ExecutionStatus;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Matches backend ExecutionSummaryResponse record:
 *   executionId, workflowId, workflowName, status, createdAt, updatedAt
 */
export interface ExecutionSummaryResponse {
  executionId: string;
  workflowId: string;
  workflowName: string;
  status: ExecutionStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Matches backend TaskExecutionResponse record:
 *   nodeId, nodeType, status, logMessage, outputData, createdAt
 */
export interface TaskExecutionResponse {
  nodeId: string;
  nodeType: string;
  status: ExecutionStatus;
  logMessage: string | null;
  outputData: Record<string, unknown> | null;
  createdAt: string;
}

export interface ExecuteWorkflowResponse {
  message: string;
  executionId: string;
}

export interface ExecutionFilters {
  workflowId?: string;
  status?: ExecutionStatus;
  page?: number;
  size?: number;
  sort?: string;
}
