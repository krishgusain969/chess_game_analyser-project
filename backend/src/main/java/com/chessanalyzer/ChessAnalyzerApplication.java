package com.chessanalyzer;

import com.chessanalyzer.config.StockfishConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StockfishConfig.class)
public class ChessAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChessAnalyzerApplication.class, args);
    }
}

