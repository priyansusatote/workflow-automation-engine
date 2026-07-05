"use client";

interface NeurexLogoProps {
  size?: number;
  className?: string;
}

/**
 * Neurex brand logo — a stylized gear/cog with "N" in the center.
 * Rendered as inline SVG for perfect scaling and zero loading issues.
 */
export function NeurexLogo({ size = 32, className }: NeurexLogoProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 120 120"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      {/* Glow filter */}
      <defs>
        <linearGradient id="gear-grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="hsl(262, 83%, 58%)" />
          <stop offset="100%" stopColor="hsl(280, 85%, 50%)" />
        </linearGradient>
        <filter id="logo-glow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="2" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      {/* Outer gear teeth */}
      <g filter="url(#logo-glow)">
        <path
          d="M60 8 L68 8 L72 18 L78 16 L84 10 L90 16 L86 24 L92 28 L102 24 L106 32 L96 36 L98 42 L108 46 L108 54 L98 58 L96 64 L106 68 L102 76 L92 72 L86 76 L90 84 L84 90 L78 84 L72 86 L68 96 L60 96 L56 86 L50 84 L44 90 L38 84 L42 76 L36 72 L26 76 L22 68 L32 64 L30 58 L20 54 L20 46 L30 42 L32 36 L22 32 L26 24 L36 28 L42 24 L38 16 L44 10 L50 16 L56 18 L60 8Z"
          fill="url(#gear-grad)"
          stroke="hsl(262, 83%, 68%)"
          strokeWidth="0.5"
        />

        {/* Inner ring */}
        <circle
          cx="64"
          cy="52"
          r="28"
          fill="hsl(228, 14%, 10%)"
          stroke="hsl(262, 83%, 58%)"
          strokeWidth="2.5"
        />
        <circle
          cx="64"
          cy="52"
          r="22"
          fill="none"
          stroke="hsl(262, 83%, 45%)"
          strokeWidth="1"
          strokeDasharray="3 3"
        />

        {/* Circuit dots on the ring */}
        <circle cx="40" cy="38" r="3" fill="hsl(262, 83%, 58%)" />
        <circle cx="88" cy="38" r="3" fill="hsl(262, 83%, 58%)" />
        <circle cx="40" cy="66" r="3" fill="hsl(262, 83%, 58%)" />
        <circle cx="88" cy="66" r="3" fill="hsl(262, 83%, 58%)" />

        {/* N letter */}
        <text
          x="64"
          y="60"
          textAnchor="middle"
          dominantBaseline="middle"
          fill="white"
          fontFamily="Inter, system-ui, sans-serif"
          fontWeight="700"
          fontSize="28"
          fontStyle="italic"
        >
          N
        </text>
      </g>
    </svg>
  );
}
