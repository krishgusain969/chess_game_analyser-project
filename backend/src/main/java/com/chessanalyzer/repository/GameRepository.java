package com.chessanalyzer.repository;

import com.chessanalyzer.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}

