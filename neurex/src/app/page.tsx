"use client";

import { motion, useScroll, useTransform } from "framer-motion";
import {
  ArrowRight,
  Sparkles,
  Play,
  BarChart3,
  Shield,
  Workflow,
  Globe,
  Clock,
  ChevronRight,
  GitBranch,
  Cpu,
  Eye,
  Bot,
  Terminal,
  Layers,
} from "lucide-react";
import Link from "next/link";
import { NeurexLogo } from "@/components/ui/neurex-logo";
import { AnimatedBuilderDemo } from "@/components/landing/animated-builder-demo";
import { useRef } from "react";

/* ─────────────────────── Hero ─────────────────────── */
function Hero() {
  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden px-6">
      {/* Animated gradient mesh — 3 orbiting blobs */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
        <div
          className="absolute w-[600px] h-[600px] rounded-full animate-gradient-rotate"
          style={{
            top: "20%",
            left: "30%",
            background:
              "radial-gradient(circle, hsl(262, 83%, 64%, 0.12) 0%, transparent 60%)",
            filter: "blur(80px)",
          }}
        />
        <div
          className="absolute w-[500px] h-[500px] rounded-full"
          style={{
            top: "40%",
            right: "20%",
            background:
              "radial-gradient(circle, hsl(217, 91%, 60%, 0.08) 0%, transparent 60%)",
            filter: "blur(80px)",
            animation: "gradient-rotate 20s ease-in-out infinite reverse",
          }}
        />
        <div
          className="absolute w-[400px] h-[400px] rounded-full"
          style={{
            bottom: "20%",
            left: "50%",
            background:
              "radial-gradient(circle, hsl(280, 67%, 60%, 0.06) 0%, transparent 60%)",
            filter: "blur(60px)",
            animation: "gradient-rotate 25s ease-in-out infinite",
          }}
        />
      </div>

      {/* Grid pattern overlay */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          backgroundImage: `linear-gradient(hsl(228, 10%, 18%, 0.3) 1px, transparent 1px),
                            linear-gradient(90deg, hsl(228, 10%, 18%, 0.3) 1px, transparent 1px)`,
          backgroundSize: "64px 64px",
          maskImage: "radial-gradient(ellipse 80% 60% at 50% 40%, black, transparent)",
          WebkitMaskImage: "radial-gradient(ellipse 80% 60% at 50% 40%, black, transparent)",
        }}
      />

      <div className="relative z-10 max-w-4xl mx-auto text-center">
        {/* Badge */}
        <motion.div
          initial={{ opacity: 0, y: 20, scale: 0.9 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full mb-8"
          style={{
            background: "rgba(139, 92, 246, 0.08)",
            border: "1px solid rgba(139, 92, 246, 0.2)",
            backdropFilter: "blur(12px)",
          }}
        >
          <Sparkles className="w-3.5 h-3.5" style={{ color: "var(--neurex-accent)" }} />
          <span className="text-xs font-medium" style={{ color: "var(--neurex-accent)" }}>
            AI-Powered Workflow Orchestration
          </span>
        </motion.div>

        {/* Heading */}
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
          className="text-5xl md:text-7xl font-bold leading-[1.1] mb-6"
          style={{ color: "var(--neurex-text-primary)", letterSpacing: "-0.03em" }}
        >
          Build workflows.{" "}
          <span
            className="animate-text-shimmer"
            style={{
              background: "linear-gradient(90deg, hsl(262, 83%, 64%), hsl(262, 83%, 78%), hsl(217, 91%, 70%), hsl(262, 83%, 64%))",
              backgroundSize: "200% auto",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
            }}
          >
            Let AI orchestrate.
          </span>
        </motion.h1>

        {/* Subtitle */}
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
          className="text-lg md:text-xl max-w-2xl mx-auto mb-10 leading-relaxed"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          Design complex DAG workflows visually, generate them with natural language,
          and monitor every execution in real-time. The workflow engine that thinks.
        </motion.p>

        {/* CTAs */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3, ease: [0.16, 1, 0.3, 1] }}
          className="flex flex-col sm:flex-row items-center justify-center gap-4"
        >
          <Link
            href="/signup"
            className="neurex-btn-primary flex items-center gap-2 h-12 px-8 text-base"
          >
            Start building
            <ArrowRight className="w-4 h-4" />
          </Link>
          <Link
            href="/login"
            className="neurex-btn-ghost flex items-center gap-2 h-12 px-8 text-base"
          >
            Sign in
            <ChevronRight className="w-4 h-4" />
          </Link>
        </motion.div>

        {/* Tech badges */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.8, delay: 0.5 }}
          className="flex items-center justify-center gap-6 mt-16 flex-wrap"
        >
          {["Spring Boot", "Kafka", "React Flow", "AI-Powered", "Real-time"].map((tech, i) => (
            <motion.span
              key={tech}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.6 + i * 0.08, ease: [0.16, 1, 0.3, 1] }}
              className="text-xs font-mono px-3 py-1 rounded-full"
              style={{
                color: "var(--neurex-text-ghost)",
                border: "1px solid var(--neurex-border-default)",
              }}
            >
              {tech}
            </motion.span>

          ))}
        </motion.div>
      </div>
    </section>
  );
}

