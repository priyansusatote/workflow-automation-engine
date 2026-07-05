/**
 * ExecutionDataProvider — Abstraction over execution data fetching.
 *
 * Today:  Polling with TanStack Query refetchInterval
 * Future: SSE or WebSocket — swap strategy here, UI stays the same.
 *
 * This provider gives components:
 * - execution: The execution record
 * - tasks: Array of task executions
 * - isLive: Whether data is actively being polled (RUNNING/WAITING)
 * - refresh(): Manual refetch trigger
 */

"use client";

import React, { createContext, useContext, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
  useExecution,
  useTaskExecutions,
  executionKeys,
} from "@/hooks/use-executions";
import type {
  WorkflowExecutionResponse,
  TaskExecutionResponse,
} from "@/types/execution";

interface ExecutionDataContextType {
  execution: WorkflowExecutionResponse | undefined;
  tasks: TaskExecutionResponse[] | undefined;
  isLoading: boolean;
  isLive: boolean;
  error: Error | null;
  refresh: () => void;
}

const ExecutionDataContext = createContext<ExecutionDataContextType | null>(null);

export function ExecutionDataProvider({
  executionId,
  children,
}: {
  executionId: string;
  children: React.ReactNode;
}) {
  const queryClient = useQueryClient();

  const {
    data: execution,
    isLoading: execLoading,
    error: execError,
  } = useExecution(executionId);

  const {
    data: tasks,
    isLoading: tasksLoading,
    error: tasksError,
  } = useTaskExecutions(executionId);

  const isLive =
    execution?.status === "RUNNING" || execution?.status === "WAITING";

  const refresh = useCallback(() => {
    queryClient.invalidateQueries({
      queryKey: executionKeys.detail(executionId),
    });
    queryClient.invalidateQueries({
      queryKey: executionKeys.tasks(executionId),
    });
  }, [queryClient, executionId]);

  return (
    <ExecutionDataContext.Provider
      value={{
        execution,
        tasks,
        isLoading: execLoading || tasksLoading,
        isLive,
        error: (execError || tasksError) as Error | null,
        refresh,
      }}
    >
      {children}
    </ExecutionDataContext.Provider>
  );
}

export function useExecutionData() {
  const ctx = useContext(ExecutionDataContext);
  if (!ctx) {
    throw new Error("useExecutionData must be used within ExecutionDataProvider");
  }
  return ctx;
}
