// ============================================
// Dashboard Types — Mirrors backend DashboardResponse
// ============================================

export interface DashboardResponse {
  totalWorkflows: number;
  activeWorkflows: number;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  runningExecutions: number;
  successRate: number;
}

export interface WorkflowStatsResponse {
  workflowId: string;
  workflowName: string;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  successRate: number;
}
