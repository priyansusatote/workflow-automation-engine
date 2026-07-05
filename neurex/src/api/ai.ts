import { apiClient } from "./client";

const BASE = "/ai/workflows";

export interface GenerateWorkflowRequest {
  workflowName: string;
  prompt: string;
}

export interface GeneratedWorkflowResponse {
  workflowId: string;
  workflowName: string;
  workflowJson: Record<string, unknown>;
}

export const aiApi = {
  /**
   * Preview — generate a workflow definition from a prompt.
   * Returns the raw JSON string of the definition.
   */
  generate: async (prompt: string): Promise<string> => {
    const response = await apiClient.post<string>(
      `${BASE}/generate`,
      { prompt }
    );
    return response.data;
  },

  /**
   * Generate a workflow from a prompt AND persist it immediately.
   * Returns the created workflow ID + definition JSON.
   */
  generateAndSave: async (
    data: GenerateWorkflowRequest
  ): Promise<GeneratedWorkflowResponse> => {
    const response = await apiClient.post<GeneratedWorkflowResponse>(
      `${BASE}/generate-and-save`,
      data
    );
    return response.data;
  },
};
