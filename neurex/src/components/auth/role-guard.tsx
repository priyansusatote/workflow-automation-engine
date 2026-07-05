"use client";

import { useRole } from "@/hooks/use-role";
import type { UserRole } from "@/types/auth";

interface RoleGuardProps {
  requiredRole: UserRole;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function RoleGuard({ requiredRole, children, fallback }: RoleGuardProps) {
  const { hasRole } = useRole();

  if (!hasRole(requiredRole)) {
    return fallback ? <>{fallback}</> : null;
  }

  return <>{children}</>;
}
