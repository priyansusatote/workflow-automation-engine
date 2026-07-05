"use client";

import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Sparkles,
  Send,
  Loader2,
  ArrowRight,
  Copy,
  Check,
  GitBranch,
  Zap,
  Brain,
  Filter,
  Globe,
  Clock,
  Terminal,
  ChevronRight,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { useGenerateWorkflow, useGenerateAndSaveWorkflow } from "@/hooks/use-ai";

// ─── Suggestion Cards ───────────────────────
const SUGGESTIONS = [
  {
    icon: Globe,
    title: "API Data Pipeline",
    prompt: "Create a workflow that fetches data from a REST API, filters results using AI classification, and sends the summary via HTTP webhook",
    category: "Integration",
  },
  {
    icon: Brain,
    title: "Content Moderation",
    prompt: "Build a workflow that takes user-submitted text, uses AI to classify it as safe or harmful, then routes harmful content for review and safe content for publishing",
    category: "AI",
  },
  {
    icon: Filter,
    title: "Lead Qualification",
    prompt: "Design a workflow that receives lead data, uses AI to extract key info, makes a decision on lead quality, and routes high-quality leads to sales team via HTTP action",
    category: "Business",
  },
  {
    icon: Clock,
    title: "Scheduled Report",
    prompt: "Create a workflow that waits for a scheduled time, generates a daily summary report using AI, and sends it via HTTP to a Slack webhook",
    category: "Automation",
  },
];

// ─── JSON Syntax Highlight (minimal) ────────
function highlightJSON(json: string): string {
  return json
    .replace(
      /("(?:\\u[\da-fA-F]{4}|\\[^u]|[^\\"])*")\s*:/g,
      '<span style="color: hsl(262, 83%, 72%)">$1</span>:'
    )
    .replace(
      /:\s*("(?:\\u[\da-fA-F]{4}|\\[^u]|[^\\"])*")/g,
      ': <span style="color: hsl(152, 69%, 53%)">$1</span>'
    )
    .replace(
      /:\s*(\d+\.?\d*)/g,
      ': <span style="color: hsl(38, 92%, 58%)">$1</span>'
    )
    .replace(
      /:\s*(true|false|null)/g,
      ': <span style="color: hsl(217, 91%, 60%)">$1</span>'
    );
}

