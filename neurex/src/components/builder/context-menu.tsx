"use client";

import { useCallback, useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Copy,
  Trash2,
  Scissors,
  ClipboardPaste,
  AlignVerticalJustifyCenter,
  AlignHorizontalJustifyCenter,
  ZoomIn,
  ZoomOut,
  Maximize,
  Plus,
} from "lucide-react";

interface ContextMenuItem {
  label: string;
  icon: React.ElementType;
  shortcut?: string;
  action: () => void;
  danger?: boolean;
  divider?: boolean;
}

interface ContextMenuProps {
  x: number;
  y: number;
  items: ContextMenuItem[];
  onClose: () => void;
}

export function ContextMenu({ x, y, items, onClose }: ContextMenuProps) {
  const menuRef = useRef<HTMLDivElement>(null);

  // Close on click outside or Escape
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as HTMLElement)) {
        onClose();
      }
    };
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("mousedown", handleClick);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("mousedown", handleClick);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [onClose]);

  // Adjust position to stay in viewport
  const adjustedX = Math.min(x, window.innerWidth - 220);
  const adjustedY = Math.min(y, window.innerHeight - items.length * 36 - 20);

  return (
    <AnimatePresence>
      <motion.div
        ref={menuRef}
        initial={{ opacity: 0, scale: 0.92, y: -4 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.92, y: -4 }}
        transition={{ duration: 0.12 }}
        className="fixed z-50 min-w-[200px] py-1.5 rounded-xl overflow-hidden"
        style={{
          left: adjustedX,
          top: adjustedY,
          backgroundColor: "var(--neurex-bg-overlay)",
          border: "1px solid var(--neurex-border-default)",
          boxShadow: "0 8px 32px rgba(0,0,0,0.5), 0 0 1px rgba(255,255,255,0.05)",
          backdropFilter: "blur(20px)",
        }}
      >
        {items.map((item, i) => (
          <div key={i}>
            {item.divider && i > 0 && (
              <div
                className="my-1 mx-2"
                style={{ height: 1, backgroundColor: "var(--neurex-border-default)" }}
              />
            )}
            <button
              onClick={() => {
                item.action();
                onClose();
              }}
              className="w-full flex items-center gap-2.5 px-3 py-1.5 text-left transition-colors"
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = item.danger
                  ? "var(--neurex-error-subtle)"
                  : "var(--neurex-bg-subtle)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "transparent";
              }}
            >
              <item.icon
                className="w-3.5 h-3.5 flex-shrink-0"
                style={{
                  color: item.danger
                    ? "var(--neurex-error)"
                    : "var(--neurex-text-ghost)",
                }}
              />
              <span
                className="text-[13px] flex-1"
                style={{
                  color: item.danger
                    ? "var(--neurex-error)"
                    : "var(--neurex-text-secondary)",
                }}
              >
                {item.label}
              </span>
              {item.shortcut && (
                <span
                  className="text-[10px] font-mono px-1.5 py-0.5 rounded"
                  style={{
                    backgroundColor: "var(--neurex-bg-base)",
                    color: "var(--neurex-text-ghost)",
                  }}
                >
                  {item.shortcut}
                </span>
              )}
            </button>
          </div>
        ))}
      </motion.div>
    </AnimatePresence>
  );
}

// ─── Hook: Node Context Menu Items ──────────
export function useNodeContextMenu() {
  return {
    getNodeItems: (
      nodeId: string,
      onDuplicate: () => void,
      onDelete: () => void,
    ): ContextMenuItem[] => [
      { label: "Duplicate", icon: Copy, shortcut: "Ctrl+D", action: onDuplicate },
      { label: "Delete", icon: Trash2, shortcut: "Del", action: onDelete, danger: true, divider: true },
    ],
  };
}

// ─── Hook: Canvas Context Menu Items ────────
export function useCanvasContextMenu() {
  return {
    getCanvasItems: (
      onFitView: () => void,
      onZoomIn: () => void,
      onZoomOut: () => void,
    ): ContextMenuItem[] => [
      { label: "Fit View", icon: Maximize, shortcut: "F", action: onFitView },
      { label: "Zoom In", icon: ZoomIn, shortcut: "Ctrl++", action: onZoomIn },
      { label: "Zoom Out", icon: ZoomOut, shortcut: "Ctrl+-", action: onZoomOut },
    ],
  };
}
