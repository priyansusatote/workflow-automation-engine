import { apiClient } from "./client";
import type { DashboardResponse, WorkflowStatsResponse } from "@/types/dashboard";

const BASE = "/api/v1/dashboard";

export const dashboardApi = {
  getStats: async (): Promise<DashboardResponse> => {
    const response = await apiClient.get<DashboardResponse>(BASE);
    return response.data;
  },

  getWorkflowStats: async (): Promise<WorkflowStatsResponse[]> => {
    const response = await apiClient.get<WorkflowStatsResponse[]>(
      `${BASE}/workflow-stats`
    );
    return response.data;
  },
};
