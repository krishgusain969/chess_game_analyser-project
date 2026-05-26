package com.chessanalyzer.controller;

import com.chessanalyzer.model.PositionAnalysis;
import com.chessanalyzer.service.AnalysisService;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final AnalysisService analysisService;

    public BoardController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateMoveResponse> validateMove(@RequestBody ValidateMoveRequest request) {
        if (!StringUtils.hasText(request.getFen())) {
            throw new IllegalArgumentException("fen is required");
        }
        if (!StringUtils.hasText(request.getFrom()) || !StringUtils.hasText(request.getTo())) {
            throw new IllegalArgumentException("from and to are required");
        }

        String fen = request.getFen().trim();
        Board board = new Board();
        board.loadFromFen(fen);

        String from = normalizeSquare(request.getFrom());
        String to = normalizeSquare(request.getTo());
        String promotion = request.getPromotion() == null ? null : request.getPromotion().trim().toLowerCase();

        String baseUci = from + to;

        List<Move> legalMoves = board.legalMoves();
        List<Move> matches = new ArrayList<>();
        for (Move legal : legalMoves) {
            String uci = legal.toString().toLowerCase();
            if (promotion == null) {
                if (uci.startsWith(baseUci)) {
                    matches.add(legal);
                }
            } else {
                String expected = baseUci + promotion;
                if (uci.equalsIgnoreCase(expected)) {
                    matches.add(legal);
                }
            }
        }

        ValidateMoveResponse response = new ValidateMoveResponse();
        response.setLegal(!matches.isEmpty());

        List<String> matchedMoves = matches.stream().map(m -> m.toString().toLowerCase()).toList();
        response.setMatchedMovesUci(matchedMoves);

        if (!matches.isEmpty()) {
            Board afterBoard = new Board();
            afterBoard.loadFromFen(fen);
            afterBoard.doMove(matches.get(0));
            response.setResultingFen(afterBoard.getFen());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/legal-moves")
    public ResponseEntity<List<LegalMoveResponseItem>> legalMoves(@RequestBody LegalMovesRequest request) {
        if (!StringUtils.hasText(request.getFen())) {
            throw new IllegalArgumentException("fen is required");
        }

        Board board = new Board();
        board.loadFromFen(request.getFen().trim());
        List<Move> legalMoves = board.legalMoves();

        List<LegalMoveResponseItem> response = new ArrayList<>();
        for (Move move : legalMoves) {
            String uci = move.toString().toLowerCase();
            String from = uci.substring(0, 2);
            String to = uci.substring(2, 4);
            String promotion = uci.length() == 5 ? uci.substring(4, 5) : null;

            LegalMoveResponseItem item = new LegalMoveResponseItem();
            item.setUci(uci);
            item.setFrom(from);
            item.setTo(to);
            item.setPromotion(promotion);
            response.add(item);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fen-from-pgn")
    public ResponseEntity<FenFromPgnResponse> fenFromPgn(@RequestBody FenFromPgnRequest request) {
        if (!StringUtils.hasText(request.getPgn())) {
            throw new IllegalArgumentException("pgn is required");
        }
        if (request.getPlyCount() < 0) {
            throw new IllegalArgumentException("plyCount must be >= 0");
        }

        String fen = analysisService.fenFromPgn(request.getPgn(), request.getPlyCount());
        FenFromPgnResponse response = new FenFromPgnResponse();
        response.setFen(fen);
        return ResponseEntity.ok(response);
    }

    private static String normalizeSquare(String square) {
        String s = square.trim().toLowerCase();
        if (s.length() != 2) {
            throw new IllegalArgumentException("Invalid square: " + square);
        }
        char file = s.charAt(0);
        char rank = s.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid square: " + square);
        }
        return s;
    }

    public static class ValidateMoveRequest {
        private String fen;
        private String from;
        private String to;
        private String promotion; // 'q', 'r', 'b', 'n' (optional)

        public String getFen() {
            return fen;
        }

        public void setFen(String fen) {
            this.fen = fen;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getPromotion() {
            return promotion;
        }

        public void setPromotion(String promotion) {
            this.promotion = promotion;
        }
    }

    public static class ValidateMoveResponse {
        private boolean legal;
        private String resultingFen;
        private List<String> matchedMovesUci;

        public boolean isLegal() {
            return legal;
        }

        public void setLegal(boolean legal) {
            this.legal = legal;
        }

        public String getResultingFen() {
            return resultingFen;
        }

        public void setResultingFen(String resultingFen) {
            this.resultingFen = resultingFen;
        }

        public List<String> getMatchedMovesUci() {
            return matchedMovesUci;
        }

        public void setMatchedMovesUci(List<String> matchedMovesUci) {
            this.matchedMovesUci = matchedMovesUci;
        }
    }

    public static class LegalMovesRequest {
        private String fen;

        public String getFen() {
            return fen;
        }

        public void setFen(String fen) {
            this.fen = fen;
        }
    }

    public static class LegalMoveResponseItem {
        private String uci;
        private String from;
        private String to;
        private String promotion;

        public String getUci() {
            return uci;
        }

        public void setUci(String uci) {
            this.uci = uci;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getPromotion() {
            return promotion;
        }

        public void setPromotion(String promotion) {
            this.promotion = promotion;
        }
    }

    public static class FenFromPgnRequest {
        private String pgn;
        private int plyCount;

        public String getPgn() {
            return pgn;
        }

        public void setPgn(String pgn) {
            this.pgn = pgn;
        }

        public int getPlyCount() {
            return plyCount;
        }

        public void setPlyCount(int plyCount) {
            this.plyCount = plyCount;
        }
    }

    public static class FenFromPgnResponse {
        private String fen;

        public String getFen() {
            return fen;
        }

        public void setFen(String fen) {
            this.fen = fen;
        }
    }
}

