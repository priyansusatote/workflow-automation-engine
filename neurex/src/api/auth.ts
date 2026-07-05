import { apiClient } from "./client";
import type {
  AuthRequest,
  LoginResponse,
  SignupResponse,
  RefreshResponse,
  LogoutResponse,
} from "@/types/auth";

export const authApi = {
  login: async (data: AuthRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>("/auth/login", data);
    return response.data;
  },

  signup: async (data: AuthRequest): Promise<SignupResponse> => {
    const response = await apiClient.post<SignupResponse>("/auth/signup", data);
    return response.data;
  },

  refresh: async (): Promise<RefreshResponse> => {
    const response = await apiClient.post<RefreshResponse>("/auth/refresh");
    return response.data;
  },

  logout: async (): Promise<LogoutResponse> => {
    const response = await apiClient.post<LogoutResponse>("/auth/logout");
    return response.data;
  },
};
