import React from 'react';

function badgeClass(classification) {
  switch (classification) {
    case 'Blunder':
      return 'border-red-500/40 bg-red-500/20 text-red-100';
    case 'Mistake':
      return 'border-orange-500/40 bg-orange-500/20 text-orange-100';
    case 'Inaccuracy':
      return 'border-yellow-400/40 bg-yellow-400/20 text-yellow-100';
    case 'Brilliant':
      return 'border-purple-500/40 bg-purple-500/20 text-purple-100';
    case 'Good':
    default:
      return 'border-green-500/40 bg-green-500/20 text-green-100';
  }
}

export default function AnalysisPanel({ moveEvaluation }) {
  if (!moveEvaluation) {
    return (
      <div className="rounded-lg border border-white/10 bg-white/0 p-4 text-white/70">
        Run an analysis to see move evaluations.
      </div>
    );
  }

  const { evalScore, classification, bestMove, pvLine } = moveEvaluation;

  return (
    <div className="rounded-lg border border-white/10 bg-white/0 p-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm text-white/70">Evaluation</div>
          <div className="text-2xl font-bold">
            {evalScore >= 0 ? '+' : ''}
            {evalScore}
            <span className="text-sm font-semibold text-white/60"> cp</span>
          </div>
        </div>
        <div className={`px-3 py-1 rounded border text-xs font-semibold ${badgeClass(classification)}`}>
          {classification}
        </div>
      </div>

      <div className="mt-4 space-y-2">
        <div>
          <div className="text-sm text-white/70">Engine best move</div>
          <div className="font-mono text-white/90">{bestMove || '—'}</div>
        </div>
        <div>
          <div className="text-sm text-white/70">Principal variation</div>
          <div className="font-mono text-white/70 text-xs break-all">{pvLine || '—'}</div>
        </div>
      </div>
    </div>
  );
}

