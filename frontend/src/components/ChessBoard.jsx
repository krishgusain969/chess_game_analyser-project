import React, { useMemo } from 'react';
import { Chessboard } from 'react-chessboard';

export default function ChessBoard({
  fen,
  boardOrientation = 'white',
  bestMoveUci,
  squareHighlights = {}
}) {
  const customArrows = useMemo(() => {
    if (!bestMoveUci || bestMoveUci.length < 4) return [];
    const uci = bestMoveUci.toLowerCase();
    const from = uci.slice(0, 2);
    const to = uci.slice(2, 4);
    if (!from || !to) return [];
    return [[from, to, 'rgb(34,197,94)']]; // green
  }, [bestMoveUci]);

  return (
    <div className="rounded-lg border border-white/10 bg-chess-board/20 p-2">
      <Chessboard
        position={fen || 'start'}
        boardOrientation={boardOrientation}
        customArrows={customArrows}
        customSquareStyles={squareHighlights}
        arePiecesDraggable={false}
        boardWidth={480}
      />
    </div>
  );
}

