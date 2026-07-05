import Link from "next/link";

export default function NotFound() {
  return (
    <div
      className="min-h-screen flex flex-col items-center justify-center px-6"
      style={{ backgroundColor: "var(--neurex-bg-base)" }}
    >
      <div className="text-center">
        <h1
          className="text-7xl font-bold mb-4"
          style={{
            background: "linear-gradient(135deg, var(--neurex-accent), hsl(262, 83%, 72%))",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          404
        </h1>
        <h2
          className="text-xl font-semibold mb-3"
          style={{ color: "var(--neurex-text-primary)" }}
        >
          Page not found
        </h2>
        <p
          className="text-sm mb-8 max-w-md"
          style={{ color: "var(--neurex-text-secondary)" }}
        >
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link href="/dashboard" className="neurex-btn-primary">
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
}
