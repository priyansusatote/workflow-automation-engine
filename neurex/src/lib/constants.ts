// ============================================
// NEUREX — Constants
// ============================================

export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  SIGNUP: "/signup",
  DASHBOARD: "/dashboard",
  WORKFLOWS: "/workflows",
  WORKFLOW_DETAIL: (id: string) => `/workflows/${id}`,
  WORKFLOW_BUILDER: (id: string) => `/workflows/${id}/builder`,
  WORKFLOW_EXECUTIONS: (id: string) => `/workflows/${id}/executions`,
  EXECUTIONS: "/executions",
  EXECUTION_DETAIL: (id: string) => `/executions/${id}`,
  AI_GENERATE: "/ai/generate",
  SETTINGS: "/settings",
  ADMIN: "/admin",
} as const;

export const NODE_TYPES = [
  "TRIGGER",
  "WEBHOOK_TRIGGER",
  "AI_GENERATE",
  "AI_DECISION",
  "AI_EXTRACT",
  "AI_CLASSIFY",
  "ACTION",
  "HTTP_ACTION",
  "RULE",
  "WAIT",
] as const;

export type NodeType = (typeof NODE_TYPES)[number];

export const NODE_CATEGORIES = {
  Triggers: ["TRIGGER", "WEBHOOK_TRIGGER"],
  "AI Nodes": ["AI_GENERATE", "AI_DECISION", "AI_EXTRACT", "AI_CLASSIFY"],
  Actions: ["ACTION", "HTTP_ACTION"],
  "Flow Control": ["RULE", "WAIT"],
} as const;

export const STATUS_COLORS = {
  RUNNING: {
    bg: "var(--neurex-running-subtle)",
    text: "var(--neurex-running)",
    glow: "var(--shadow-glow-running)",
  },
  SUCCESS: {
    bg: "var(--neurex-success-subtle)",
    text: "var(--neurex-success)",
    glow: "var(--shadow-glow-success)",
  },
  FAILED: {
    bg: "var(--neurex-error-subtle)",
    text: "var(--neurex-error)",
    glow: "var(--shadow-glow-error)",
  },
  WAITING: {
    bg: "var(--neurex-waiting-subtle)",
    text: "var(--neurex-waiting)",
    glow: "none",
  },
} as const;

export const EXECUTION_STATUS_LABELS = {
  RUNNING: "Running",
  SUCCESS: "Completed",
  FAILED: "Failed",
  WAITING: "Waiting",
} as const;

export const WORKFLOW_STATUS_LABELS = {
  ACTIVE: "Active",
  INACTIVE: "Inactive",
} as const;
