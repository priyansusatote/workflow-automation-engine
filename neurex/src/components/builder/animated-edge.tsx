"use client";

import { memo } from "react";
import {
  BaseEdge,
  getSmoothStepPath,
  type EdgeProps,
} from "@xyflow/react";

function AnimatedEdgeComponent({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  selected,
  style,
}: EdgeProps) {
  const [edgePath] = getSmoothStepPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
    sourcePosition,
    targetPosition,
    borderRadius: 16,
  });

  return (
    <>
      {/* Glow layer (visible on hover / selection) */}
      {selected && (
        <path
          d={edgePath}
          fill="none"
          stroke="var(--neurex-accent)"
          strokeWidth={6}
          strokeOpacity={0.15}
          filter="blur(4px)"
        />
      )}

      {/* Main edge */}
      <BaseEdge
        id={id}
        path={edgePath}
        style={{
          stroke: selected
            ? "var(--neurex-accent)"
            : "var(--neurex-border-focus)",
          strokeWidth: selected ? 2 : 1.5,
          transition: "stroke 0.2s, stroke-width 0.2s",
          ...style,
        }}
      />

      {/* Animated dot flowing along the edge */}
      <circle r="3" fill="var(--neurex-accent)" opacity={0.7}>
        <animateMotion dur="2.5s" repeatCount="indefinite" path={edgePath} />
      </circle>
    </>
  );
}

export const AnimatedEdge = memo(AnimatedEdgeComponent);
