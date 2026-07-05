"use client";

import { useFeatureFlag } from "@/hooks/use-feature-flag";
import type { FeatureFlag } from "@/lib/flags";

interface FeatureGateProps {
  flag: FeatureFlag;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function FeatureGate({ flag, children, fallback }: FeatureGateProps) {
  const isEnabled = useFeatureFlag(flag);

  if (!isEnabled) {
    return fallback ? <>{fallback}</> : null;
  }

  return <>{children}</>;
}
