"use client";

import { usePathname } from "next/navigation";
import { Search, Command } from "lucide-react";
import { useCommandPaletteStore } from "@/stores/command-palette-store";
import { useSidebarStore } from "@/stores/sidebar-store";
import { useEffect } from "react";

function getBreadcrumbs(pathname: string): string[] {
  const segments = pathname.split("/").filter(Boolean);
  return segments.map((segment) => {
    // Capitalize and clean up route segments
    if (segment === "ai") return "AI";
    return segment.charAt(0).toUpperCase() + segment.slice(1);
  });
}

export function TopBar() {
  const pathname = usePathname();
  const breadcrumbs = getBreadcrumbs(pathname);
  const { open } = useCommandPaletteStore();
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);

  // Global ⌘+K shortcut
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        open();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open]);

  return (
    <header
      className="sticky top-0 z-30 flex items-center justify-between h-14 px-6 shrink-0"
      style={{
        backgroundColor: "hsla(228, 14%, 7%, 0.8)",
        borderBottom: "1px solid var(--neurex-border-default)",
        backdropFilter: "blur(12px) saturate(150%)",
        WebkitBackdropFilter: "blur(12px) saturate(150%)",
      }}
    >
      {/* Breadcrumbs */}
      <nav className="flex items-center gap-1.5">
        {breadcrumbs.map((crumb, i) => (
          <div key={i} className="flex items-center gap-1.5">
            {i > 0 && (
              <span
                className="text-xs select-none"
                style={{ color: "var(--neurex-text-ghost)" }}
              >
                /
              </span>
            )}
            <span
              className="text-sm"
              style={{
                color:
                  i === breadcrumbs.length - 1
                    ? "var(--neurex-text-primary)"
                    : "var(--neurex-text-tertiary)",
                fontWeight: i === breadcrumbs.length - 1 ? 500 : 400,
              }}
            >
              {crumb}
            </span>
          </div>
        ))}
      </nav>

      {/* Command palette trigger */}
      <button
        onClick={open}
        className="flex items-center gap-3 rounded-lg px-3 py-1.5 transition-all duration-200"
        style={{
          backgroundColor: "var(--neurex-bg-elevated)",
          border: "1px solid var(--neurex-border-default)",
          color: "var(--neurex-text-tertiary)",
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.borderColor = "var(--neurex-border-hover)";
          e.currentTarget.style.color = "var(--neurex-text-secondary)";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.borderColor = "var(--neurex-border-default)";
          e.currentTarget.style.color = "var(--neurex-text-tertiary)";
        }}
      >
        <Search className="w-3.5 h-3.5" />
        <span className="text-sm hidden sm:inline">Search...</span>
        <kbd
          className="hidden sm:inline-flex items-center gap-0.5 rounded px-1.5 py-0.5 text-xs font-mono"
          style={{
            backgroundColor: "var(--neurex-bg-subtle)",
            color: "var(--neurex-text-ghost)",
            fontSize: "0.65rem",
          }}
        >
          <Command className="w-2.5 h-2.5" />K
        </kbd>
      </button>
    </header>
  );
}
