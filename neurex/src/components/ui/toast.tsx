"use client";

import {
  createContext,
  useContext,
  useState,
  useCallback,
  type ReactNode,
} from "react";
import { motion, AnimatePresence } from "framer-motion";
import { X, CheckCircle2, AlertTriangle, XCircle, Info } from "lucide-react";

type ToastVariant = "success" | "error" | "warning" | "info";

interface Toast {
  id: string;
  message: string;
  variant: ToastVariant;
  duration?: number;
}

interface ToastContextValue {
  toast: (message: string, variant?: ToastVariant, duration?: number) => void;
  success: (message: string) => void;
  error: (message: string) => void;
  warning: (message: string) => void;
  info: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const variantConfig: Record<
  ToastVariant,
  { icon: typeof CheckCircle2; color: string; bg: string }
> = {
  success: {
    icon: CheckCircle2,
    color: "var(--neurex-success)",
    bg: "var(--neurex-success-subtle)",
  },
  error: {
    icon: XCircle,
    color: "var(--neurex-error)",
    bg: "var(--neurex-error-subtle)",
  },
  warning: {
    icon: AlertTriangle,
    color: "var(--neurex-warning)",
    bg: "var(--neurex-warning-subtle)",
  },
  info: {
    icon: Info,
    color: "var(--neurex-accent)",
    bg: "hsl(262, 83%, 58%, 0.1)",
  },
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (message: string, variant: ToastVariant = "info", duration = 4000) => {
      const id = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
      setToasts((prev) => [...prev, { id, message, variant, duration }]);

      if (duration > 0) {
        setTimeout(() => removeToast(id), duration);
      }
    },
    [removeToast]
  );

  const value: ToastContextValue = {
    toast: addToast,
    success: useCallback((msg: string) => addToast(msg, "success"), [addToast]),
    error: useCallback((msg: string) => addToast(msg, "error", 6000), [addToast]),
    warning: useCallback((msg: string) => addToast(msg, "warning"), [addToast]),
    info: useCallback((msg: string) => addToast(msg, "info"), [addToast]),
  };

  return (
    <ToastContext.Provider value={value}>
      {children}

      {/* Toast container */}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col-reverse gap-2 max-w-sm w-full pointer-events-none">
        <AnimatePresence>
          {toasts.map((t) => {
            const config = variantConfig[t.variant];
            const Icon = config.icon;

            return (
              <motion.div
                key={t.id}
                initial={{ opacity: 0, y: 20, scale: 0.95 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, x: 80, scale: 0.95 }}
                transition={{ type: "spring", stiffness: 400, damping: 25 }}
                className="pointer-events-auto flex items-start gap-3 px-4 py-3 rounded-xl"
                style={{
                  backgroundColor: "var(--neurex-bg-overlay)",
                  border: "1px solid var(--neurex-border-default)",
                  boxShadow:
                    "0 8px 32px rgba(0,0,0,0.4), 0 0 1px rgba(255,255,255,0.05)",
                  backdropFilter: "blur(16px)",
                }}
              >
                <div
                  className="w-6 h-6 rounded-md flex items-center justify-center flex-shrink-0 mt-0.5"
                  style={{ backgroundColor: config.bg }}
                >
                  <Icon
                    className="w-3.5 h-3.5"
                    style={{ color: config.color }}
                  />
                </div>
                <p
                  className="text-sm flex-1 leading-snug pt-0.5"
                  style={{ color: "var(--neurex-text-primary)" }}
                >
                  {t.message}
                </p>
                <button
                  onClick={() => removeToast(t.id)}
                  className="p-0.5 rounded hover:bg-[var(--neurex-bg-subtle)] transition-colors flex-shrink-0"
                >
                  <X
                    className="w-3.5 h-3.5"
                    style={{ color: "var(--neurex-text-ghost)" }}
                  />
                </button>
              </motion.div>
            );
          })}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    // Fallback for components outside provider — use console/alert
    return {
      toast: (msg) => console.log("[Toast]", msg),
      success: (msg) => console.log("[Toast:success]", msg),
      error: (msg) => console.error("[Toast:error]", msg),
      warning: (msg) => console.warn("[Toast:warning]", msg),
      info: (msg) => console.log("[Toast:info]", msg),
    };
  }
  return ctx;
}