/* ─────────────── Builder Preview ─────────────── */
function BuilderPreview() {
  const ref = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start end", "end start"],
  });
  const y = useTransform(scrollYProgress, [0, 1], [60, -60]);

  return (
    <section ref={ref} className="relative py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, amount: 0.2 }}
          transition={{ duration: 0.6 }}
          className="text-center mb-16"
        >
          <div
            className="inline-flex items-center gap-2 px-3 py-1 rounded-full mb-4"
            style={{
              backgroundColor: "var(--neurex-accent-subtle)",
              border: "1px solid hsl(262, 83%, 64%, 0.15)",
            }}
          >
            <GitBranch className="w-3 h-3" style={{ color: "var(--neurex-accent)" }} />
            <span className="text-xs font-medium" style={{ color: "var(--neurex-accent)" }}>
              Visual Builder
            </span>
          </div>
          <h2
            className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Design workflows visually
          </h2>
          <p
            className="text-lg max-w-xl mx-auto"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            Drag, connect, and configure. Build complex DAG workflows with our
            intuitive node-based editor.
          </p>
        </motion.div>

        {/* Animated Builder Demo */}
        <motion.div style={{ y }}>
          <AnimatedBuilderDemo />
        </motion.div>
      </div>
    </section>
  );
}

/* ─────────────── AI Generation Demo ─────────────── */
function AIDemo() {
  return (
    <section className="py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="grid md:grid-cols-2 gap-16 items-center">
          {/* Left — Text */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ duration: 0.6 }}
          >
            <div
              className="inline-flex items-center gap-2 px-3 py-1 rounded-full mb-4"
              style={{
                backgroundColor: "var(--neurex-accent-subtle)",
                border: "1px solid hsl(262, 83%, 64%, 0.15)",
              }}
            >
              <Sparkles className="w-3 h-3" style={{ color: "var(--neurex-accent)" }} />
              <span className="text-xs font-medium" style={{ color: "var(--neurex-accent)" }}>
                AI Generation
              </span>
            </div>
            <h2
              className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Describe it.{" "}
              <span style={{ color: "var(--neurex-accent)" }}>We build it.</span>
            </h2>
            <p
              className="text-lg mb-8 leading-relaxed"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Write a plain-English prompt and watch Neurex generate a complete
              workflow with nodes, edges, and configurations — ready to execute.
            </p>
            <Link
              href="/signup"
              className="neurex-btn-primary inline-flex items-center gap-2"
            >
              Try AI Generation
              <ArrowRight className="w-4 h-4" />
            </Link>
          </motion.div>

          {/* Right — Terminal mockup */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <div
              className="rounded-2xl overflow-hidden"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
                boxShadow: "0 24px 80px hsl(228, 14%, 0%, 0.5)",
              }}
            >
              <div
                className="flex items-center gap-3 px-5 py-3"
                style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
              >
                <Terminal className="w-3.5 h-3.5" style={{ color: "var(--neurex-text-ghost)" }} />
                <span className="text-xs font-mono" style={{ color: "var(--neurex-text-ghost)" }}>
                  ai-prompt
                </span>
              </div>
              <div className="p-6 space-y-4">
                {/* Prompt */}
                <div>
                  <span className="text-xs font-mono" style={{ color: "var(--neurex-accent)" }}>
                    → prompt
                  </span>
                  <p
                    className="text-sm mt-1 leading-relaxed"
                    style={{ color: "var(--neurex-text-secondary)" }}
                  >
                    &quot;Create a workflow that receives a webhook, classifies the
                    incoming data using AI, then sends a notification via HTTP POST&quot;
                  </p>
                </div>
                {/* Response */}
                <div
                  className="p-4 rounded-lg"
                  style={{
                    backgroundColor: "var(--neurex-bg-base)",
                    border: "1px solid var(--neurex-border-default)",
                  }}
                >
                  <span className="text-xs font-mono" style={{ color: "var(--neurex-success)" }}>
                    ✓ Generated — 3 nodes, 2 edges
                  </span>
                  <pre
                    className="mt-3 text-xs leading-relaxed overflow-x-auto"
                    style={{ color: "var(--neurex-text-tertiary)", fontFamily: "var(--font-mono)" }}
                  >
                    {`{
  "nodes": [
    { "id": "trigger-1", "type": "WEBHOOK_TRIGGER" },
    { "id": "ai-1", "type": "AI_CLASSIFY" },
    { "id": "action-1", "type": "HTTP_ACTION" }
  ],
  "edges": [
    { "from": "trigger-1", "to": "ai-1" },
    { "from": "ai-1", "to": "action-1" }
  ]
}`}
                  </pre>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}

