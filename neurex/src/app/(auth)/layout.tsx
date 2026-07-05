import { AnimatedBackground } from "@/components/auth/animated-background";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden">
      <AnimatedBackground />
      <div className="relative z-10 w-full max-w-md px-6">{children}</div>
    </div>
  );
}
