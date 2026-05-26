import React, { useMemo } from 'react';

function clamp(n, a, b) {
  return Math.max(a, Math.min(b, n));
}

export default function EvaluationBar({ evalScore = 0 }) {
  const { strength, isWhiteAdv } = useMemo(() => {
    const capped = clamp(evalScore, -10000, 10000);
    const strength01 = clamp(Math.abs(capped) / 500, 0, 1);
    return { strength: strength01, isWhiteAdv: capped >= 0 };
  }, [evalScore]);

  const fillHeight = `${Math.round(strength * 100)}%`;

  return (
    <div className="rounded-lg border border-white/10 bg-white/0 p-2">
      <div className="h-[260px] w-10 relative overflow-hidden rounded-md bg-chess-board/30">
        {/* Neutral base */}
        <div className="absolute inset-0 bg-gradient-to-b from-white/5 to-white/0" />

        {isWhiteAdv ? (
          <div
            className="absolute left-0 right-0 bottom-0 bg-green-500/30"
            style={{ height: fillHeight, transition: 'height 160ms ease' }}
            title={`Eval: ${evalScore} cp`}
          />
        ) : (
          <div
            className="absolute left-0 right-0 top-0 bg-red-500/25"
            style={{ height: fillHeight, transition: 'height 160ms ease' }}
            title={`Eval: ${evalScore} cp`}
          />
        )}
      </div>

      <div className="mt-3 text-center text-xs text-white/70">
        {evalScore >= 0 ? '+' : ''}
        {evalScore} cp
      </div>
    </div>
  );
}