/* ─────────────── Execution Monitor Demo ─────────────── */
function ExecutionDemo() {
  const statuses = [
    { node: "webhook-trigger", status: "SUCCESS", time: "0.2s", color: "var(--neurex-success)" },
    { node: "ai-classify", status: "SUCCESS", time: "1.4s", color: "var(--neurex-success)" },
    { node: "http-action", status: "RUNNING", time: "...", color: "var(--neurex-running)" },
  ];

  return (
    <section className="py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="grid md:grid-cols-2 gap-16 items-center">
          {/* Left — Execution mockup */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ duration: 0.6 }}
          >
            <div
              className="rounded-2xl overflow-hidden"
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
                boxShadow: "0 24px 80px hsl(228, 14%, 0%, 0.5)",
              }}
            >
              <div
                className="flex items-center justify-between px-5 py-3"
                style={{ borderBottom: "1px solid var(--neurex-border-default)" }}
              >
                <div className="flex items-center gap-2">
                  <Eye className="w-3.5 h-3.5" style={{ color: "var(--neurex-text-ghost)" }} />
                  <span className="text-xs font-mono" style={{ color: "var(--neurex-text-ghost)" }}>
                    execution-monitor
                  </span>
                </div>
                <span
                  className="inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-0.5 rounded-full"
                  style={{
                    backgroundColor: "var(--neurex-running-subtle)",
                    color: "var(--neurex-running)",
                  }}
                >
                  <span
                    className="w-1.5 h-1.5 rounded-full animate-pulse-glow"
                    style={{ backgroundColor: "var(--neurex-running)" }}
                  />
                  RUNNING
                </span>
              </div>
              <div className="p-6 space-y-3">
                {statuses.map((item, i) => (
                  <motion.div
                    key={item.node}
                    initial={{ opacity: 0, x: -10 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: i * 0.15, duration: 0.4 }}
                    className="flex items-center justify-between p-3 rounded-lg"
                    style={{
                      backgroundColor: "var(--neurex-bg-base)",
                      border: "1px solid var(--neurex-border-default)",
                    }}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className="w-2 h-2 rounded-full"
                        style={{
                          backgroundColor: item.color,
                          boxShadow: item.status === "RUNNING" ? `0 0 8px ${item.color}` : "none",
                        }}
                      />
                      <span className="text-sm font-mono" style={{ color: "var(--neurex-text-primary)" }}>
                        {item.node}
                      </span>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="text-xs font-mono" style={{ color: "var(--neurex-text-ghost)" }}>
                        {item.time}
                      </span>
                      <span className="text-xs font-medium" style={{ color: item.color }}>
                        {item.status}
                      </span>
                    </div>
                  </motion.div>
                ))}
              </div>
            </div>
          </motion.div>

          {/* Right — Text */}
          <motion.div
            initial={{ opacity: 0, x: 30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <div
              className="inline-flex items-center gap-2 px-3 py-1 rounded-full mb-4"
              style={{
                backgroundColor: "var(--neurex-running-subtle)",
                border: "1px solid hsl(217, 91%, 60%, 0.15)",
              }}
            >
              <Eye className="w-3 h-3" style={{ color: "var(--neurex-running)" }} />
              <span className="text-xs font-medium" style={{ color: "var(--neurex-running)" }}>
                Real-time Monitoring
              </span>
            </div>
            <h2
              className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Watch every node execute
            </h2>
            <p
              className="text-lg mb-8 leading-relaxed"
              style={{ color: "var(--neurex-text-secondary)" }}
            >
              Track each task in real-time. See logs, timing data, and output for
              every node. Catch failures the moment they happen.
            </p>
            <Link
              href="/signup"
              className="neurex-btn-primary inline-flex items-center gap-2"
            >
              Start monitoring
              <ArrowRight className="w-4 h-4" />
            </Link>
          </motion.div>
        </div>
      </div>
    </section>
  );
}

