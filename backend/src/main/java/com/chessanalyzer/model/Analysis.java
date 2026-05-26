package com.chessanalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long gameId;

    @Lob
    @Column(nullable = false)
    private String movesJson;

    private int totalBlunders;
    private int totalMistakes;
    private int totalInaccuracies;

    private double accuracyWhite;
    private double accuracyBlack;

    private LocalDateTime analyzedAt;

    public Long getId() {
        return id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getMovesJson() {
        return movesJson;
    }

    public void setMovesJson(String movesJson) {
        this.movesJson = movesJson;
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

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}

