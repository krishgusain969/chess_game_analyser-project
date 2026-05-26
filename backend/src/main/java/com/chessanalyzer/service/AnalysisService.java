package com.chessanalyzer.service;

import com.chessanalyzer.model.Analysis;
import com.chessanalyzer.model.AnalysisResult;
import com.chessanalyzer.model.MoveEvaluation;
import com.chessanalyzer.model.PositionAnalysis;
import com.chessanalyzer.model.Game;
import com.chessanalyzer.repository.AnalysisRepository;
import com.chessanalyzer.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class AnalysisService {

    private final GameService gameService;
    private final AnalysisRepository analysisRepository;
    private final StockfishService stockfishService;
    private final ObjectMapper objectMapper;

    public AnalysisService(GameService gameService,
                            AnalysisRepository analysisRepository,
                            StockfishService stockfishService,
                            ObjectMapper objectMapper) {
        this.gameService = gameService;
        this.analysisRepository = analysisRepository;
        this.stockfishService = stockfishService;
        this.objectMapper = objectMapper;
    }

    public AnalysisResult analyzeAndSaveGame(String pgn) {
        Objects.requireNonNull(pgn, "pgn");
        if (!StringUtils.hasText(pgn)) {
            throw new IllegalArgumentException("pgn must not be empty");
        }

        Game game = gameService.importGame(pgn);
        AnalysisResult computed = computeAnalysisForPgn(game.getId(), pgn);

        Analysis entity = new Analysis();
        entity.setGameId(game.getId());
        entity.setMovesJson(toJson(computed.getMoves()));
        entity.setTotalBlunders(computed.getTotalBlunders());
        entity.setTotalMistakes(computed.getTotalMistakes());
        entity.setTotalInaccuracies(computed.getTotalInaccuracies());
        entity.setAccuracyWhite(computed.getAccuracyWhite());
        entity.setAccuracyBlack(computed.getAccuracyBlack());
        entity.setAnalyzedAt(LocalDateTime.now());
        analysisRepository.save(entity);

        return computed;
    }

    public AnalysisResult getAnalysisByGameId(Long gameId) {
        Analysis analysis = analysisRepository.findByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("No analysis found for gameId=" + gameId));

        AnalysisResult result = new AnalysisResult();
        result.setGameId(analysis.getGameId());
        result.setTotalBlunders(analysis.getTotalBlunders());
        result.setTotalMistakes(analysis.getTotalMistakes());
        result.setTotalInaccuracies(analysis.getTotalInaccuracies());
        result.setAccuracyWhite(analysis.getAccuracyWhite());
        result.setAccuracyBlack(analysis.getAccuracyBlack());

        try {
            List<MoveEvaluation> moves = objectMapper.readValue(
                    analysis.getMovesJson(),
                    new TypeReference<List<MoveEvaluation>>() {
                    }
            );
            result.setMoves(moves);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse stored analysis movesJson for gameId=" + gameId, e);
        }

        return result;
    }

    public PositionAnalysis analyzePosition(String fen) {
        StockfishService.EngineAnalysis engine = stockfishService.analyzePosition(fen);
        PositionAnalysis response = new PositionAnalysis();
        response.setEvalScore(engine.getEvalScoreCp());
        response.setBestMove(engine.getBestMove());
        response.setPvLine(engine.getPvLine());
        response.setDepth(engine.getDepth());
        return response;
    }

    public String fenFromPgn(String pgn, int plyCount) {
        MoveList moveList = parseMoveListFromPgn(pgn);
        Board board = new Board();

        if (plyCount < 0) {
            throw new IllegalArgumentException("plyCount must be >= 0");
        }
        if (plyCount > moveList.size()) {
            throw new IllegalArgumentException("plyCount exceeds PGN move count. plyCount=" + plyCount);
        }

        int i = 0;
        for (Move move : moveList) {
            if (i >= plyCount) {
                break;
            }
            board.doMove(move);
            i++;
        }
        return board.getFen();
    }

    private AnalysisResult computeAnalysisForPgn(Long gameId, String pgn) {
        MoveList moveList = parseMoveListFromPgn(pgn);
        String[] sanHalfMoves = moveList.toSanArray();

        // Engine eval baseline: starting position.
        Board board = new Board();
        String startFen = board.getFen();
        StockfishService.EngineAnalysis startEngine = stockfishService.analyzePosition(startFen);
        int prevEvalCp = startEngine.getEvalScoreCp();

        List<MoveEvaluation> evaluations = new ArrayList<>(sanHalfMoves.length);

        int totalBlunders = 0;
        int totalMistakes = 0;
        int totalInaccuracies = 0;

        // Per-side accuracy aggregation from classifications.
        int whiteQualitySum = 0;
        int blackQualitySum = 0;
        int whiteMoveCount = 0;
        int blackMoveCount = 0;

        int plyIndex = 0;
        for (Move move : moveList) {
            boolean isWhite = board.getSideToMove() == Side.WHITE;

            board.doMove(move);
            String fenAfter = board.getFen();

            StockfishService.EngineAnalysis engine = stockfishService.analyzePosition(fenAfter);
            int newEvalCp = engine.getEvalScoreCp();

            int prevFromMover = isWhite ? prevEvalCp : -prevEvalCp;
            int newFromMover = isWhite ? newEvalCp : -newEvalCp;

            int drop = prevFromMover - newFromMover;
            int gain = newFromMover - prevFromMover;

            String classification = classifyMove(drop, gain);
            int quality = qualityScoreForClassification(classification);

            if ("Blunder".equals(classification)) {
                totalBlunders++;
            } else if ("Mistake".equals(classification)) {
                totalMistakes++;
            } else if ("Inaccuracy".equals(classification)) {
                totalInaccuracies++;
            }

            if (isWhite) {
                whiteQualitySum += quality;
                whiteMoveCount++;
            } else {
                blackQualitySum += quality;
                blackMoveCount++;
            }

            int moveNumber = (plyIndex / 2) + 1;
            String sanMove = sanHalfMoves[plyIndex];

            evaluations.add(new MoveEvaluation(
                    moveNumber,
                    sanMove,
                    fenAfter,
                    newEvalCp,
                    classification,
                    engine.getBestMove(),
                    engine.getPvLine(),
                    isWhite
            ));

            prevEvalCp = newEvalCp;
            plyIndex++;
        }

        double accuracyWhite = whiteMoveCount == 0 ? 0 : (whiteQualitySum / (double) whiteMoveCount);
        double accuracyBlack = blackMoveCount == 0 ? 0 : (blackQualitySum / (double) blackMoveCount);

        // Normalize from 20-100 to 0-100 (already quality is 20-100).
        accuracyWhite = clamp(accuracyWhite, 0, 100);
        accuracyBlack = clamp(accuracyBlack, 0, 100);

        AnalysisResult result = new AnalysisResult();
        result.setGameId(gameId);
        result.setMoves(evaluations);
        result.setTotalBlunders(totalBlunders);
        result.setTotalMistakes(totalMistakes);
        result.setTotalInaccuracies(totalInaccuracies);
        result.setAccuracyWhite(accuracyWhite);
        result.setAccuracyBlack(accuracyBlack);
        return result;
    }

    // (intentionally no index-based lookup - we iterate in-order for correctness & speed)

    private static String classifyMove(int dropForMover, int gainForMover) {
        // dropForMover > 0 means the mover made things worse for themselves.
        if (dropForMover >= 200) {
            return "Blunder";
        }
        if (dropForMover >= 100) {
            return "Mistake";
        }
        if (dropForMover >= 50) {
            return "Inaccuracy";
        }
        if (gainForMover >= 50) {
            return "Brilliant";
        }
        return "Good";
    }

    private static int qualityScoreForClassification(String classification) {
        // Quality score in 0-100 range used as accuracy percentage.
        return switch (classification) {
            case "Brilliant" -> 100;
            case "Good" -> 85;
            case "Inaccuracy" -> 70;
            case "Mistake" -> 50;
            case "Blunder" -> 20;
            default -> 0;
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private MoveList parseMoveListFromPgn(String pgn) {
        String movesText = stripPgnToMoveText(pgn);
        if (!StringUtils.hasText(movesText)) {
            throw new IllegalArgumentException("Could not extract move list from PGN.");
        }

        MoveList moveList = new MoveList();
        try {
            moveList.loadFromSan(movesText);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid PGN move text. " + e.getMessage(), e);
        }
        return moveList;
    }

    /**
     * Removes PGN tags and tries to keep only the SAN move text.
     * This is intentionally simple (good enough for typical PGNs pasted by users).
     */
    private static String stripPgnToMoveText(String pgn) {
        String normalized = pgn.replace("\r", " ").trim();

        // Drop header tags like [Event "..."]
        normalized = normalized.replaceAll("(?m)^\\s*\\[[^\\]]*\\]\\s*$", " ");

        // Remove comments: {...}
        normalized = normalized.replaceAll("(?s)\\{[^}]*\\}", " ");

        // Remove variations: (...), non-nested
        normalized = normalized.replaceAll("(?s)\\([^)]*\\)", " ");

        // Remove NAGs like $1
        normalized = normalized.replaceAll("\\$\\d+", " ");

        // Collapse whitespace
        normalized = normalized.replaceAll("\\s+", " ").trim();

        // Remove final result token if present
        normalized = normalized.replaceAll("\\s+(1-0|0-1|1/2-1/2|\\*)\\s*$", "").trim();
        return normalized;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize analysis JSON", e);
        }
    }
}