/* ─────────────── Features Bento ─────────────── */
const features = [
  {
    icon: Workflow,
    title: "Visual DAG Builder",
    desc: "Drag-and-drop node editor with smart snapping, animated edges, and real-time validation.",
    accent: "var(--neurex-accent)",
    accentBg: "var(--neurex-accent-subtle)",
    span: "md:col-span-2",
  },
  {
    icon: Bot,
    title: "AI Node Types",
    desc: "Generate, classify, extract, and decide — powered by Gemini LLMs directly in your workflow.",
    accent: "var(--neurex-accent)",
    accentBg: "var(--neurex-accent-subtle)",
    span: "",
  },
  {
    icon: Cpu,
    title: "Parallel Execution",
    desc: "DAG-aware engine runs independent branches concurrently with 5-thread parallelism.",
    accent: "var(--neurex-success)",
    accentBg: "var(--neurex-success-subtle)",
    span: "",
  },
  {
    icon: Clock,
    title: "Wait & Resume",
    desc: "Pause workflows for timed delays and resume automatically. Built-in scheduling support.",
    accent: "var(--neurex-warning)",
    accentBg: "var(--neurex-warning-subtle)",
    span: "",
  },
  {
    icon: Shield,
    title: "JWT + RBAC",
    desc: "Role-based access control with secure JWT authentication and httpOnly refresh tokens.",
    accent: "var(--neurex-running)",
    accentBg: "var(--neurex-running-subtle)",
    span: "",
  },
  {
    icon: Layers,
    title: "Kafka-Powered",
    desc: "Event-driven execution pipeline with dead-letter recovery and automatic retry policies.",
    accent: "var(--neurex-error)",
    accentBg: "var(--neurex-error-subtle)",
    span: "md:col-span-2",
  },
];

