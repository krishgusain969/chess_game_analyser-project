package com.chessanalyzer.service;

import com.chessanalyzer.model.Game;
import com.chessanalyzer.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game importGame(String pgn) {
        if (!StringUtils.hasText(pgn)) {
            throw new IllegalArgumentException("PGN must not be empty.");
        }

        String white = extractHeader(pgn, "White");
        String black = extractHeader(pgn, "Black");
        String result = extractHeader(pgn, "Result");

        if (!StringUtils.hasText(result)) {
            result = extractResultFromTail(pgn);
        }

        Game game = new Game();
        game.setPgn(pgn);
        game.setWhite(white);
        game.setBlack(black);
        game.setResult(result);
        return gameRepository.save(game);
    }

    private static String extractHeader(String pgn, String headerKey) {
        Pattern pattern = Pattern.compile("^\\s*\\[" + Pattern.quote(headerKey) + "\\s+\"([^\"]*)\"\\]\\s*$",
                Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(pgn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String extractResultFromTail(String pgn) {
        Pattern pattern = Pattern.compile("(1-0|0-1|1/2-1/2|\\*)\\s*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(pgn.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

