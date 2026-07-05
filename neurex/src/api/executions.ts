import { apiClient } from "./client";
import type {
  WorkflowExecutionResponse,
  ExecutionSummaryResponse,
  TaskExecutionResponse,
  ExecutionStatus,
} from "@/types/execution";
import type { Paginated } from "@/types/common";

const BASE = "/api/v1/workflows";

export const executionsApi = {
  getAll: async (
    page = 0,
    size = 10,
    workflowId?: string,
    status?: ExecutionStatus
  ): Promise<Paginated<ExecutionSummaryResponse>> => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    params.set("sort", "createdAt,desc");
    if (workflowId) params.set("workflowId", workflowId);
    if (status) params.set("status", status);

    const response = await apiClient.get<Paginated<ExecutionSummaryResponse>>(
      `${BASE}/executions?${params.toString()}`
    );
    return response.data;
  },

  getById: async (id: string): Promise<WorkflowExecutionResponse> => {
    const response = await apiClient.get<WorkflowExecutionResponse>(
      `${BASE}/executions/${id}`
    );
    return response.data;
  },

  getTaskExecutions: async (id: string): Promise<TaskExecutionResponse[]> => {
    const response = await apiClient.get<TaskExecutionResponse[]>(
      `${BASE}/executions/${id}/tasks`
    );
    return response.data;
  },

  getByWorkflow: async (
    workflowId: string,
    page = 0,
    size = 10
  ): Promise<Paginated<ExecutionSummaryResponse>> => {
    return executionsApi.getAll(page, size, workflowId);
  },

  resume: async (id: string): Promise<void> => {
    await apiClient.post(`${BASE}/executions/${id}/resume`);
  },
};
