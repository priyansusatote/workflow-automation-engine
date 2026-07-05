"use client";

import { useEffect, useState } from "react";
import { useAuthStore } from "@/stores/auth-store";
import { authApi } from "@/api/auth";

/**
 * Attempts to restore auth session on mount by calling /auth/refresh.
 * The refresh token is in an httpOnly cookie, so the browser sends it automatically.
 * On success, populates the auth store with the new access token (which contains user info).
 */
export function useAuthInit() {
  const [isLoading, setIsLoading] = useState(true);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const setAccessToken = useAuthStore((s) => s.setAccessToken);

  useEffect(() => {
    // If already authenticated (e.g., just logged in), skip
    if (isAuthenticated) {
      setIsLoading(false);
      return;
    }

    const restoreSession = async () => {
      try {
        const response = await authApi.refresh();
        setAccessToken(response.accessToken);
      } catch {
        // No valid refresh token — user is not authenticated
        // This is fine for protected pages; middleware will redirect
      } finally {
        setIsLoading(false);
      }
    };

    restoreSession();
  }, [isAuthenticated, setAccessToken]);

  return { isLoading };
}