function FeaturesBento() {
  return (
    <section className="py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-16"
        >
          <h2
            className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Everything you need
          </h2>
          <p
            className="text-lg max-w-xl mx-auto"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            A complete orchestration platform with enterprise-grade features built in.
          </p>
        </motion.div>

        <div className="grid md:grid-cols-4 gap-4">
          {features.map((feature, i) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.08, duration: 0.5 }}
              className={`group p-6 rounded-2xl transition-all duration-300 ${feature.span}`}
              style={{
                backgroundColor: "var(--neurex-bg-elevated)",
                border: "1px solid var(--neurex-border-default)",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = "var(--neurex-border-hover)";
                e.currentTarget.style.transform = "translateY(-2px)";
                e.currentTarget.style.boxShadow = "var(--shadow-lg)";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = "var(--neurex-border-default)";
                e.currentTarget.style.transform = "translateY(0)";
                e.currentTarget.style.boxShadow = "none";
              }}
            >
              <div
                className="w-10 h-10 rounded-xl flex items-center justify-center mb-4"
                style={{ backgroundColor: feature.accentBg }}
              >
                <feature.icon className="w-5 h-5" style={{ color: feature.accent }} />
              </div>
              <h3
                className="text-base font-semibold mb-2"
                style={{ color: "var(--neurex-text-primary)" }}
              >
                {feature.title}
              </h3>
              <p className="text-sm leading-relaxed" style={{ color: "var(--neurex-text-secondary)" }}>
                {feature.desc}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ─────────────── Pricing Placeholder ─────────────── */
function Pricing() {
  return (
    <section className="py-32 px-6">
      <div className="max-w-4xl mx-auto text-center">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
        >
          <h2
            className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Free during beta
          </h2>
          <p
            className="text-lg max-w-xl mx-auto mb-12"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            Full access to every feature. No credit card. No limits.
            Build what you need, scale when you&apos;re ready.
          </p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="inline-block p-8 rounded-2xl"
          style={{
            backgroundColor: "var(--neurex-bg-elevated)",
            border: "1px solid hsl(262, 83%, 64%, 0.2)",
            boxShadow: "0 0 40px hsl(262, 83%, 64%, 0.06)",
          }}
        >
          <div className="flex items-baseline justify-center gap-1 mb-2">
            <span
              className="text-5xl font-bold"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              $0
            </span>
            <span className="text-lg" style={{ color: "var(--neurex-text-tertiary)" }}>
              /mo
            </span>
          </div>
          <p className="text-sm mb-6" style={{ color: "var(--neurex-text-tertiary)" }}>
            Everything included
          </p>
          <div className="space-y-2 mb-8 text-left">
            {[
              "Unlimited workflows",
              "AI workflow generation",
              "Real-time execution monitoring",
              "Webhook triggers",
              "Parallel DAG execution",
              "Full API access",
            ].map((item) => (
              <div key={item} className="flex items-center gap-2.5">
                <span style={{ color: "var(--neurex-success)" }} className="text-sm">✓</span>
                <span className="text-sm" style={{ color: "var(--neurex-text-secondary)" }}>
                  {item}
                </span>
              </div>
            ))}
          </div>
          <Link
            href="/signup"
            className="neurex-btn-primary w-full flex items-center justify-center gap-2 h-11"
          >
            Get started free
            <ArrowRight className="w-4 h-4" />
          </Link>
        </motion.div>
      </div>
    </section>
  );
}

/* ─────────────── CTA ─────────────── */
function CTA() {
  return (
    <section className="py-32 px-6">
      <div className="max-w-4xl mx-auto text-center">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
        >
          <h2
            className="text-3xl md:text-5xl font-bold tracking-tight mb-4"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Ready to orchestrate?
          </h2>
          <p
            className="text-lg max-w-lg mx-auto mb-10"
            style={{ color: "var(--neurex-text-secondary)" }}
          >
            Join the next generation of workflow automation.
            Build smarter. Ship faster. Scale effortlessly.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              href="/signup"
              className="neurex-btn-primary flex items-center gap-2 h-12 px-8 text-base"
            >
              Create free account
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              href="/login"
              className="neurex-btn-ghost flex items-center gap-2 h-12 px-8 text-base"
            >
              Sign in
            </Link>
          </div>
        </motion.div>
      </div>
    </section>
  );
}

/* ─────────────── Footer ─────────────── */
function Footer() {
  return (
    <footer
      className="py-8 px-6"
      style={{ borderTop: "1px solid var(--neurex-border-default)" }}
    >
      <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2.5">
          <NeurexLogo size={28} />
          <span
            className="text-sm font-semibold"
            style={{ color: "var(--neurex-text-primary)" }}
          >
            Neurex
          </span>
        </div>
        <p className="text-xs" style={{ color: "var(--neurex-text-ghost)" }}>
          © {new Date().getFullYear()} Neurex. AI-Powered Workflow Orchestration.
        </p>
      </div>
    </footer>
  );
}

/* ─────────────── Navbar ─────────────── */
function Navbar() {
  return (
    <motion.nav
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
      className="fixed top-0 left-0 right-0 z-50 flex items-center justify-between px-6 h-16"
      style={{
        backgroundColor: "hsla(228, 14%, 7%, 0.7)",
        borderBottom: "1px solid var(--neurex-border-default)",
        backdropFilter: "blur(16px) saturate(150%)",
        WebkitBackdropFilter: "blur(16px) saturate(150%)",
      }}
    >
      <div className="flex items-center gap-2.5">
        <NeurexLogo size={32} />
        <span
          className="text-base font-bold tracking-tight"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Neurex
        </span>
      </div>

      <div className="flex items-center gap-3">
        <Link
          href="/login"
          className="text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          Sign in
        </Link>
        <Link
          href="/signup"
          className="neurex-btn-primary text-sm px-4 py-2"
        >
          Get started
        </Link>
      </div>
    </motion.nav>
  );
}

/* ─────────────── Tech Stack ─────────────── */
function TechStack() {
  const techs = [
    {
      name: "Spring Boot",
      desc: "Enterprise-grade Java backend",
      gradient: "linear-gradient(135deg, hsl(120, 60%, 42%), hsl(120, 60%, 52%))",
      letter: "S",
    },
    {
      name: "Apache Kafka",
      desc: "Event-driven messaging",
      gradient: "linear-gradient(135deg, hsl(0, 0%, 20%), hsl(0, 0%, 40%))",
      letter: "K",
    },
    {
      name: "React Flow",
      desc: "Visual workflow canvas",
      gradient: "linear-gradient(135deg, hsl(200, 90%, 50%), hsl(200, 90%, 65%))",
      letter: "R",
    },
    {
      name: "Gemini AI",
      desc: "AI-powered generation",
      gradient: "linear-gradient(135deg, hsl(262, 83%, 58%), hsl(262, 83%, 72%))",
      letter: "G",
    },
    {
      name: "PostgreSQL",
      desc: "Reliable data persistence",
      gradient: "linear-gradient(135deg, hsl(210, 50%, 45%), hsl(210, 50%, 60%))",
      letter: "P",
    },
  ];

  const stats = [
    { value: "<50ms", label: "Avg latency" },
    { value: "DAG", label: "Execution model" },
    { value: "∞", label: "Scalable nodes" },
    { value: "24/7", label: "Real-time monitoring" },
  ];

  return (
    <section className="py-20 px-6">
      <div className="max-w-5xl mx-auto">
        {/* Label */}
        <motion.p
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="text-center text-xs font-medium uppercase tracking-[0.2em] mb-10"
          style={{ color: "var(--neurex-text-ghost)" }}
        >
          Powered by industry-leading technologies
        </motion.p>

        {/* Tech logos */}
        <div className="flex items-center justify-center gap-8 flex-wrap mb-16">
          {techs.map((tech, i) => (
            <motion.div
              key={tech.name}
              initial={{ opacity: 0, y: 10 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1, duration: 0.4 }}
              className="flex items-center gap-3 group"
            >
              <div
                className="w-9 h-9 rounded-lg flex items-center justify-center text-sm font-bold text-white"
                style={{ background: tech.gradient }}
              >
                {tech.letter}
              </div>
              <div>
                <p
                  className="text-sm font-semibold"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {tech.name}
                </p>
                <p
                  className="text-xs"
                  style={{ color: "var(--neurex-text-ghost)" }}
                >
                  {tech.desc}
                </p>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Stats strip */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="grid grid-cols-2 md:grid-cols-4 gap-px rounded-xl overflow-hidden"
          style={{
            backgroundColor: "var(--neurex-border-default)",
            border: "1px solid var(--neurex-border-default)",
          }}
        >
          {stats.map((stat) => (
            <div
              key={stat.label}
              className="flex flex-col items-center py-6"
              style={{ backgroundColor: "var(--neurex-bg-elevated)" }}
            >
              <span
                className="text-2xl font-bold tracking-tight"
                style={{ color: "var(--neurex-accent)" }}
              >
                {stat.value}
              </span>
              <span
                className="text-xs mt-1"
                style={{ color: "var(--neurex-text-ghost)" }}
              >
                {stat.label}
              </span>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  );
}

/* ─────────────── Page ─────────────── */
export default function LandingPage() {
  return (
    <div style={{ backgroundColor: "var(--neurex-bg-base)" }}>
      <Navbar />
      <Hero />
      <TechStack />
      <BuilderPreview />
      <AIDemo />
      <ExecutionDemo />
      <FeaturesBento />
      <Pricing />
      <CTA />
      <Footer />
    </div>
  );
}
