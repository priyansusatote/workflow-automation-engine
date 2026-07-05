"use client";

import { AlertTriangle, RefreshCw } from "lucide-react";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="en" data-theme="dark">
      <body
        style={{
          margin: 0,
          backgroundColor: "hsl(228, 14%, 7%)",
          fontFamily: "Inter, system-ui, sans-serif",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "100vh",
        }}
      >
        <div style={{ textAlign: "center", padding: "2rem", maxWidth: 480 }}>
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: 16,
              backgroundColor: "hsl(0, 84%, 64%, 0.1)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 24px",
            }}
          >
            <AlertTriangle
              style={{ width: 32, height: 32, color: "hsl(0, 84%, 64%)" }}
            />
          </div>
          <h1
            style={{
              color: "hsl(222, 14%, 95%)",
              fontSize: 24,
              fontWeight: 600,
              marginBottom: 8,
            }}
          >
            Application Error
          </h1>
          <p
            style={{
              color: "hsl(222, 8%, 52%)",
              fontSize: 14,
              marginBottom: 24,
              lineHeight: 1.6,
            }}
          >
            A critical error occurred. Please try again.
          </p>
          {error.digest && (
            <p
              style={{
                color: "hsl(222, 8%, 35%)",
                fontSize: 11,
                fontFamily: "monospace",
                marginBottom: 24,
              }}
            >
              Error ID: {error.digest}
            </p>
          )}
          <button
            onClick={reset}
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: 8,
              padding: "10px 20px",
              borderRadius: 12,
              border: "none",
              background: "linear-gradient(135deg, hsl(262, 83%, 58%), hsl(262, 83%, 50%))",
              color: "white",
              fontSize: 14,
              fontWeight: 500,
              cursor: "pointer",
            }}
          >
            <RefreshCw style={{ width: 16, height: 16 }} />
            Try Again
          </button>
        </div>
      </body>
    </html>
  );
}
