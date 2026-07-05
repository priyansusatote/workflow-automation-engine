import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { executionsApi } from "@/api/executions";

export const executionKeys = {
  all: ["executions"] as const,
  lists: () => [...executionKeys.all, "list"] as const,
  list: (page: number, size: number) =>
    [...executionKeys.lists(), { page, size }] as const,
  details: () => [...executionKeys.all, "detail"] as const,
  detail: (id: string) => [...executionKeys.details(), id] as const,
  tasks: (id: string) => [...executionKeys.all, "tasks", id] as const,
  byWorkflow: (workflowId: string, page: number) =>
    [...executionKeys.all, "byWorkflow", workflowId, page] as const,
};

export function useExecutions(page = 0, size = 20) {
  return useQuery({
    queryKey: executionKeys.list(page, size),
    queryFn: () => executionsApi.getAll(page, size),
  });
}

export function useExecution(id: string) {
  return useQuery({
    queryKey: executionKeys.detail(id),
    queryFn: () => executionsApi.getById(id),
    enabled: !!id && id !== "undefined",
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === "RUNNING" || status === "WAITING" ? 3000 : false;
    },
  });
}

export function useTaskExecutions(executionId: string) {
  return useQuery({
    queryKey: executionKeys.tasks(executionId),
    queryFn: () => executionsApi.getTaskExecutions(executionId),
    enabled: !!executionId,
    refetchInterval: (query) => {
      const tasks = query.state.data;
      const hasRunning = tasks?.some(
        (t) => t.status === "RUNNING" || t.status === "WAITING"
      );
      return hasRunning ? 3000 : false;
    },
  });
}

export function useWorkflowExecutions(workflowId: string, page = 0) {
  return useQuery({
    queryKey: executionKeys.byWorkflow(workflowId, page),
    queryFn: () => executionsApi.getByWorkflow(workflowId, page),
    enabled: !!workflowId,
  });
}

export function useResumeExecution() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => executionsApi.resume(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: executionKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: executionKeys.tasks(id) });
      queryClient.invalidateQueries({ queryKey: executionKeys.lists() });
    },
  });
}
