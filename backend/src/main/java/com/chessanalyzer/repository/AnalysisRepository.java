package com.chessanalyzer.repository;

import com.chessanalyzer.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Optional<Analysis> findByGameId(Long gameId);

    void deleteByGameId(Long gameId);
}

