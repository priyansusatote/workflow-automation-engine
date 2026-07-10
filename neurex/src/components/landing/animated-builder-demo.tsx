"use client";

import { motion, useInView } from "framer-motion";
import { useRef, useState, useEffect } from "react";
import {
  Globe,
  Brain,
  GitBranch,
  Zap,
  CheckCircle2,
  Shield,
  Save,
  Play,
} from "lucide-react";

/* ───── Node type configs matching real builder ───── */
const nodeTypes = {
  HTTP_ACTION: {
    color: "hsl(200, 80%, 55%)",
    bg: "hsl(200, 80%, 55%, 0.12)",
    icon: Globe,
  },
  AI_EXTRACT: {
    color: "hsl(262, 83%, 64%)",
    bg: "hsl(262, 83%, 64%, 0.12)",
    icon: Brain,
  },
  AI_DECISION: {
    color: "hsl(340, 75%, 58%)",
    bg: "hsl(340, 75%, 58%, 0.12)",
    icon: GitBranch,
  },
};

/* ───── Node data — mirrors a real "Invoice Approval" workflow ───── */
const demoNodes = [
  {
    id: 1,
    type: "HTTP_ACTION" as const,
    label: "HTTP_ACTION",
    x: 80,
    y: 40,
    configs: [{ key: "url", value: "/api/invoices" }],
  },
  {
    id: 2,
    type: "AI_EXTRACT" as const,
    label: "AI_EXTRACT",
    x: 300,
    y: 130,
    configs: [
      { key: "prompt", value: "Extract invoice da..." },
      { key: "schema", value: '{"type":"object",...' },
    ],
  },
  {
    id: 3,
    type: "AI_DECISION" as const,
    label: "AI_DECISION",
    x: 560,
    y: 40,
    configs: [{ key: "prompt", value: "Is the invoice amo..." }],
  },
  {
    id: 4,
    type: "HTTP_ACTION" as const,
    label: "HTTP_ACTION",
    x: 180,
    y: 280,
    configs: [{ key: "actionType", value: "manager_approval" }],
  },
  {
    id: 5,
    type: "HTTP_ACTION" as const,
    label: "HTTP_ACTION",
    x: 450,
    y: 300,
    configs: [{ key: "actionType", value: "auto_approve" }],
  },
];

const demoEdges = [
  { from: 1, to: 2 },
  { from: 2, to: 3 },
  { from: 3, to: 4 },
  { from: 3, to: 5 },
];

/* ───── Animated handle dot ───── */
function Handle({
  side,
  style,
}: {
  side: "top" | "bottom";
  style?: React.CSSProperties;
}) {
  return (
    <div
      style={{
        position: "absolute",
        left: "50%",
        transform: "translateX(-50%)",
        ...(side === "top" ? { top: -5 } : { bottom: -5 }),
        width: 10,
        height: 10,
        borderRadius: "50%",
        backgroundColor: "var(--neurex-bg-base)",
        border: "2px solid hsl(262, 83%, 64%)",
        zIndex: 5,
        ...style,
      }}
    />
  );
}

/* ───── Single Node ───── */
function DemoNode({
  node,
  delay,
  isActive,
}: {
  node: (typeof demoNodes)[number];
  delay: number;
  isActive: boolean;
}) {
  const cfg = nodeTypes[node.type];
  const Icon = cfg.icon;

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.6, y: 20 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      transition={{
        delay,
        duration: 0.5,
        type: "spring",
        stiffness: 260,
        damping: 20,
      }}
      style={{
        position: "absolute",
        left: node.x,
        top: node.y,
        width: 200,
        zIndex: 2,
      }}
    >
      <div
        style={{
          backgroundColor: "var(--neurex-bg-overlay)",
          border: isActive
            ? `1.5px solid ${cfg.color}`
            : "1px solid var(--neurex-border-default)",
          borderRadius: 12,
          padding: "10px 14px",
          position: "relative",
          boxShadow: isActive
            ? `0 0 20px ${cfg.color}22, 0 4px 16px rgba(0,0,0,0.3)`
            : "0 4px 16px rgba(0,0,0,0.2)",
          transition: "border-color 0.3s, box-shadow 0.3s",
        }}
      >
        <Handle side="top" />

        {/* Header */}
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            marginBottom: 8,
          }}
        >
          <div
            style={{
              width: 26,
              height: 26,
              borderRadius: 8,
              backgroundColor: cfg.bg,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
            }}
          >
            <Icon style={{ width: 14, height: 14, color: cfg.color }} />
          </div>
          <div style={{ minWidth: 0 }}>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 6,
              }}
            >
              <span
                style={{
                  fontSize: 13,
                  fontWeight: 600,
                  color: "var(--neurex-text-primary)",
                }}
              >
                {node.id}
              </span>
            </div>
            <span
              style={{
                fontSize: 10,
                fontFamily: "var(--font-mono, monospace)",
                color: "var(--neurex-text-ghost)",
                letterSpacing: "0.02em",
              }}
            >
              {node.label}
            </span>
          </div>
        </div>

        {/* Config fields */}
        {node.configs.map((cfg, i) => (
          <div
            key={i}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 6,
              padding: "3px 0",
            }}
          >
            <span
              style={{
                fontSize: 10,
                fontFamily: "var(--font-mono, monospace)",
                color: "var(--neurex-text-ghost)",
                flexShrink: 0,
              }}
            >
              {cfg.key}
            </span>
            <TypewriterValue value={cfg.value} delay={delay + 0.5 + i * 0.8} />
          </div>
        ))}

        <Handle side="bottom" />
      </div>
    </motion.div>
  );
}

