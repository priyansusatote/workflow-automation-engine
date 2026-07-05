import { useQuery } from "@tanstack/react-query";
import { dashboardApi } from "@/api/dashboard";

export const dashboardKeys = {
  all: ["dashboard"] as const,
  stats: () => [...dashboardKeys.all, "stats"] as const,
};

export function useDashboardStats() {
  return useQuery({
    queryKey: dashboardKeys.stats(),
    queryFn: dashboardApi.getStats,
    refetchInterval: 30000, // Refresh every 30s
    staleTime: 10000,
  });
}
