import { useMutation } from "@tanstack/react-query";
import { aiApi } from "@/api/ai";
import type { GenerateWorkflowRequest } from "@/api/ai";

/**
 * Preview-only generation — returns raw JSON string for preview.
 */
export function useGenerateWorkflow() {
  return useMutation({
    mutationFn: (prompt: string) => aiApi.generate(prompt),
  });
}

/**
 * Generate + persist in one shot. Returns created workflow ID.
 */
export function useGenerateAndSaveWorkflow() {
  return useMutation({
    mutationFn: (data: GenerateWorkflowRequest) =>
      aiApi.generateAndSave(data),
  });
}
