import axios from 'axios';

const BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: BASE,
  timeout: 120_000
});

export async function exportGame(pgn) {
  const res = await api.post('/games/import', { pgn });
  return res.data;
}

export async function analyzeGame(pgn) {
  const res = await api.post('/analysis/analyze', { pgn });
  return res.data;
}

export async function getGameHistory() {
  const res = await api.get('/games');
  return res.data;
}

export async function getAnalysis(gameId) {
  const res = await api.get(`/analysis/${gameId}`);
  return res.data;
}

export async function fenFromPgn(pgn, plyCount) {
  const res = await api.post('/board/fen-from-pgn', { pgn, plyCount });
  return res.data;
}

export async function validateMove({ fen, from, to, promotion }) {
  const res = await api.post('/board/validate', { fen, from, to, promotion });
  return res.data;
}

export async function getLegalMoves(fen) {
  const res = await api.post('/board/legal-moves', { fen });
  return res.data;
}

