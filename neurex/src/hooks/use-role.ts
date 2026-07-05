import { useAuthStore } from "@/stores/auth-store";
import type { UserRole } from "@/types/auth";

export function useRole() {
  const role = useAuthStore((s) => s.role);

  return {
    role,
    isAdmin: role === "ADMIN",
    isUser: role === "USER",
    hasRole: (required: UserRole): boolean => {
      if (!role) return false;
      if (required === "USER") return true; // All authenticated users are at least USER
      if (required === "ADMIN") return role === "ADMIN";
      return false;
    },
  };
}
