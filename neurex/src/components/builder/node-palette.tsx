"use client";

import { memo } from "react";
import { motion } from "framer-motion";
import { NODE_CATEGORIES, NODE_TYPES } from "./node-registry";

function NodePaletteComponent() {
  const onDragStart = (
    event: React.DragEvent,
    nodeType: string
  ) => {
    event.dataTransfer.setData("application/neurex-node-type", nodeType);
    event.dataTransfer.effectAllowed = "move";
  };

  return (
    <div
      className="w-[220px] flex-shrink-0 overflow-y-auto"
      style={{
        backgroundColor: "var(--neurex-bg-elevated)",
        borderRight: "1px solid var(--neurex-border-default)",
      }}
    >
      <div
        className="px-4 py-3"
        style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
      >
        <h3
          className="text-xs font-semibold uppercase tracking-wider"
          style={{ color: "var(--neurex-text-ghost)" }}
        >
          Node Palette
        </h3>
      </div>

      <div className="p-3 space-y-4">
        {NODE_CATEGORIES.map((category) => (
          <div key={category.id}>
            <p
              className="text-[10px] font-semibold uppercase tracking-wider mb-2 px-1"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              {category.label}
            </p>
            <div className="space-y-1">
              {category.types.map((typeKey) => {
                const config = NODE_TYPES[typeKey];
                if (!config) return null;
                const Icon = config.icon;
                return (
                  <motion.div
                    key={typeKey}
                    draggable
                    onDragStart={(e) =>
                      onDragStart(e as unknown as React.DragEvent, typeKey)
                    }
                    whileHover={{ scale: 1.02, x: 2 }}
                    whileTap={{ scale: 0.98 }}
                    className="flex items-center gap-2.5 p-2 rounded-lg cursor-grab active:cursor-grabbing transition-colors"
                    style={{
                      border: "1px solid transparent",
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor =
                        "var(--neurex-bg-overlay)";
                      e.currentTarget.style.borderColor =
                        "var(--neurex-border-default)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = "transparent";
                      e.currentTarget.style.borderColor = "transparent";
                    }}
                  >
                    <div
                      className="w-7 h-7 rounded-md flex items-center justify-center flex-shrink-0"
                      style={{ backgroundColor: config.colorSubtle }}
                    >
                      <Icon
                        className="w-3.5 h-3.5"
                        style={{ color: config.color }}
                      />
                    </div>
                    <div className="min-w-0">
                      <p
                        className="text-xs font-medium leading-tight"
                        style={{ color: "var(--neurex-text-primary)" }}
                      >
                        {config.label}
                      </p>
                      <p
                        className="text-[10px] leading-tight truncate"
                        style={{ color: "var(--neurex-text-ghost)" }}
                      >
                        {config.description}
                      </p>
                    </div>
                  </motion.div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export const NodePalette = memo(NodePaletteComponent);
