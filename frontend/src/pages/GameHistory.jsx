import React, { useEffect, useMemo, useState } from 'react';
import { getGameHistory, getAnalysis } from '../services/api.js';
import ChessBoard from '../components/ChessBoard.jsx';
import MoveList from '../components/MoveList.jsx';
import AnalysisPanel from '../components/AnalysisPanel.jsx';
import EvaluationBar from '../components/EvaluationBar.jsx';

function parseUciArrow(uci) {
  if (!uci || uci.length < 4) return null;
  const s = uci.toLowerCase();
  return { from: s.slice(0, 2), to: s.slice(2, 4) };
}

export default function GameHistory() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [selectedGameId, setSelectedGameId] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [boardOrientation, setBoardOrientation] = useState('white');

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await getGameHistory();
        if (!mounted) return;
        setGames(data);
      } catch (e) {
        if (!mounted) return;
        setError(e?.response?.data?.message || e.message || 'Failed to load history');
      } finally {
        if (!mounted) return;
        setLoading(false);
      }
    }
    load();
    return () => {
      mounted = false;
    };
  }, []);

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

  const loadGameAnalysis = async (gameId) => {
    setSelectedGameId(gameId);
    setAnalysis(null);
    try {
      const data = await getAnalysis(gameId);
      setAnalysis(data);
      setSelectedIndex(Math.max(0, (data?.moves?.length || 1) - 1));
    } catch (e) {
      setError(e?.response?.data?.message || e.message || 'Failed to load analysis');
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-4 items-start">
      <div className="rounded-lg border border-white/10 bg-white/0 overflow-hidden">
        <div className="px-4 py-3 border-b border-white/10 font-semibold text-sm text-white/80">
          Game History
        </div>
        {loading ? (
          <div className="p-4 text-white/70 text-sm">Loading…</div>
        ) : error ? (
          <div className="p-4 text-red-200 text-sm">{error}</div>
        ) : (
          <div className="max-h-[520px] overflow-auto">
            {games.length === 0 ? (
              <div className="p-4 text-white/70 text-sm">No games yet. Run an analysis first.</div>
            ) : (
              games.map((g) => (
                <button
                  key={g.id}
                  onClick={() => loadGameAnalysis(g.id)}
                  className={`w-full text-left px-4 py-3 border-b border-white/5 hover:bg-white/5 transition ${
                    selectedGameId === g.id ? 'bg-white/10' : ''
                  }`}
                >
                  <div className="text-sm font-semibold text-white/90">Game #{g.id}</div>
                  <div className="text-xs text-white/60 mt-1">
                    {g.white || 'White'} vs {g.black || 'Black'}
                  </div>
                  <div className="text-xs text-white/60 mt-1">Result: {g.result || '—'}</div>
                </button>
              ))
            )}
          </div>
        )}
      </div>

      <div className="space-y-4">
        {analysis ? (
          <div className="grid grid-cols-1 lg:grid-cols-[520px_160px_1fr] gap-4 items-start">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="text-sm text-white/70">
                  Analysis loaded for game #{analysis.gameId}
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
          <div className="rounded-lg border border-white/10 bg-white/0 p-4 text-white/70">
            Select a game from the history list to view the analysis.
          </div>
        )}
      </div>
    </div>
  );
}

