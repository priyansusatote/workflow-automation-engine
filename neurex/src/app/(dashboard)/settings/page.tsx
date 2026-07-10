"use client";

import { useState } from "react";
import { useAuthStore } from "@/stores/auth-store";
import { useLogout } from "@/hooks/use-auth";
import { useToast } from "@/components/ui/toast";
import { motion, AnimatePresence } from "framer-motion";
import {
  User,
  LogOut,
  Loader2,
  Shield,
  Bell,
  Monitor,
  Key,
  Copy,
  Check,
  Eye,
  EyeOff,
  AlertTriangle,
  Palette,
  Mail,
  Clock,
  ChevronRight,
} from "lucide-react";

/* ───── Section Card ───── */
function SettingsCard({
  title,
  description,
  icon: Icon,
  children,
  accent,
}: {
  title: string;
  description: string;
  icon: React.ElementType;
  children: React.ReactNode;
  accent?: string;
}) {
  return (
    <div
      className="rounded-xl overflow-hidden"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
      }}
    >
      <div
        className="flex items-center gap-3 px-6 py-4"
        style={{
          borderBottom: "1px solid var(--neurex-border-default)",
        }}
      >
        <div
          className="w-8 h-8 rounded-lg flex items-center justify-center"
          style={{
            backgroundColor: accent || "var(--neurex-accent-subtle)",
          }}
        >
          <Icon
            className="w-4 h-4"
            style={{
              color: accent
                ? "white"
                : "var(--neurex-accent)",
            }}
          />
        </div>
        <div>
          <h3
            className="text-sm font-semibold"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            {title}
          </h3>
          <p
            className="text-xs"
            style={{ color: "var(--neurex-text-tertiary)" }}
          >
            {description}
          </p>
        </div>
      </div>
      <div className="px-6 py-5">{children}</div>
    </div>
  );
}

/* ───── Toggle Switch ───── */
function Toggle({
  checked,
  onChange,
}: {
  checked: boolean;
  onChange: (val: boolean) => void;
}) {
  return (
    <button
      onClick={() => onChange(!checked)}
      className="relative w-10 h-[22px] rounded-full transition-colors duration-200 shrink-0"
      style={{
        backgroundColor: checked
          ? "var(--neurex-accent)"
          : "var(--neurex-bg-subtle)",
      }}
    >
      <motion.div
        className="absolute top-[3px] w-4 h-4 rounded-full bg-white"
        animate={{ left: checked ? 20 : 3 }}
        transition={{ type: "spring", stiffness: 500, damping: 30 }}
      />
    </button>
  );
}

/* ───── Preference Row ───── */
function PrefRow({
  label,
  description,
  checked,
  onChange,
}: {
  label: string;
  description: string;
  checked: boolean;
  onChange: (val: boolean) => void;
}) {
  return (
    <div className="flex items-center justify-between py-3">
      <div>
        <p
          className="text-sm font-medium"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          {label}
        </p>
        <p
          className="text-xs mt-0.5"
          style={{ color: "var(--neurex-text-tertiary)" }}
        >
          {description}
        </p>
      </div>
      <Toggle checked={checked} onChange={onChange} />
    </div>
  );
}

/* ═══════════════════════════════════════════
   Main Settings Page
   ═══════════════════════════════════════════ */
