// ============================================
// NEUREX — Feature Flag System
// Static config for v1. Future: env vars or remote config.
// ============================================

export const FEATURE_FLAGS = {
  AI_GENERATION: true,
  WEBHOOKS: true,
  ADMIN_PANEL: false,
  DARK_MODE: true,
  LIGHT_MODE: false, // Deferred to v2
} as const;

export type FeatureFlag = keyof typeof FEATURE_FLAGS;

export function isFeatureEnabled(flag: FeatureFlag): boolean {
  return FEATURE_FLAGS[flag];
}
