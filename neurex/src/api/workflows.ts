import { apiClient } from "./client";
import type { WorkflowRequest, WorkflowResponse } from "@/types/workflow";
import type {
  WorkflowDefinitionRequest,
} from "@/types/definition";

const BASE = "/api/v1/workflows";

export const workflowsApi = {
  getAll: async (): Promise<WorkflowResponse[]> => {
    const response = await apiClient.get<WorkflowResponse[]>(BASE);
    return response.data;
  },

  getById: async (id: string): Promise<WorkflowResponse> => {
    const response = await apiClient.get<WorkflowResponse>(`${BASE}/${id}`);
    return response.data;
  },

  create: async (data: WorkflowRequest): Promise<WorkflowResponse> => {
    const response = await apiClient.post<WorkflowResponse>(BASE, data);
    return response.data;
  },

  update: async (
    id: string,
    data: WorkflowRequest
  ): Promise<WorkflowResponse> => {
    const response = await apiClient.put<WorkflowResponse>(
      `${BASE}/${id}`,
      data
    );
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE}/${id}`);
  },

  activate: async (id: string): Promise<WorkflowResponse> => {
    const response = await apiClient.put<WorkflowResponse>(
      `${BASE}/${id}/activate`
    );
    return response.data;
  },

  deactivate: async (id: string): Promise<WorkflowResponse> => {
    const response = await apiClient.put<WorkflowResponse>(
      `${BASE}/${id}/deactivate`
    );
    return response.data;
  },

  getDefinition: async (id: string): Promise<string> => {
    const response = await apiClient.get<string>(
      `${BASE}/${id}/definition`
    );
    return response.data;
  },

  saveDefinition: async (
    id: string,
    data: WorkflowDefinitionRequest
  ): Promise<void> => {
    await apiClient.post(`${BASE}/${id}/definition`, data);
  },

  validate: async (
    id: string
  ): Promise<{ valid: boolean; errors: string[] }> => {
    const response = await apiClient.post<{
      valid: boolean;
      errors: string[];
    }>(`${BASE}/${id}/validate`);
    return response.data;
  },

  execute: async (
    id: string,
    inputData?: Record<string, unknown>
  ): Promise<{ message: string; executionId: string }> => {
    const response = await apiClient.post<{
      message: string;
      executionId: string;
    }>(`${BASE}/${id}/execute`, inputData || {});
    return response.data;
  },
};
