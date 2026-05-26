package com.chessanalyzer.model;

public class MoveEvaluation {
    private int moveNumber;
    private String move;
    private String fen;

    /**
     * Stockfish evaluation in centipawns, always from White's perspective.
     * Positive => White is better, negative => Black is better.
     */
    private int evalScore;

    private String classification;
    private String bestMove;
    private String pvLine;
    private boolean isWhiteTurn;

    public MoveEvaluation() {
    }

    public MoveEvaluation(
            int moveNumber,
            String move,
            String fen,
            int evalScore,
            String classification,
            String bestMove,
            String pvLine,
            boolean isWhiteTurn
    ) {
        this.moveNumber = moveNumber;
        this.move = move;
        this.fen = fen;
        this.evalScore = evalScore;
        this.classification = classification;
        this.bestMove = bestMove;
        this.pvLine = pvLine;
        this.isWhiteTurn = isWhiteTurn;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public int getEvalScore() {
        return evalScore;
    }

    public void setEvalScore(int evalScore) {
        this.evalScore = evalScore;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getBestMove() {
        return bestMove;
    }

    public void setBestMove(String bestMove) {
        this.bestMove = bestMove;
    }

    public String getPvLine() {
        return pvLine;
    }

    public void setPvLine(String pvLine) {
        this.pvLine = pvLine;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        isWhiteTurn = whiteTurn;
    }
}

