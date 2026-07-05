import { create } from "zustand";
import type { AuthUser, UserRole } from "@/types/auth";
import { jwtDecode } from "jwt-decode";

interface JwtPayload {
  sub: string; // email
  userId: string;
  role: string;
  exp: number;
  jti: string;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  role: UserRole | null;
  isAuthenticated: boolean;

  setAccessToken: (token: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  role: null,
  isAuthenticated: false,

  setAccessToken: (token: string) => {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      const user: AuthUser = {
        userId: decoded.userId,
        email: decoded.sub,
        role: decoded.role as UserRole,
      };
      set({
        accessToken: token,
        user,
        role: user.role,
        isAuthenticated: true,
      });
    } catch {
      set({
        accessToken: token,
        user: null,
        role: null,
        isAuthenticated: true,
      });
    }
  },

  clearAuth: () => {
    set({
      accessToken: null,
      user: null,
      role: null,
      isAuthenticated: false,
    });
  },
}));
