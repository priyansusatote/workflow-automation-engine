"use client";

import { useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, Play, Loader2, AlertCircle, Braces, Sparkles } from "lucide-react";

interface ExecuteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onExecute: (inputData: Record<string, unknown>) => void;
  isPending: boolean;
  workflowName?: string;
}

export function ExecuteModal({
  isOpen,
  onClose,
  onExecute,
  isPending,
  workflowName,
}: ExecuteModalProps) {
  const [inputJson, setInputJson] = useState("{\n  \n}");
  const [parseError, setParseError] = useState<string | null>(null);

  const handleExecute = useCallback(() => {
    try {
      const trimmed = inputJson.trim();
      const parsed = trimmed === "" ? {} : JSON.parse(trimmed);
      setParseError(null);
      onExecute(parsed);
    } catch (err) {
      setParseError(
        `Invalid JSON: ${err instanceof Error ? err.message : "Parse error"}`
      );
    }
  }, [inputJson, onExecute]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      // Ctrl+Enter to execute
      if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
        e.preventDefault();
        handleExecute();
      }
      // Escape to close
      if (e.key === "Escape") {
        onClose();
      }
      // Tab to indent
      if (e.key === "Tab") {
        e.preventDefault();
        const target = e.target as HTMLTextAreaElement;
        const start = target.selectionStart;
        const end = target.selectionEnd;
        const newValue =
          inputJson.substring(0, start) + "  " + inputJson.substring(end);
        setInputJson(newValue);
        // Restore cursor position after state update
        requestAnimationFrame(() => {
          target.selectionStart = target.selectionEnd = start + 2;
        });
      }
    },
    [handleExecute, onClose, inputJson]
  );

  const insertTemplate = useCallback(() => {
    setInputJson(
      JSON.stringify(
        {
          content: "Hello World",
          source: "manual",
          metadata: {
            priority: "high",
          },
        },
        null,
        2
      )
    );
    setParseError(null);
  }, []);

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50"
            style={{ backgroundColor: "rgba(0, 0, 0, 0.6)", backdropFilter: "blur(4px)" }}
            onClick={onClose}
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            transition={{ type: "spring", stiffness: 400, damping: 30 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            onClick={(e) => e.stopPropagation()}
          >
            <div
              className="w-full max-w-xl rounded-2xl overflow-hidden"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
                boxShadow:
                  "0 24px 80px rgba(0, 0, 0, 0.5), 0 0 1px rgba(255, 255, 255, 0.05)",
              }}
            >
              {/* Header */}
              <div
                className="flex items-center justify-between px-6 py-4"
                style={{
                  borderBottom: "1px solid var(--neurex-border-default)",
                  background:
                    "linear-gradient(135deg, hsl(262, 83%, 58%, 0.08), transparent)",
                }}
              >
                <div className="flex items-center gap-3">
                  <div
                    className="w-9 h-9 rounded-lg flex items-center justify-center"
                    style={{
                      background:
                        "linear-gradient(135deg, var(--neurex-accent), hsl(262, 83%, 50%))",
                      boxShadow: "var(--shadow-glow-sm)",
                    }}
                  >
                    <Play className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <h2
                      className="text-base font-semibold"
                      style={{ color: "var(--neurex-text-primary)" }}
                    >
                      Execute Workflow
                    </h2>
                    {workflowName && (
                      <p
                        className="text-xs mt-0.5"
                        style={{ color: "var(--neurex-text-ghost)" }}
                      >
                        {workflowName}
                      </p>
                    )}
                  </div>
                </div>
                <button
                  onClick={onClose}
                  className="p-1.5 rounded-lg transition-colors"
                  style={{ color: "var(--neurex-text-ghost)" }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor =
                      "var(--neurex-bg-subtle)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = "transparent";
                  }}
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {/* Body */}
              <div className="px-6 py-5">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <Braces
                      className="w-3.5 h-3.5"
                      style={{ color: "var(--neurex-accent)" }}
                    />
                    <label
                      className="text-sm font-medium"
                      style={{ color: "var(--neurex-text-secondary)" }}
                    >
                      Input Data (JSON)
                    </label>
                  </div>
                  <button
                    onClick={insertTemplate}
                    className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs transition-colors"
                    style={{
                      color: "var(--neurex-accent)",
                      backgroundColor: "var(--neurex-bg-subtle)",
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor =
                        "hsl(262, 83%, 58%, 0.15)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor =
                        "var(--neurex-bg-subtle)";
                    }}
                  >
                    <Sparkles className="w-3 h-3" />
                    Insert Template
                  </button>
                </div>

                <div
                  className="relative rounded-xl overflow-hidden"
                  style={{
                    border: `1px solid ${
                      parseError
                        ? "var(--neurex-error)"
                        : "var(--neurex-border-default)"
                    }`,
                  }}
                >
                  <textarea
                    value={inputJson}
                    onChange={(e) => {
                      setInputJson(e.target.value);
                      setParseError(null);
                    }}
                    onKeyDown={handleKeyDown}
                    className="w-full h-48 p-4 text-sm font-mono resize-none focus:outline-none"
                    style={{
                      backgroundColor: "var(--neurex-bg-base)",
                      color: "var(--neurex-text-primary)",
                      caretColor: "var(--neurex-accent)",
                    }}
                    placeholder='{ "key": "value" }'
                    spellCheck={false}
                  />
                  <div
                    className="absolute bottom-0 left-0 right-0 h-8 pointer-events-none"
                    style={{
                      background:
                        "linear-gradient(transparent, var(--neurex-bg-base))",
                    }}
                  />
                </div>

                {/* Error display */}
                <AnimatePresence>
                  {parseError && (
                    <motion.div
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: "auto" }}
                      exit={{ opacity: 0, height: 0 }}
                      className="flex items-center gap-2 mt-3 p-2.5 rounded-lg"
                      style={{
                        backgroundColor: "var(--neurex-error-subtle)",
                        border: "1px solid hsl(0, 84%, 64%, 0.2)",
                      }}
                    >
                      <AlertCircle
                        className="w-3.5 h-3.5 flex-shrink-0"
                        style={{ color: "var(--neurex-error)" }}
                      />
                      <span
                        className="text-xs"
                        style={{ color: "var(--neurex-error)" }}
                      >
                        {parseError}
                      </span>
                    </motion.div>
                  )}
                </AnimatePresence>

                <p
                  className="text-xs mt-3"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  Pass input data as JSON for the TRIGGER node. Leave empty{" "}
                  <code
                    className="px-1 py-0.5 rounded text-[10px]"
                    style={{
                      backgroundColor: "var(--neurex-bg-subtle)",
                      color: "var(--neurex-text-tertiary)",
                    }}
                  >
                    {"{}"}
                  </code>{" "}
                  for no input.
                </p>
              </div>

              {/* Footer */}
              <div
                className="flex items-center justify-between px-6 py-4"
                style={{
                  borderTop: "1px solid var(--neurex-border-default)",
                  backgroundColor: "var(--neurex-bg-subtle)",
                }}
              >
                <span
                  className="text-[10px] font-mono"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  Ctrl+Enter to execute · Escape to cancel
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={onClose}
                    className="neurex-btn-ghost text-sm px-4 py-2"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleExecute}
                    disabled={isPending}
                    className="neurex-btn-primary flex items-center gap-2 text-sm px-5 py-2"
                  >
                    {isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <Play className="w-4 h-4" />
                    )}
                    Execute
                  </button>
                </div>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
