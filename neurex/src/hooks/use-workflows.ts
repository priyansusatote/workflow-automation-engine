import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workflowsApi } from "@/api/workflows";
import type { WorkflowRequest } from "@/types/workflow";
import type { WorkflowDefinitionRequest } from "@/types/definition";

// ─── Query keys ─────────────────────────────
export const workflowKeys = {
  all: ["workflows"] as const,
  lists: () => [...workflowKeys.all, "list"] as const,
  details: () => [...workflowKeys.all, "detail"] as const,
  detail: (id: string) => [...workflowKeys.details(), id] as const,
  definitions: () => [...workflowKeys.all, "definition"] as const,
  definition: (id: string) => [...workflowKeys.definitions(), id] as const,
};

// ─── Queries ────────────────────────────────
export function useWorkflows() {
  return useQuery({
    queryKey: workflowKeys.lists(),
    queryFn: workflowsApi.getAll,
  });
}

export function useWorkflow(id: string) {
  return useQuery({
    queryKey: workflowKeys.detail(id),
    queryFn: () => workflowsApi.getById(id),
    enabled: !!id,
  });
}

export function useWorkflowDefinition(id: string) {
  return useQuery({
    queryKey: workflowKeys.definition(id),
    queryFn: () => workflowsApi.getDefinition(id),
    enabled: !!id,
  });
}

// ─── Mutations ──────────────────────────────
export function useCreateWorkflow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: WorkflowRequest) => workflowsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.lists() });
    },
  });
}

export function useUpdateWorkflow(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: WorkflowRequest) => workflowsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: workflowKeys.lists() });
    },
  });
}

export function useDeleteWorkflow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workflowsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.lists() });
    },
  });
}

export function useActivateWorkflow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workflowsApi.activate(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: workflowKeys.lists() });
    },
  });
}

export function useDeactivateWorkflow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workflowsApi.deactivate(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: workflowKeys.lists() });
    },
  });
}

export function useSaveDefinition(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: WorkflowDefinitionRequest) =>
      workflowsApi.saveDefinition(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: workflowKeys.definition(id) });
    },
  });
}

export function useExecuteWorkflow() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      workflowId,
      inputData,
    }: {
      workflowId: string;
      inputData?: Record<string, unknown>;
    }) => workflowsApi.execute(workflowId, inputData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["executions"] });
    },
  });
}

export function useValidateWorkflow() {
  return useMutation({
    mutationFn: (id: string) => workflowsApi.validate(id),
  });
}