export default function AIGeneratePage() {
  const router = useRouter();
  const [prompt, setPrompt] = useState("");
  const [workflowName, setWorkflowName] = useState("");
  const [generatedJSON, setGeneratedJSON] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [showNameInput, setShowNameInput] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const resultRef = useRef<HTMLDivElement>(null);

  const generateMutation = useGenerateWorkflow();
  const saveWorkflow = useGenerateAndSaveWorkflow();

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height =
        Math.min(textareaRef.current.scrollHeight, 200) + "px";
    }
  }, [prompt]);

  // Scroll to result
  useEffect(() => {
    if (generatedJSON && resultRef.current) {
      resultRef.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [generatedJSON]);

  const handleGenerate = () => {
    if (!prompt.trim() || generateMutation.isPending) return;
    generateMutation.mutate(prompt.trim(), {
      onSuccess: (data) => {
        try {
          // Try to prettify the JSON
          const parsed = typeof data === "string" ? JSON.parse(data) : data;
          setGeneratedJSON(JSON.stringify(parsed, null, 2));
        } catch {
          setGeneratedJSON(typeof data === "string" ? data : JSON.stringify(data, null, 2));
        }
      },
    });
  };

  const handleSaveWorkflow = () => {
    if (!workflowName.trim() || !prompt.trim()) return;
    saveWorkflow.mutate(
      { workflowName: workflowName.trim(), prompt: prompt.trim() },
      {
        onSuccess: (result) => {
          router.push(`/workflows/${result.workflowId}/builder`);
        },
      }
    );
  };

  const handleCopy = () => {
    if (generatedJSON) {
      navigator.clipboard.writeText(generatedJSON);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleGenerate();
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      {/* ── Header ── */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="mb-8"
      >
        <div className="flex items-center gap-3 mb-2">
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center"
            style={{
              background:
                "linear-gradient(135deg, var(--neurex-accent), hsl(280, 67%, 60%))",
              boxShadow: "0 0 20px var(--neurex-accent-glow)",
            }}
          >
            <Sparkles className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1
              className="text-2xl font-bold tracking-tight"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              AI Workflow Generator
            </h1>
            <p
              className="text-sm"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Describe what you need. AI builds the workflow.
            </p>
          </div>
        </div>
      </motion.div>

      {/* ── Prompt Input ── */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
        className="relative mb-8"
      >
        <div
          className="rounded-2xl overflow-hidden transition-all duration-300"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: `1px solid ${
              prompt
                ? "var(--neurex-accent)"
                : "var(--neurex-border-default)"
            }`,
            boxShadow: prompt
              ? "0 0 30px var(--neurex-accent-glow), 0 8px 24px hsl(228, 14%, 0%, 0.4)"
              : "var(--shadow-md)",
          }}
        >
          {/* Terminal-style header */}
          <div
            className="flex items-center gap-2 px-4 py-2.5"
            style={{
              borderBottom: "1px solid var(--neurex-border-default)",
              backgroundColor: "var(--neurex-bg-overlay)",
            }}
          >
            <Terminal
              className="w-3.5 h-3.5"
              style={{ color: "var(--neurex-accent)" }}
            />
            <span
              className="text-xs font-mono"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              neurex generate
            </span>
            <div className="flex-1" />
            <div className="flex items-center gap-1.5">
              <div
                className="w-2 h-2 rounded-full"
                style={{ backgroundColor: "var(--neurex-error)" }}
              />
              <div
                className="w-2 h-2 rounded-full"
                style={{ backgroundColor: "var(--neurex-warning)" }}
              />
              <div
                className="w-2 h-2 rounded-full"
                style={{ backgroundColor: "var(--neurex-success)" }}
              />
            </div>
          </div>

          <div className="p-4 flex items-end gap-3">
            <textarea
              ref={textareaRef}
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Describe your workflow in plain English..."
              rows={1}
              className="flex-1 resize-none bg-transparent text-sm leading-relaxed focus:outline-none"
              style={{
                color: "var(--neurex-text-primary)",
                fontFamily: "var(--font-body)",
                maxHeight: "200px",
              }}
            />
            <button
              onClick={handleGenerate}
              disabled={!prompt.trim() || generateMutation.isPending}
              className="flex items-center justify-center w-10 h-10 rounded-xl transition-all duration-200 flex-shrink-0"
              style={{
                backgroundColor:
                  prompt.trim() && !generateMutation.isPending
                    ? "var(--neurex-accent)"
                    : "var(--neurex-bg-overlay)",
                color:
                  prompt.trim() && !generateMutation.isPending
                    ? "white"
                    : "var(--neurex-text-ghost)",
                boxShadow:
                  prompt.trim() && !generateMutation.isPending
                    ? "0 0 20px var(--neurex-accent-glow)"
                    : "none",
                cursor:
                  !prompt.trim() || generateMutation.isPending
                    ? "not-allowed"
                    : "pointer",
              }}
            >
              {generateMutation.isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Send className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>

        {/* Keyboard hint */}
        <div className="flex justify-end mt-2">
          <span
            className="text-xs"
            style={{ color: "var(--neurex-text-ghost)" }}
          >
            Press <kbd className="px-1.5 py-0.5 rounded text-xs font-mono"
              style={{
                backgroundColor: "var(--neurex-bg-overlay)",
                border: "1px solid var(--neurex-border-default)",
                color: "var(--neurex-text-tertiary)",
              }}
            >Enter</kbd> to generate · <kbd className="px-1.5 py-0.5 rounded text-xs font-mono"
              style={{
                backgroundColor: "var(--neurex-bg-overlay)",
                border: "1px solid var(--neurex-border-default)",
                color: "var(--neurex-text-tertiary)",
              }}
            >Shift+Enter</kbd> for new line
          </span>
        </div>
      </motion.div>

      {/* ── Suggestion Cards ── */}
      <AnimatePresence>
        {!generatedJSON && !generateMutation.isPending && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4, delay: 0.2 }}
          >
            <p
              className="text-xs font-medium uppercase tracking-wider mb-3"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              Try a suggestion
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {SUGGESTIONS.map((s, i) => {
                const Icon = s.icon;
                return (
                  <motion.button
                    key={i}
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.25 + i * 0.08 }}
                    onClick={() => setPrompt(s.prompt)}
                    className="group text-left p-4 rounded-xl transition-all duration-200"
                    style={{
                      backgroundColor: "var(--neurex-bg-elevated)",
                      border: "1px solid var(--neurex-border-default)",
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.borderColor =
                        "var(--neurex-accent)";
                      e.currentTarget.style.boxShadow =
                        "0 0 20px var(--neurex-accent-glow)";
                      e.currentTarget.style.transform = "translateY(-1px)";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.borderColor =
                        "var(--neurex-border-default)";
                      e.currentTarget.style.boxShadow = "none";
                      e.currentTarget.style.transform = "translateY(0)";
                    }}
                  >
                    <div className="flex items-start gap-3">
                      <div
                        className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5"
                        style={{
                          backgroundColor: "var(--neurex-accent-subtle)",
                        }}
                      >
                        <Icon
                          className="w-4 h-4"
                          style={{ color: "var(--neurex-accent)" }}
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <span
                            className="text-sm font-medium"
                            style={{
                              color: "var(--neurex-text-primary)",
                            }}
                          >
                            {s.title}
                          </span>
                          <span
                            className="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded-full"
                            style={{
                              backgroundColor: "var(--neurex-bg-overlay)",
                              color: "var(--neurex-text-ghost)",
                            }}
                          >
                            {s.category}
                          </span>
                        </div>
                        <p
                          className="text-xs mt-1 line-clamp-2"
                          style={{
                            color: "var(--neurex-text-tertiary)",
                          }}
                        >
                          {s.prompt}
                        </p>
                      </div>
                    </div>
                  </motion.button>
                );
              })}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Loading State ── */}
      <AnimatePresence>
        {generateMutation.isPending && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className="flex flex-col items-center justify-center py-16"
          >
            <div
              className="relative w-16 h-16 rounded-2xl flex items-center justify-center mb-4"
              style={{
                background:
                  "linear-gradient(135deg, var(--neurex-accent-subtle), hsl(280, 67%, 60%, 0.12))",
                boxShadow: "0 0 40px var(--neurex-accent-glow)",
              }}
            >
              <motion.div
                animate={{ rotate: 360 }}
                transition={{
                  duration: 3,
                  repeat: Infinity,
                  ease: "linear",
                }}
              >
                <Sparkles
                  className="w-7 h-7"
                  style={{ color: "var(--neurex-accent)" }}
                />
              </motion.div>
              {/* Pulse ring */}
              <motion.div
                className="absolute inset-0 rounded-2xl"
                animate={{ scale: [1, 1.3, 1], opacity: [0.4, 0, 0.4] }}
                transition={{
                  duration: 2,
                  repeat: Infinity,
                  ease: "easeInOut",
                }}
                style={{
                  border: "1px solid var(--neurex-accent)",
                }}
              />
            </div>
            <p
              className="text-sm font-medium"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Generating workflow...
            </p>
            <p
              className="text-xs mt-1"
              style={{ color: "var(--neurex-text-ghost)" }}
            >
              AI is analyzing your prompt and building a DAG
            </p>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Generated Result ── */}
      <AnimatePresence>
        {generatedJSON && (
          <motion.div
            ref={resultRef}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.5 }}
            className="mt-8"
          >
            {/* Result Header */}
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: "var(--neurex-success)" }}
                />
                <span
                  className="text-sm font-medium"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  Workflow Generated
                </span>
              </div>
              <button
                onClick={handleCopy}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs transition-colors"
                style={{
                  backgroundColor: "var(--neurex-bg-overlay)",
                  color: copied
                    ? "var(--neurex-success)"
                    : "var(--neurex-text-secondary)",
                  border: "1px solid var(--neurex-border-default)",
                }}
              >
                {copied ? (
                  <Check className="w-3 h-3" />
                ) : (
                  <Copy className="w-3 h-3" />
                )}
                {copied ? "Copied" : "Copy"}
              </button>
            </div>

            {/* Code Block */}
            <div
              className="rounded-xl overflow-hidden"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
              }}
            >
              <div
                className="flex items-center justify-between px-4 py-2"
                style={{
                  borderBottom: "1px solid var(--neurex-border-default)",
                  backgroundColor: "var(--neurex-bg-overlay)",
                }}
              >
                <div className="flex items-center gap-2">
                  <Terminal
                    className="w-3.5 h-3.5"
                    style={{ color: "var(--neurex-text-ghost)" }}
                  />
                  <span
                    className="text-xs font-mono"
                    style={{ color: "var(--neurex-text-ghost)" }}
                  >
                    workflow-definition.json
                  </span>
                </div>
              </div>
              <pre
                className="p-4 overflow-x-auto text-xs leading-relaxed"
                style={{
                  fontFamily: "var(--font-mono)",
                  maxHeight: "400px",
                  color: "var(--neurex-text-secondary)",
                }}
                dangerouslySetInnerHTML={{
                  __html: highlightJSON(generatedJSON),
                }}
              />
            </div>

            {/* ── Action Buttons ── */}
            <div className="flex items-center gap-3 mt-4">
              {!showNameInput ? (
                <>
                  <button
                    onClick={() => setShowNameInput(true)}
                    className="neurex-btn-primary flex items-center gap-2 text-sm"
                  >
                    <Zap className="w-4 h-4" />
                    Save as Workflow
                  </button>
                  <button
                    onClick={() => {
                      setGeneratedJSON(null);
                      setPrompt("");
                    }}
                    className="neurex-btn-ghost flex items-center gap-2 text-sm"
                  >
                    Generate Another
                  </button>
                </>
              ) : (
                <motion.div
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  className="flex items-center gap-3 flex-1"
                >
                  <input
                    type="text"
                    value={workflowName}
                    onChange={(e) => setWorkflowName(e.target.value)}
                    placeholder="Workflow name..."
                    className="neurex-input flex-1 text-sm"
                    autoFocus
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSaveWorkflow();
                      if (e.key === "Escape") setShowNameInput(false);
                    }}
                  />
                  <button
                    onClick={handleSaveWorkflow}
                    disabled={
                      !workflowName.trim() || saveWorkflow.isPending
                    }
                    className="neurex-btn-primary flex items-center gap-2 text-sm whitespace-nowrap"
                  >
                    {saveWorkflow.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <GitBranch className="w-4 h-4" />
                    )}
                    Create & Open Builder
                    <ChevronRight className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => setShowNameInput(false)}
                    className="neurex-btn-ghost text-sm"
                  >
                    Cancel
                  </button>
                </motion.div>
              )}
            </div>

            {/* Error from save */}
            {saveWorkflow.isError && (
              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="text-xs mt-2"
                style={{ color: "var(--neurex-error)" }}
              >
                Failed to save workflow. Please try again.
              </motion.p>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Error State ── */}
      {generateMutation.isError && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="mt-6 p-4 rounded-xl"
          style={{
            backgroundColor: "var(--neurex-error-subtle)",
            border: "1px solid hsl(0, 84%, 64%, 0.2)",
          }}
        >
          <p
            className="text-sm font-medium mb-1"
            style={{ color: "var(--neurex-error)" }}
          >
            Generation Failed
          </p>
          <p
            className="text-xs"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            {(generateMutation.error as Error)?.message ||
              "An error occurred while generating the workflow. Please check your backend is running and try again."}
          </p>
          <button
            onClick={handleGenerate}
            className="mt-3 text-xs font-medium transition-colors"
            style={{ color: "var(--neurex-accent)" }}
          >
            Try again →
          </button>
        </motion.div>
      )}
    </div>
  );
}
