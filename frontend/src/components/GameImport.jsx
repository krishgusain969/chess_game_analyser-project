import React, { useState } from 'react';

export default function GameImport({ onAnalyze }) {
  const [pgn, setPgn] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const submit = async () => {
    setError(null);
    const trimmed = pgn.trim();
    if (!trimmed) {
      setError('Please paste a PGN first.');
      return;
    }
    try {
      setLoading(true);
      await onAnalyze(trimmed);
    } catch (e) {
      setError(e?.response?.data?.message || e.message || 'Failed to analyze PGN');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-lg border border-white/10 bg-white/0 p-4">
      <div className="text-white/80 font-semibold mb-2">Paste PGN</div>
      <textarea
        className="w-full min-h-[180px] rounded-md border border-white/10 bg-chess-board/20 p-3 font-mono text-sm text-white/90 outline-none focus:border-chess-accent/50"
        value={pgn}
        onChange={(e) => setPgn(e.target.value)}
        placeholder={`[Event "..."]\n[White "..."]\n\n1. e4 e5 2. Nf3 Nc6 ...`}
      />
      {error ? (
        <div className="mt-3 text-sm text-red-200 bg-red-500/10 border border-red-500/20 rounded p-2">
          {error}
        </div>
      ) : null}
      <div className="mt-3 flex items-center gap-3">
        <button
          className="px-4 py-2 rounded border border-chess-accent/40 bg-green-600/15 text-white hover:bg-green-600/25 disabled:opacity-60"
          onClick={submit}
          disabled={loading}
        >
          {loading ? 'Analyzing...' : 'Analyze'}
        </button>
        <div className="text-xs text-white/60">
          Uses Stockfish for evaluation (may take a moment for longer games).
        </div>
      </div>
    </div>
  );
}

