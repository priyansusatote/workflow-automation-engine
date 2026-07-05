import { isFeatureEnabled, type FeatureFlag } from "@/lib/flags";

export function useFeatureFlag(flag: FeatureFlag): boolean {
  return isFeatureEnabled(flag);
}
