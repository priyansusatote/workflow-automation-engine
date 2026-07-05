"use client";

import { RoleGuard } from "@/components/auth/role-guard";
import { Shield } from "lucide-react";
import { motion } from "framer-motion";

function AdminContent() {
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
          Admin Panel
        </h1>
        <p
          className="text-sm mt-1"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          Platform administration
        </p>
      </div>

      <div
        className="flex flex-col items-center justify-center py-20 rounded-2xl"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
        }}
      >
        <div
          className="w-16 h-16 rounded-2xl flex items-center justify-center mb-6"
          style={{ backgroundColor: "var(--neurex-accent-subtle)" }}
        >
          <Shield className="w-8 h-8" style={{ color: "var(--neurex-accent)" }} />
        </div>
        <h2
          className="text-lg font-semibold mb-2"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Coming Soon
        </h2>
        <p
          className="text-sm max-w-md text-center"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          The admin panel is being built. Platform-wide workflow management,
          user administration, and system metrics will be available here.
        </p>
      </div>
    </motion.div>
  );
}

function AccessDenied() {
  return (
    <div
      className="flex flex-col items-center justify-center py-20 rounded-2xl"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
    >
      <h2
        className="text-lg font-semibold mb-2"
        style={{ color: "var(--neurex-error)" }}
      >
        Access Denied
      </h2>
      <p
        className="text-sm"
        style={{ color: "var(--neurex-text-secondary)" }}
      >
        You need administrator privileges to access this page.
      </p>
    </div>
  );
}

export default function AdminPage() {
  return (
    <RoleGuard requiredRole="ADMIN" fallback={<AccessDenied />}>
      <AdminContent />
    </RoleGuard>
  );
}
