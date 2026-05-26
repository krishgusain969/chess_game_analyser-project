import React, { useMemo, useState } from 'react';
import { analyzeGame } from '../services/api.js';
import ChessBoard from '../components/ChessBoard.jsx';
import MoveList from '../components/MoveList.jsx';
import AnalysisPanel from '../components/AnalysisPanel.jsx';
import EvaluationBar from '../components/EvaluationBar.jsx';
import GameImport from '../components/GameImport.jsx';

function parseUciArrow(uci) {
  if (!uci || uci.length < 4) return null;
  const s = uci.toLowerCase();
  return {
    from: s.slice(0, 2),
    to: s.slice(2, 4)
  };
}

export default function Analyzer() {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [boardOrientation, setBoardOrientation] = useState('white');

  const moves = analysis?.moves || [];
  const selectedMove = moves[selectedIndex] || null;

  const bestMoveArrow = parseUciArrow(selectedMove?.bestMove);

  const squareHighlights = useMemo(() => {
    if (!bestMoveArrow) return {};
    return {
      [bestMoveArrow.from]: { backgroundColor: 'rgba(250, 204, 21, 0.35)' },
      [bestMoveArrow.to]: { backgroundColor: 'rgba(250, 204, 21, 0.35)' }
    };
  }, [bestMoveArrow]);

  const currentFen = selectedMove?.fen || 'start';

  const onAnalyze = async (pgnText) => {
    setError(null);
    setLoading(true);
    try {
      const result = await analyzeGame(pgnText);
      setAnalysis(result);
      setSelectedIndex(Math.max(0, (result?.moves?.length || 1) - 1));
    } catch (e) {
      setError(e?.response?.data?.message || e.message || 'Failed to analyze PGN');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <GameImport onAnalyze={onAnalyze} />

      {error ? (
        <div className="rounded-lg border border-red-500/30 bg-red-500/10 p-3 text-red-100 text-sm">
          {error}
        </div>
      ) : null}

      {loading && !analysis ? (
        <div className="text-white/70 text-sm">Analyzing with Stockfish…</div>
      ) : null}

      {analysis ? (
        <div className="grid grid-cols-1 lg:grid-cols-[520px_160px_1fr] gap-4 items-start">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="text-sm text-white/70">
                Game analysis: {analysis.totalBlunders} blunders, {analysis.totalMistakes} mistakes,{' '}
                {analysis.totalInaccuracies} inacc.
              </div>
              <button
                className="px-3 py-2 rounded border border-white/10 text-xs text-white/80 hover:bg-white/5"
                onClick={() => setBoardOrientation((o) => (o === 'white' ? 'black' : 'white'))}
              >
                Flip board
              </button>
            </div>

            <ChessBoard
              fen={selectedMove?.fen}
              boardOrientation={boardOrientation}
              bestMoveUci={selectedMove?.bestMove}
              squareHighlights={squareHighlights}
            />
          </div>

          <div className="flex justify-center">
            <EvaluationBar evalScore={selectedMove?.evalScore || 0} />
          </div>

          <div className="space-y-4">
            <AnalysisPanel moveEvaluation={selectedMove} />

            <MoveList
              moves={moves}
              selectedIndex={selectedIndex}
              onSelect={(idx) => setSelectedIndex(idx)}
            />
          </div>
        </div>
      ) : (
        <div className="rounded-lg border border-white/10 bg-white/0 p-4">
          <div className="text-sm text-white/80 mb-3">Starting chess position</div>
          <div className="w-full max-w-md mx-auto">
            <ChessBoard
              fen={currentFen}
              boardOrientation={boardOrientation}
              bestMoveUci={null}
              squareHighlights={{}}
            />
          </div>
          <div className="mt-3 text-sm text-white/70">
            Paste a PGN and click Analyze to view the actual board positions and move-by-move evaluation.
          </div>
        </div>
      )}
    </div>
  );
}

