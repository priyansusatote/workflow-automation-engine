"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { NeurexLogo } from "@/components/ui/neurex-logo";
import { motion, AnimatePresence } from "framer-motion";
import {
  LayoutDashboard,
  GitBranch,
  Play,
  Sparkles,
  Settings,
  Shield,
  ChevronLeft,
  LogOut,
  User,
} from "lucide-react";
import { useSidebarStore } from "@/stores/sidebar-store";
import { useAuthStore } from "@/stores/auth-store";
import { useLogout } from "@/hooks/use-auth";
import { useRole } from "@/hooks/use-role";
import { useFeatureFlag } from "@/hooks/use-feature-flag";
import { cn } from "@/lib/utils";

interface NavItemProps {
  href: string;
  icon: React.ElementType;
  label: string;
  badge?: string | number;
  isCollapsed: boolean;
}

function NavItem({ href, icon: Icon, label, badge, isCollapsed }: NavItemProps) {
  const pathname = usePathname();
  const isActive = pathname === href || pathname.startsWith(href + "/");

  return (
    <Link
      href={href}
      className={cn(
        "flex items-center gap-3 rounded-lg transition-all duration-200 group relative",
        isCollapsed ? "justify-center px-3 py-2.5" : "px-3 py-2.5"
      )}
      style={{
        backgroundColor: isActive
          ? "var(--neurex-accent-subtle)"
          : "transparent",
        color: isActive
          ? "var(--neurex-accent)"
          : "var(--neurex-text-secondary)",
      }}
      onMouseEnter={(e) => {
        if (!isActive) {
          e.currentTarget.style.backgroundColor = "var(--neurex-bg-subtle)";
          e.currentTarget.style.color = "var(--neurex-text-primary)";
        }
      }}
      onMouseLeave={(e) => {
        if (!isActive) {
          e.currentTarget.style.backgroundColor = "transparent";
          e.currentTarget.style.color = "var(--neurex-text-secondary)";
        }
      }}
    >
      <Icon className="w-[18px] h-[18px] shrink-0" />
      <AnimatePresence>
        {!isCollapsed && (
          <motion.span
            initial={{ opacity: 0, width: 0 }}
            animate={{ opacity: 1, width: "auto" }}
            exit={{ opacity: 0, width: 0 }}
            className="text-sm font-medium whitespace-nowrap overflow-hidden"
          >
            {label}
          </motion.span>
        )}
      </AnimatePresence>
      {badge && !isCollapsed && (
        <span
          className="ml-auto text-xs px-1.5 py-0.5 rounded-full font-mono"
          style={{
            backgroundColor: "var(--neurex-bg-subtle)",
            color: "var(--neurex-text-tertiary)",
            fontSize: "0.65rem",
          }}
        >
          {badge}
        </span>
      )}
      {/* Active indicator bar */}
      {isActive && (
        <motion.div
          layoutId="sidebar-active"
          className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 rounded-r-full"
          style={{ backgroundColor: "var(--neurex-accent)" }}
          transition={{ type: "spring", stiffness: 300, damping: 25 }}
        />
      )}
      {/* Tooltip for collapsed */}
      {isCollapsed && (
        <div
          className="absolute left-full ml-3 px-2.5 py-1.5 rounded-md text-xs font-medium whitespace-nowrap opacity-0 pointer-events-none group-hover:opacity-100 transition-opacity z-50"
          style={{
            backgroundColor: "var(--neurex-bg-overlay)",
            color: "var(--neurex-text-primary)",
            border: "1px solid var(--neurex-border-default)",
            boxShadow: "var(--shadow-md)",
          }}
        >
          {label}
        </div>
      )}
    </Link>
  );
}

