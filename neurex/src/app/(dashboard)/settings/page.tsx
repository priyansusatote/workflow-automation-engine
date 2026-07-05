"use client";

import { useAuthStore } from "@/stores/auth-store";
import { useLogout } from "@/hooks/use-auth";
import { motion } from "framer-motion";
import { User, LogOut, Loader2 } from "lucide-react";

export default function SettingsPage() {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      <div className="mb-8">
        <h1
          className="text-2xl font-semibold tracking-tight"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Settings
        </h1>
        <p
          className="text-sm mt-1"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          Manage your account preferences
        </p>
      </div>

      <div className="max-w-lg space-y-6">
        {/* Profile */}
        <div
          className="p-6 rounded-xl"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          <div className="flex items-center gap-4 mb-5">
            <div
              className="w-12 h-12 rounded-full flex items-center justify-center"
              style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
            >
              <User className="w-5 h-5" style={{ color: "var(--neurex-accent)" }} />
            </div>
            <div>
              <p
                className="text-sm font-medium"
                style={{ color: "var(--neurex-text-primary)" }}
              >
                {user?.email || "—"}
              </p>
              <p
                className="text-xs font-mono mt-0.5"
                style={{ color: "var(--neurex-text-tertiary)" }}
              >
                {user?.role || "—"}
              </p>
            </div>
          </div>
        </div>

        {/* Logout */}
        <button
          onClick={() => logout.mutate()}
          disabled={logout.isPending}
          className="neurex-btn-danger flex items-center gap-2"
        >
          {logout.isPending ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <LogOut className="w-4 h-4" />
          )}
          Sign out
        </button>
      </div>
    </motion.div>
  );
}
