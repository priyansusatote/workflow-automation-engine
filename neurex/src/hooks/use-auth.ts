"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/auth-store";
import { authApi } from "@/api/auth";
import type { AuthRequest } from "@/types/auth";

export function useLogin() {
  const router = useRouter();
  const setAccessToken = useAuthStore((s) => s.setAccessToken);

  return useMutation({
    mutationFn: (data: AuthRequest) => authApi.login(data),
    onSuccess: (response) => {
      setAccessToken(response.accessToken);
      router.push("/dashboard");
    },
  });
}

export function useSignup() {
  const router = useRouter();

  return useMutation({
    mutationFn: (data: AuthRequest) => authApi.signup(data),
    onSuccess: () => {
      router.push("/login?registered=true");
    },
  });
}

export function useLogout() {
  const router = useRouter();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return useMutation({
    mutationFn: () => authApi.logout(),
    onSuccess: () => {
      clearAuth();
      router.push("/login");
    },
    onError: () => {
      // Even if logout API fails, clear local state
      clearAuth();
      router.push("/login");
    },
  });
}
