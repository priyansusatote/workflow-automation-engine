"use client";

import { Component, type ErrorInfo, type ReactNode } from "react";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { NeurexLogo } from "@/components/ui/neurex-logo";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * React Error Boundary — catches unhandled errors in child components
 * and displays a premium recovery UI instead of a blank screen.
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("[ErrorBoundary] Caught:", error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback;

      return (
        <div
          className="flex flex-col items-center justify-center min-h-[400px] p-8"
          style={{ backgroundColor: "var(--neurex-bg-base)" }}
        >
          <div
            className="w-full max-w-md rounded-2xl p-8 text-center"
            style={{
              backgroundColor: "var(--neurex-bg-elevated)",
              border: "1px solid var(--neurex-border-default)",
              boxShadow: "var(--shadow-lg)",
            }}
          >
            {/* Icon */}
            <div
              className="w-14 h-14 rounded-xl flex items-center justify-center mx-auto mb-5"
              style={{
                backgroundColor: "var(--neurex-error-subtle)",
              }}
            >
              <AlertTriangle
                className="w-7 h-7"
                style={{ color: "var(--neurex-error)" }}
              />
            </div>

            <h2
              className="text-lg font-semibold mb-2"
              style={{ color: "var(--neurex-text-primary)" }}
            >
              Something went wrong
            </h2>
            <p
              className="text-sm mb-6 leading-relaxed"
              style={{ color: "var(--neurex-text-tertiary)" }}
            >
              An unexpected error occurred. Try refreshing the component or
              reloading the page.
            </p>

            {/* Error detail */}
            {this.state.error && (
              <div
                className="text-left mb-6 p-3 rounded-lg font-mono text-xs break-all"
                style={{
                  backgroundColor: "var(--neurex-bg-base)",
                  color: "var(--neurex-error)",
                  border: "1px solid hsl(0, 84%, 64%, 0.15)",
                }}
              >
                {this.state.error.message}
              </div>
            )}

            {/* Actions */}
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={this.handleRetry}
                className="neurex-btn-primary flex items-center gap-2 text-sm"
              >
                <RefreshCw className="w-4 h-4" />
                Retry
              </button>
              <button
                onClick={() => window.location.reload()}
                className="neurex-btn-ghost text-sm"
              >
                Reload Page
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
