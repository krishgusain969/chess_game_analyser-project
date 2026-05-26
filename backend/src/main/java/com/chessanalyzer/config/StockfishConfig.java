package com.chessanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stockfish")
public class StockfishConfig {
    /**
     * Path to the Stockfish binary.
     * Example: src/main/resources/stockfish/stockfish.exe
     */
    private String path;

    private int depth = 15;
    private long timeoutMs = 8000;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}

