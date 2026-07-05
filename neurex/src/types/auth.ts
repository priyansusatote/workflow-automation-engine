// ============================================
// Auth Types — Mirrors backend AuthRequest, LoginResponse, UserRole
// ============================================

export type UserRole = "USER" | "ADMIN";

export interface AuthRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
}

export interface AuthUser {
  userId: string;
  email: string;
  role: UserRole;
}

export interface SignupResponse {
  message: string;
}

export interface LogoutResponse {
  message: string;
}

export interface RefreshResponse {
  accessToken: string;
}