/* ───── Typewriter config value ───── */
function TypewriterValue({ value, delay }: { value: string; delay: number }) {
  const [displayed, setDisplayed] = useState("");
  const [started, setStarted] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setStarted(true), delay * 1000);
    return () => clearTimeout(timer);
  }, [delay]);

  useEffect(() => {
    if (!started) return;
    let idx = 0;
    const interval = setInterval(() => {
      idx++;
      setDisplayed(value.slice(0, idx));
      if (idx >= value.length) clearInterval(interval);
    }, 40);
    return () => clearInterval(interval);
  }, [started, value]);

  return (
    <span
      style={{
        fontSize: 11,
        fontFamily: "var(--font-mono, monospace)",
        color: "var(--neurex-text-secondary)",
        backgroundColor: "var(--neurex-bg-base)",
        borderRadius: 4,
        padding: "1px 6px",
        display: "inline-block",
        minWidth: 60,
        overflow: "hidden",
        whiteSpace: "nowrap",
        borderRight: started && displayed.length < value.length
          ? "2px solid var(--neurex-accent)"
          : "2px solid transparent",
      }}
    >
      {displayed || "\u00A0"}
    </span>
  );
}

/* ───── Animated edge path ───── */
function DemoEdge({
  from,
  to,
  delay,
}: {
  from: (typeof demoNodes)[number];
  to: (typeof demoNodes)[number];
  delay: number;
}) {
  // Calculate connection points (bottom of from → top of to)
  const x1 = from.x + 100; // center of 200w node
  const y1 = from.y + 70; // ~bottom of node
  const x2 = to.x + 100;
  const y2 = to.y; // top of node

  const midY = (y1 + y2) / 2;
  const d = `M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}`;

  return (
    <>
      <motion.path
        d={d}
        fill="none"
        stroke="hsl(262, 83%, 64%)"
        strokeWidth={2}
        strokeDasharray="6 4"
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 0.5 }}
        transition={{ delay, duration: 0.8, ease: "easeInOut" }}
      />
      {/* Animated flow dot */}
      <motion.circle
        r={3}
        fill="hsl(262, 83%, 74%)"
        initial={{ opacity: 0 }}
        animate={{ opacity: [0, 1, 1, 0] }}
        transition={{
          delay: delay + 0.8,
          duration: 2,
          repeat: Infinity,
          repeatDelay: 1,
        }}
      >
        <animateMotion dur="2s" repeatCount="indefinite" begin={`${delay + 0.8}s`}>
          <mpath href={`#edge-${from.id}-${to.id}`} />
        </animateMotion>
      </motion.circle>
      <path id={`edge-${from.id}-${to.id}`} d={d} fill="none" stroke="none" />
    </>
  );
}

/* ───── Palette sidebar (decorative) ───── */
function DemoPalette() {
  const items = [
    { label: "AI Generate", color: "hsl(262, 83%, 64%)" },
    { label: "AI Decision", color: "hsl(340, 75%, 58%)" },
    { label: "AI Extract", color: "hsl(262, 83%, 64%)" },
    { label: "Rule", color: "hsl(38, 92%, 58%)" },
    { label: "HTTP Action", color: "hsl(200, 80%, 55%)" },
  ];

  return (
    <div
      style={{
        width: 170,
        borderRight: "1px solid var(--neurex-border-default)",
        padding: "12px 10px",
        flexShrink: 0,
      }}
    >
      <span
        style={{
          fontSize: 9,
          fontWeight: 600,
          letterSpacing: "0.08em",
          color: "var(--neurex-text-ghost)",
          textTransform: "uppercase",
          display: "block",
          marginBottom: 10,
        }}
      >
        Node Palette
      </span>
      {items.map((item, i) => (
        <motion.div
          key={item.label}
          initial={{ opacity: 0, x: -10 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3 + i * 0.1, duration: 0.3 }}
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            padding: "6px 8px",
            borderRadius: 8,
            marginBottom: 2,
            cursor: "default",
          }}
        >
          <div
            style={{
              width: 6,
              height: 6,
              borderRadius: 3,
              backgroundColor: item.color,
              flexShrink: 0,
            }}
          />
          <span
            style={{
              fontSize: 11,
              color: "var(--neurex-text-secondary)",
              whiteSpace: "nowrap",
            }}
          >
            {item.label}
          </span>
        </motion.div>
      ))}
    </div>
  );
}