export default function SettingsPage() {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();
  const { success } = useToast();

  // Local preference state (no backend for these yet — frontend-only)
  const [emailOnFailure, setEmailOnFailure] = useState(true);
  const [emailOnSuccess, setEmailOnSuccess] = useState(false);
  const [showApiKey, setShowApiKey] = useState(false);
  const [copiedKey, setCopiedKey] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);

  // Mock API key for display
  const apiKey = "nrx_sk_" + (user?.email?.replace(/[^a-z0-9]/gi, "") || "demo").slice(0, 8) + "...redacted";

  const handleCopyKey = () => {
    navigator.clipboard.writeText("nrx_sk_mock_key_placeholder");
    setCopiedKey(true);
    success("API key copied to clipboard");
    setTimeout(() => setCopiedKey(false), 2000);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      {/* Header */}
      <div className="mb-8">
        <h1
          className="text-2xl font-bold tracking-tight"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Settings
        </h1>
        <p
          className="text-sm mt-1"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          Manage your account, preferences, and API access
        </p>
      </div>

      <div className="max-w-2xl space-y-6">
        {/* ─── Profile ─── */}
        <SettingsCard
          title="Profile"
          description="Your account information"
          icon={User}
        >
          <div className="space-y-4">
            {/* Avatar + email */}
            <div className="flex items-center gap-4">
              <div
                className="w-14 h-14 rounded-full flex items-center justify-center text-lg font-bold"
                style={{
                  background: "linear-gradient(135deg, var(--neurex-accent), hsl(262, 83%, 78%))",
                  color: "white",
                }}
              >
                {user?.email?.charAt(0).toUpperCase() || "U"}
              </div>
              <div className="flex-1 min-w-0">
                <p
                  className="text-base font-semibold truncate"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {user?.email || "—"}
                </p>
                <div className="flex items-center gap-2 mt-1">
                  <span
                    className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium"
                    style={{
                      backgroundColor: "var(--neurex-accent-subtle)",
                      color: "var(--neurex-accent)",
                    }}
                  >
                    <Shield className="w-3 h-3" />
                    {user?.role || "USER"}
                  </span>
                </div>
              </div>
            </div>

            {/* Info rows */}
            <div
              className="pt-4 space-y-3"
              style={{ borderTop: "1px solid var(--neurex-border-default)" }}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Mail
                    className="w-4 h-4"
                    style={{ color: "var(--neurex-text-ghost)" }}
                  />
                  <span
                    className="text-sm"
                    style={{ color: "var(--neurex-text-secondary)" }}
                  >
                    Email
                  </span>
                </div>
                <span
                  className="text-sm font-mono"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {user?.email || "—"}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Clock
                    className="w-4 h-4"
                    style={{ color: "var(--neurex-text-ghost)" }}
                  />
                  <span
                    className="text-sm"
                    style={{ color: "var(--neurex-text-secondary)" }}
                  >
                    Member since
                  </span>
                </div>
                <span
                  className="text-sm"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {new Date().toLocaleDateString("en-US", {
                    month: "long",
                    year: "numeric",
                  })}
                </span>
              </div>
            </div>
          </div>
        </SettingsCard>

        {/* ─── API Key ─── */}
        <SettingsCard
          title="API Access"
          description="Manage your API keys for programmatic access"
          icon={Key}
        >
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div
                className="flex-1 flex items-center gap-2 px-3 py-2.5 rounded-lg font-mono text-sm"
                style={{
                  backgroundColor: "var(--neurex-bg-base)",
                  border: "1px solid var(--neurex-border-default)",
                  color: "var(--neurex-text-secondary)",
                }}
              >
                <span className="truncate">
                  {showApiKey ? apiKey : "nrx_sk_••••••••••••••••"}
                </span>
              </div>
              <button
                onClick={() => setShowApiKey(!showApiKey)}
                className="p-2.5 rounded-lg transition-colors"
                style={{
                  backgroundColor: "var(--neurex-bg-base)",
                  border: "1px solid var(--neurex-border-default)",
                  color: "var(--neurex-text-secondary)",
                }}
              >
                {showApiKey ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
              <button
                onClick={handleCopyKey}
                className="p-2.5 rounded-lg transition-colors"
                style={{
                  backgroundColor: copiedKey
                    ? "var(--neurex-success-subtle)"
                    : "var(--neurex-bg-base)",
                  border: `1px solid ${copiedKey ? "var(--neurex-success)" : "var(--neurex-border-default)"}`,
                  color: copiedKey
                    ? "var(--neurex-success)"
                    : "var(--neurex-text-secondary)",
                }}
              >
                {copiedKey ? (
                  <Check className="w-4 h-4" />
                ) : (
                  <Copy className="w-4 h-4" />
                )}
              </button>
            </div>
            <p
              className="text-xs"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              Use this key to authenticate API requests. Keep it secret — do not
              share it publicly.
            </p>
          </div>
        </SettingsCard>

        {/* ─── Notifications ─── */}
        <SettingsCard
          title="Notifications"
          description="Configure when you receive alerts"
          icon={Bell}
        >
          <div
            className="divide-y"
            style={{ borderColor: "var(--neurex-border-default)" }}
          >
            <PrefRow
              label="Workflow failure alerts"
              description="Email notification when a workflow execution fails"
              checked={emailOnFailure}
              onChange={setEmailOnFailure}
            />
            <PrefRow
              label="Execution success alerts"
              description="Email notification on successful completions"
              checked={emailOnSuccess}
              onChange={setEmailOnSuccess}
            />
          </div>
        </SettingsCard>

        {/* ─── Appearance ─── */}
        <SettingsCard
          title="Appearance"
          description="Customize the look and feel"
          icon={Palette}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div
                className="w-10 h-10 rounded-lg flex items-center justify-center"
                style={{
                  backgroundColor: "var(--neurex-bg-base)",
                  border: "1px solid var(--neurex-accent)",
                }}
              >
                <Monitor
                  className="w-5 h-5"
                  style={{ color: "var(--neurex-accent)" }}
                />
              </div>
              <div>
                <p
                  className="text-sm font-medium"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Dark Mode
                </p>
                <p
                  className="text-xs"
                  style={{ color: "var(--neurex-text-tertiary)" }}
                >
                  Currently active
                </p>
              </div>
            </div>
            <span
              className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
              style={{
                backgroundColor: "var(--neurex-success-subtle)",
                color: "var(--neurex-success)",
              }}
            >
              <Check className="w-3 h-3" />
              Active
            </span>
          </div>
        </SettingsCard>

        {/* ─── Danger Zone ─── */}
        <div
          className="rounded-xl overflow-hidden"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid var(--neurex-error-subtle)",
          }}
        >
          <div
            className="flex items-center gap-3 px-6 py-4"
            style={{
              borderBottom: "1px solid var(--neurex-error-subtle)",
            }}
          >
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center"
              style={{ backgroundColor: "var(--neurex-error-subtle)" }}
            >
              <AlertTriangle
                className="w-4 h-4"
                style={{ color: "var(--neurex-error)" }}
              />
            </div>
            <div>
              <h3
                className="text-sm font-semibold"
                style={{ color: "var(--neurex-error)" }}
              >
                Danger Zone
              </h3>
              <p
                className="text-xs"
                style={{ color: "var(--neurex-text-tertiary)" }}
              >
                Irreversible actions
              </p>
            </div>
          </div>
          <div className="px-6 py-5 space-y-4">
            {/* Sign out */}
            <div className="flex items-center justify-between">
              <div>
                <p
                  className="text-sm font-medium"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Sign out
                </p>
                <p
                  className="text-xs"
                  style={{ color: "var(--neurex-text-tertiary)" }}
                >
                  End your current session
                </p>
              </div>
              <button
                onClick={() => logout.mutate()}
                disabled={logout.isPending}
                className="neurex-btn-danger flex items-center gap-2 text-sm"
              >
                {logout.isPending ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <LogOut className="w-4 h-4" />
                )}
                Sign out
              </button>
            </div>

            <div
              style={{
                borderTop: "1px solid var(--neurex-error-subtle)",
              }}
            />

            {/* Delete account */}
            <div className="flex items-center justify-between">
              <div>
                <p
                  className="text-sm font-medium"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Delete account
                </p>
                <p
                  className="text-xs"
                  style={{ color: "var(--neurex-text-tertiary)" }}
                >
                  Permanently delete your account and all data
                </p>
              </div>
              {!deleteConfirm ? (
                <button
                  onClick={() => setDeleteConfirm(true)}
                  className="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
                  style={{
                    border: "1px solid var(--neurex-error-subtle)",
                    color: "var(--neurex-error)",
                    backgroundColor: "transparent",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = "var(--neurex-error-subtle)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = "transparent";
                  }}
                >
                  Delete Account
                </button>
              ) : (
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setDeleteConfirm(false)}
                    className="px-3 py-2 rounded-lg text-sm font-medium"
                    style={{
                      color: "var(--neurex-text-secondary)",
                      border: "1px solid var(--neurex-border-default)",
                    }}
                  >
                    Cancel
                  </button>
                  <button
                    className="neurex-btn-danger flex items-center gap-2 text-sm"
                  >
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Confirm Delete
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
