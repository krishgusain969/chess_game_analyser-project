package com.chessanalyzer.controller;

import com.chessanalyzer.model.AnalysisResult;
import com.chessanalyzer.model.PositionAnalysis;
import com.chessanalyzer.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyzeGame(@RequestBody AnalyzeGameRequest request) {
        AnalysisResult result = analysisService.analyzeAndSaveGame(request.getPgn());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/position")
    public ResponseEntity<PositionAnalysis> analyzePosition(@RequestBody AnalyzePositionRequest request) {
        PositionAnalysis response = analysisService.analyzePosition(request.getFen());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<AnalysisResult> getAnalysis(@PathVariable("gameId") Long gameId) {
        AnalysisResult result = analysisService.getAnalysisByGameId(gameId);
        return ResponseEntity.ok(result);
    }

    public static class AnalyzeGameRequest {
        private String pgn;

        public String getPgn() {
            return pgn;
        }

        public void setPgn(String pgn) {
            this.pgn = pgn;
        }
    }

    public static class AnalyzePositionRequest {
        private String fen;

        public String getFen() {
            return fen;
        }

        public void setFen(String fen) {
            this.fen = fen;
        }
    }
}

