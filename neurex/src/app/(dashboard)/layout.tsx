"use client";

import { Sidebar } from "@/components/layout/sidebar";
import { TopBar } from "@/components/layout/topbar";
import { ErrorBoundary } from "@/components/ui/error-boundary";
import { useSidebarStore } from "@/stores/sidebar-store";
import { useAuthInit } from "@/hooks/use-auth-init";
import { NeurexLogo } from "@/components/ui/neurex-logo";
import { motion } from "framer-motion";
import { usePathname } from "next/navigation";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);
  const { isLoading } = useAuthInit();
  const pathname = usePathname();

  // Builder pages need full-bleed canvas — no top bar, no padding
  const isBuilderPage = pathname.includes("/builder");

  // Show loading state while restoring auth session
  if (isLoading) {
    return (
      <div
        className="min-h-screen flex items-center justify-center"
        style={{ backgroundColor: "var(--neurex-bg-base)" }}
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
          className="flex flex-col items-center gap-4"
        >
          <div className="animate-pulse">
            <NeurexLogo size={48} />
          </div>
          <span
            className="text-sm"
            style={{ color: "var(--neurex-text-tertiary)" }}
          >
            Loading Neurex...
          </span>
        </motion.div>
      </div>
    );
  }

  return (
    <div
      className={isBuilderPage ? "h-screen overflow-hidden" : "min-h-screen"}
      style={{ backgroundColor: "var(--neurex-bg-base)" }}
    >
      <Sidebar />
      <motion.main
        className={isBuilderPage ? "h-screen flex flex-col" : "min-h-screen flex flex-col"}
        animate={{
          marginLeft: isCollapsed ? 64 : 240,
        }}
        transition={{ type: "spring", stiffness: 300, damping: 30 }}
      >
        {!isBuilderPage && <TopBar />}
        <div className={isBuilderPage ? "flex-1 min-h-0" : "flex-1 p-6"}>
          <ErrorBoundary>
            {children}
          </ErrorBoundary>
        </div>
      </motion.main>
    </div>
  );
}
