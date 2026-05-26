package com.chessanalyzer.model;

import java.util.List;

public class AnalysisResult {
    private Long gameId;
    private List<MoveEvaluation> moves;

    private int totalBlunders;
    private int totalMistakes;
    private int totalInaccuracies;

    /**
     * 0-100 percentage, computed from move classifications.
     */
    private double accuracyWhite;
    private double accuracyBlack;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public List<MoveEvaluation> getMoves() {
        return moves;
    }

    public void setMoves(List<MoveEvaluation> moves) {
        this.moves = moves;
    }

    public int getTotalBlunders() {
        return totalBlunders;
    }

    public void setTotalBlunders(int totalBlunders) {
        this.totalBlunders = totalBlunders;
    }

    public int getTotalMistakes() {
        return totalMistakes;
    }

    public void setTotalMistakes(int totalMistakes) {
        this.totalMistakes = totalMistakes;
    }

    public int getTotalInaccuracies() {
        return totalInaccuracies;
    }

    public void setTotalInaccuracies(int totalInaccuracies) {
        this.totalInaccuracies = totalInaccuracies;
    }

    public double getAccuracyWhite() {
        return accuracyWhite;
    }

    public void setAccuracyWhite(double accuracyWhite) {
        this.accuracyWhite = accuracyWhite;
    }

    public double getAccuracyBlack() {
        return accuracyBlack;
    }

    public void setAccuracyBlack(double accuracyBlack) {
        this.accuracyBlack = accuracyBlack;
    }
}

