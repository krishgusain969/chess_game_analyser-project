package com.chessanalyzer.controller;

import com.chessanalyzer.exception.GlobalExceptionHandler;
import com.chessanalyzer.model.Game;
import com.chessanalyzer.service.GameService;
import com.chessanalyzer.repository.AnalysisRepository;
import com.chessanalyzer.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameRepository gameRepository;
    private final AnalysisRepository analysisRepository;
    private final GameService gameService;

    public GameController(GameRepository gameRepository, AnalysisRepository analysisRepository, GameService gameService) {
        this.gameRepository = gameRepository;
        this.analysisRepository = analysisRepository;
        this.gameService = gameService;
    }

    @PostMapping("/games/import")
    public ResponseEntity<GameImportResponse> importGame(@RequestBody ImportGameRequest request) {
        Game game = gameService.importGame(request.getPgn());
        GameImportResponse response = new GameImportResponse();
        response.setGameId(game.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/games")
    public List<GameSummary> listGames() {
        return gameRepository.findAll().stream()
                .sorted(Comparator.comparing(Game::getImportedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(game -> {
                    GameSummary summary = new GameSummary();
                    summary.setId(game.getId());
                    summary.setWhite(game.getWhite());
                    summary.setBlack(game.getBlack());
                    summary.setResult(game.getResult());
                    summary.setImportedAt(game.getImportedAt());
                    return summary;
                })
                .toList();
    }

    @GetMapping("/games/{id}")
    public Game getGame(@PathVariable("id") Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: id=" + id));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable("id") Long id) {
        analysisRepository.deleteByGameId(id);
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Game not found: id=" + id);
        }
        gameRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class ImportGameRequest {
        private String pgn;

        public String getPgn() {
            return pgn;
        }

        public void setPgn(String pgn) {
            this.pgn = pgn;
        }
    }

    public static class GameImportResponse {
        private Long gameId;

        public Long getGameId() {
            return gameId;
        }

        public void setGameId(Long gameId) {
            this.gameId = gameId;
        }
    }

    public static class GameSummary {
        private Long id;
        private String white;
        private String black;
        private String result;
        private LocalDateTime importedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getWhite() {
            return white;
        }

        public void setWhite(String white) {
            this.white = white;
        }

        public String getBlack() {
            return black;
        }

        public void setBlack(String black) {
            this.black = black;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public LocalDateTime getImportedAt() {
            return importedAt;
        }

        public void setImportedAt(LocalDateTime importedAt) {
            this.importedAt = importedAt;
        }
    }
}

