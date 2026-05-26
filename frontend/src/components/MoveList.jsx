import React from 'react';

function classificationStyles(classification) {
  switch (classification) {
    case 'Blunder':
      return 'bg-red-500/20 text-red-200 border-red-500/30';
    case 'Mistake':
      return 'bg-orange-500/20 text-orange-200 border-orange-500/30';
    case 'Inaccuracy':
      return 'bg-yellow-400/20 text-yellow-100 border-yellow-400/30';
    case 'Brilliant':
      return 'bg-purple-500/20 text-purple-200 border-purple-500/30';
    case 'Good':
    default:
      return 'bg-green-500/20 text-green-200 border-green-500/30';
  }
}

export default function MoveList({ moves = [], selectedIndex = 0, onSelect }) {
  return (
    <div className="rounded-lg border border-white/10 bg-white/0 overflow-hidden">
      <div className="px-3 py-2 border-b border-white/10 text-sm font-semibold text-white/80">
        Moves ({moves.length})
      </div>

      <div className="max-h-[420px] overflow-auto">
        {moves.map((m, idx) => (
          <button
            key={`${idx}-${m.move}`}
            className={`w-full text-left px-3 py-2 border-b border-white/5 hover:bg-white/5 transition ${
              idx === selectedIndex ? 'bg-white/10' : ''
            }`}
            onClick={() => onSelect(idx)}
          >
            <div className="flex items-center justify-between gap-3">
              <div className="text-sm">
                {(() => {
                  const isWhiteTurn = m.whiteTurn ?? m.isWhiteTurn;
                  return (
                    <span className="text-white/60">
                      {m.moveNumber}
                      {isWhiteTurn ? '.' : '..'}
                    </span>
                  );
                })()}{' '}
                <span className="font-mono">{m.move}</span>
              </div>
              <span
                className={`text-xs px-2 py-1 rounded border ${classificationStyles(m.classification)}`}
              >
                {m.classification}
              </span>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

