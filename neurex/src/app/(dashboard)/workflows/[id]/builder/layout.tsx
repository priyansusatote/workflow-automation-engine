/**
 * Builder layout — passthrough.
 * The parent dashboard layout already handles builder-specific
 * styling (no TopBar, no padding, h-screen).
 */
export default function BuilderLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <>{children}</>;
}
