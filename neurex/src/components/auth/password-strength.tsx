"use client";

import { useState } from "react";
import { motion } from "framer-motion";

interface PasswordStrengthProps {
  password: string;
}

interface StrengthCheck {
  label: string;
  test: (pw: string) => boolean;
}

const checks: StrengthCheck[] = [
  { label: "At least 8 characters", test: (pw) => pw.length >= 8 },
  { label: "Uppercase letter", test: (pw) => /[A-Z]/.test(pw) },
  { label: "Number", test: (pw) => /[0-9]/.test(pw) },
  { label: "Special character", test: (pw) => /[^a-zA-Z0-9]/.test(pw) },
];

export function PasswordStrength({ password }: PasswordStrengthProps) {
  const passed = checks.filter((check) => check.test(password)).length;
  const strength = password.length === 0 ? 0 : passed;

  const strengthLabel = ["", "Weak", "Fair", "Good", "Strong"][strength];
  const strengthColor = [
    "var(--neurex-bg-subtle)",
    "var(--neurex-error)",
    "var(--neurex-warning)",
    "var(--neurex-running)",
    "var(--neurex-success)",
  ][strength];

  return (
    <div className="space-y-3 mt-3">
      {/* Strength bar */}
      <div className="flex items-center gap-3">
        <div className="flex-1 flex gap-1">
          {[1, 2, 3, 4].map((level) => (
            <motion.div
              key={level}
              className="h-1 flex-1 rounded-full"
              initial={{ scaleX: 0 }}
              animate={{
                scaleX: 1,
                backgroundColor:
                  level <= strength ? strengthColor : "var(--neurex-bg-subtle)",
              }}
              transition={{ duration: 0.3, delay: level * 0.05 }}
              style={{ transformOrigin: "left" }}
            />
          ))}
        </div>
        {password.length > 0 && (
          <motion.span
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="text-xs font-medium"
            style={{ color: strengthColor }}
          >
            {strengthLabel}
          </motion.span>
        )}
      </div>

      {/* Requirement checklist */}
      {password.length > 0 && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: "auto" }}
          className="space-y-1.5"
        >
          {checks.map((check, i) => {
            const met = check.test(password);
            return (
              <motion.div
                key={i}
                initial={{ opacity: 0, x: -8 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: i * 0.05 }}
                className="flex items-center gap-2 text-xs"
                style={{
                  color: met
                    ? "var(--neurex-success)"
                    : "var(--neurex-text-tertiary)",
                }}
              >
                <span className="text-[10px]">{met ? "✓" : "○"}</span>
                {check.label}
              </motion.div>
            );
          })}
        </motion.div>
      )}
    </div>
  );
}
