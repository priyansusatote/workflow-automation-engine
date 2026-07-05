import {
  Sparkles,
  GitBranch,
  Tags,
  FileSearch,
  Scale,
  Timer,
  Globe,
  type LucideIcon,
} from "lucide-react";

// ─── Node Type Registry ─────────────────────
// Maps backend NodeType enum values to builder UI metadata

export interface NodeTypeConfig {
  type: string;
  label: string;
  description: string;
  icon: LucideIcon;
  color: string;        // Primary accent for the node
  colorSubtle: string;  // Subtle background tint
  category: "ai" | "logic" | "action";
  defaultConfig: Record<string, unknown>;
}

export const NODE_TYPES: Record<string, NodeTypeConfig> = {
  AI_GENERATE: {
    type: "AI_GENERATE",
    label: "AI Generate",
    description: "Generate text using Gemini LLM",
    icon: Sparkles,
    color: "hsl(262, 83%, 64%)",
    colorSubtle: "hsl(262, 83%, 64%, 0.12)",
    category: "ai",
    defaultConfig: { prompt: "", model: "gemini-2.5-pro" },
  },
  AI_DECISION: {
    type: "AI_DECISION",
    label: "AI Decision",
    description: "AI-powered branching decision",
    icon: GitBranch,
    color: "hsl(280, 67%, 60%)",
    colorSubtle: "hsl(280, 67%, 60%, 0.12)",
    category: "ai",
    defaultConfig: { question: "", options: [] },
  },
  AI_CLASSIFY: {
    type: "AI_CLASSIFY",
    label: "AI Classify",
    description: "Classify input into categories",
    icon: Tags,
    color: "hsl(330, 70%, 58%)",
    colorSubtle: "hsl(330, 70%, 58%, 0.12)",
    category: "ai",
    defaultConfig: { categories: [], inputField: "" },
  },
  AI_EXTRACT: {
    type: "AI_EXTRACT",
    label: "AI Extract",
    description: "Extract structured data from text",
    icon: FileSearch,
    color: "hsl(200, 80%, 55%)",
    colorSubtle: "hsl(200, 80%, 55%, 0.12)",
    category: "ai",
    defaultConfig: { schema: {}, inputField: "" },
  },
  RULE: {
    type: "RULE",
    label: "Rule",
    description: "Conditional logic branching",
    icon: Scale,
    color: "hsl(38, 92%, 58%)",
    colorSubtle: "hsl(38, 92%, 58%, 0.12)",
    category: "logic",
    defaultConfig: { condition: "" },
  },
  WAIT: {
    type: "WAIT",
    label: "Wait",
    description: "Pause and resume after delay",
    icon: Timer,
    color: "hsl(152, 69%, 53%)",
    colorSubtle: "hsl(152, 69%, 53%, 0.12)",
    category: "logic",
    defaultConfig: { durationMs: 5000 },
  },
  HTTP_ACTION: {
    type: "HTTP_ACTION",
    label: "HTTP Action",
    description: "Make an HTTP request",
    icon: Globe,
    color: "hsl(217, 91%, 60%)",
    colorSubtle: "hsl(217, 91%, 60%, 0.12)",
    category: "action",
    defaultConfig: { url: "", method: "POST", headers: {}, body: "" },
  },
};

export const NODE_CATEGORIES = [
  { id: "ai", label: "AI Nodes", types: ["AI_GENERATE", "AI_DECISION", "AI_CLASSIFY", "AI_EXTRACT"] },
  { id: "logic", label: "Logic", types: ["RULE", "WAIT"] },
  { id: "action", label: "Actions", types: ["HTTP_ACTION"] },
] as const;

export function getNodeConfig(type: string): NodeTypeConfig {
  return NODE_TYPES[type] || NODE_TYPES.HTTP_ACTION;
}
