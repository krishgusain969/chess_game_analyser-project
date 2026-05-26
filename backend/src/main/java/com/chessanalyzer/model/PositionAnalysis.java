package com.chessanalyzer.model;

public class PositionAnalysis {
    private int evalScore;
    private int depth;
    private String bestMove;
    private String pvLine;

    public int getEvalScore() {
        return evalScore;
    }

    public void setEvalScore(int evalScore) {
        this.evalScore = evalScore;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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
}