/* ═══════════════════════════════════════════
   Main export — Interactive Builder Demo
   ═══════════════════════════════════════════ */
export function AnimatedBuilderDemo() {
  const ref = useRef<HTMLDivElement>(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });
  const [activeNode, setActiveNode] = useState<number | null>(null);

  // Cycle active node for glow effect
  useEffect(() => {
    if (!isInView) return;
    let idx = 0;
    const timer = setInterval(() => {
      idx = (idx + 1) % demoNodes.length;
      setActiveNode(demoNodes[idx].id);
    }, 2000);
    return () => clearInterval(timer);
  }, [isInView]);

  const nodeMap = Object.fromEntries(demoNodes.map((n) => [n.id, n]));

  return (
    <div
      ref={ref}
      style={{
        borderRadius: 16,
        overflow: "hidden",
        backgroundColor: "var(--neurex-bg-elevated)",
        border: "1px solid var(--neurex-border-default)",
        boxShadow:
          "0 24px 80px hsl(228, 14%, 0%, 0.5), 0 0 1px rgba(255,255,255,0.05)",
      }}
    >
      {/* ── Toolbar ── */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "8px 16px",
          borderBottom: "1px solid var(--neurex-border-default)",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ display: "flex", gap: 6 }}>
            <div
              style={{
                width: 12,
                height: 12,
                borderRadius: 6,
                backgroundColor: "hsl(0, 84%, 64%)",
              }}
            />
            <div
              style={{
                width: 12,
                height: 12,
                borderRadius: 6,
                backgroundColor: "hsl(38, 92%, 58%)",
              }}
            />
            <div
              style={{
                width: 12,
                height: 12,
                borderRadius: 6,
                backgroundColor: "hsl(152, 69%, 53%)",
              }}
            />
          </div>
          <span
            style={{
              fontSize: 12,
              fontWeight: 600,
              color: "var(--neurex-text-primary)",
            }}
          >
            Invoice Approval
          </span>
          <motion.span
            animate={{ opacity: [1, 0.4, 1] }}
            transition={{ duration: 2, repeat: Infinity }}
            style={{
              width: 7,
              height: 7,
              borderRadius: "50%",
              backgroundColor: "hsl(152, 69%, 53%)",
              display: "inline-block",
            }}
          />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <DemoToolbarBtn icon={<CheckCircle2 style={{ width: 13, height: 13 }} />} label="Validate" />
          <DemoToolbarBtn icon={<Save style={{ width: 13, height: 13 }} />} label="Save" />
          <DemoToolbarBtn
            icon={<Play style={{ width: 13, height: 13 }} />}
            label="Execute"
            accent
          />
        </div>
      </div>

      {/* ── Body ── */}
      <div style={{ display: "flex", height: 420 }}>
        {/* Palette */}
        <DemoPalette />

        {/* Canvas */}
        <div
          style={{
            flex: 1,
            position: "relative",
            overflow: "hidden",
            backgroundImage: `radial-gradient(circle, hsl(228, 10%, 18%, 0.5) 1px, transparent 1px)`,
            backgroundSize: "20px 20px",
          }}
        >
          {/* Edges */}
          {isInView && (
            <svg
              style={{
                position: "absolute",
                inset: 0,
                width: "100%",
                height: "100%",
                pointerEvents: "none",
                zIndex: 1,
              }}
            >
              {demoEdges.map((edge, i) => (
                <DemoEdge
                  key={`${edge.from}-${edge.to}`}
                  from={nodeMap[edge.from]}
                  to={nodeMap[edge.to]}
                  delay={0.6 + i * 0.3}
                />
              ))}
            </svg>
          )}

          {/* Nodes */}
          {isInView &&
            demoNodes.map((node, i) => (
              <DemoNode
                key={node.id}
                node={node}
                delay={0.2 + i * 0.25}
                isActive={activeNode === node.id}
              />
            ))}

          {/* Execution pulse overlay (faint) */}
          {isInView && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: [0, 0.03, 0] }}
              transition={{ delay: 3, duration: 2, repeat: Infinity, repeatDelay: 4 }}
              style={{
                position: "absolute",
                inset: 0,
                background:
                  "radial-gradient(circle at 40% 50%, hsl(262, 83%, 64%, 0.15), transparent 60%)",
                pointerEvents: "none",
              }}
            />
          )}
        </div>
      </div>
    </div>
  );
}

/* ── Toolbar button ── */
function DemoToolbarBtn({
  icon,
  label,
  accent,
}: {
  icon: React.ReactNode;
  label: string;
  accent?: boolean;
}) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 4,
        padding: "5px 10px",
        borderRadius: 8,
        fontSize: 11,
        fontWeight: 500,
        color: accent ? "white" : "var(--neurex-text-secondary)",
        backgroundColor: accent
          ? "hsl(262, 83%, 58%)"
          : "transparent",
        border: accent
          ? "none"
          : "1px solid var(--neurex-border-default)",
        cursor: "default",
      }}
    >
      {icon}
      {label}
    </div>
  );
}