export function Sidebar() {
  const { isCollapsed, toggle } = useSidebarStore();
  const user = useAuthStore((s) => s.user);
  const { isAdmin } = useRole();
  const aiEnabled = useFeatureFlag("AI_GENERATION");
  const logout = useLogout();

  return (
    <motion.aside
      className="fixed left-0 top-0 bottom-0 z-40 flex flex-col"
      animate={{
        width: isCollapsed ? 64 : 240,
      }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        borderRight: "1px solid var(--neurex-border-default)",
      }}
    >
      {/* Logo */}
      <div
        className={cn(
          "flex items-center h-14 shrink-0",
          isCollapsed ? "justify-center px-3" : "px-4 gap-3"
        )}
        style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
      >
          <NeurexLogo size={28} />
        <AnimatePresence>
          {!isCollapsed && (
            <motion.span
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="text-base font-bold tracking-tight"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Neurex
            </motion.span>
          )}
        </AnimatePresence>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-2 py-4 space-y-1 overflow-y-auto">
        <NavItem
          href="/dashboard"
          icon={LayoutDashboard}
          label="Dashboard"
          isCollapsed={isCollapsed}
        />
        <NavItem
          href="/workflows"
          icon={GitBranch}
          label="Workflows"
          isCollapsed={isCollapsed}
        />
        <NavItem
          href="/executions"
          icon={Play}
          label="Executions"
          isCollapsed={isCollapsed}
        />
        {aiEnabled && (
          <NavItem
            href="/ai/generate"
            icon={Sparkles}
            label="AI Generate"
            isCollapsed={isCollapsed}
          />
        )}
        <NavItem
          href="/settings"
          icon={Settings}
          label="Settings"
          isCollapsed={isCollapsed}
        />
        {isAdmin && (
          <>
            <div
              className="mx-3 my-3"
              style={{
                borderTop: "1px solid var(--neurex-border-default)",
              }}
            />
            <NavItem
              href="/admin"
              icon={Shield}
              label="Admin"
              isCollapsed={isCollapsed}
            />
          </>
        )}
      </nav>

      {/* Bottom section */}
      <div
        className="shrink-0 p-2 space-y-1"
        style={{ borderTop: "1px solid var(--neurex-border-default)" }}
      >
        {/* User info */}
        {user && !isCollapsed && (
          <div className="px-3 py-2">
            <p
              className="text-sm font-medium truncate"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              {user.email}
            </p>
            <p
              className="text-xs font-mono mt-0.5"
              style={{ color: "var(--neurex-text-tertiary)" }}
            >
              {user.role}
            </p>
          </div>
        )}

        {/* Collapse toggle */}
        <button
          onClick={toggle}
          className={cn(
            "flex items-center gap-3 rounded-lg transition-all duration-200 w-full",
            isCollapsed ? "justify-center px-3 py-2.5" : "px-3 py-2.5"
          )}
          style={{ color: "var(--neurex-text-tertiary)" }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = "var(--neurex-bg-subtle)";
            e.currentTarget.style.color = "var(--neurex-text-primary)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = "transparent";
            e.currentTarget.style.color = "var(--neurex-text-tertiary)";
          }}
        >
          <motion.div
            animate={{ rotate: isCollapsed ? 180 : 0 }}
            transition={{ duration: 0.2 }}
          >
            <ChevronLeft className="w-[18px] h-[18px]" />
          </motion.div>
          <AnimatePresence>
            {!isCollapsed && (
              <motion.span
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-sm font-medium"
              >
                Collapse
              </motion.span>
            )}
          </AnimatePresence>
        </button>

        {/* Logout */}
        <button
          onClick={() => logout.mutate()}
          className={cn(
            "flex items-center gap-3 rounded-lg transition-all duration-200 w-full",
            isCollapsed ? "justify-center px-3 py-2.5" : "px-3 py-2.5"
          )}
          style={{ color: "var(--neurex-text-tertiary)" }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = "var(--neurex-error-subtle)";
            e.currentTarget.style.color = "var(--neurex-error)";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = "transparent";
            e.currentTarget.style.color = "var(--neurex-text-tertiary)";
          }}
        >
          <LogOut className="w-[18px] h-[18px]" />
          <AnimatePresence>
            {!isCollapsed && (
              <motion.span
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-sm font-medium"
              >
                Logout
              </motion.span>
            )}
          </AnimatePresence>
        </button>
      </div>
    </motion.aside>
  );
}
