package com.chessanalyzer.service;

import com.chessanalyzer.config.StockfishConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class StockfishService {

    public static final class EngineAnalysis {
        private final int evalScoreCp;
        private final int depth;
        private final String bestMove;
        private final String pvLine;

        public EngineAnalysis(int evalScoreCp, int depth, String bestMove, String pvLine) {
            this.evalScoreCp = evalScoreCp;
            this.depth = depth;
            this.bestMove = bestMove;
            this.pvLine = pvLine;
        }

        public int getEvalScoreCp() {
            return evalScoreCp;
        }

        public int getDepth() {
            return depth;
        }

        public String getBestMove() {
            return bestMove;
        }

        public String getPvLine() {
            return pvLine;
        }
    }

    private final StockfishConfig config;
    private final Object engineLock = new Object();

    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean uciInitialized = false;

    private final ExecutorService readExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "stockfish-reader");
        t.setDaemon(true);
        return t;
    });

    public StockfishService(StockfishConfig config) {
        this.config = config;
    }

    // Stockfish is initialized lazily when an analysis request arrives.
    // This allows the backend to start even when the binary is not yet present.
    public void start() throws IOException {
        synchronized (engineLock) {
            startEngineIfNeeded();
            initUciIfNeeded();
        }
    }

    @PreDestroy
    public void shutdown() {
        synchronized (engineLock) {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
        readExecutor.shutdownNow();
    }

    public EngineAnalysis analyzePosition(String fen) {
        Objects.requireNonNull(fen, "fen");
        if (!StringUtils.hasText(fen)) {
            throw new IllegalArgumentException("fen must not be empty");
        }

        synchronized (engineLock) {
            try {
                startEngineIfNeeded();
                initUciIfNeeded();

                // Always set the exact position right before running search.
                sendLine("position fen " + fen.trim());
                sendLine("go depth " + config.getDepth());

                return readUntilBestMove(config.getTimeoutMs());
            } catch (IOException e) {
                // If the process broke mid-search, restart and surface a clear error.
                restartEngineQuietly();
                throw new RuntimeException("Stockfish engine error: " + e.getMessage(), e);
            }
        }
    }

    public String getBestMove(String fen) {
        return analyzePosition(fen).getBestMove();
    }

    private void startEngineIfNeeded() throws IOException {
        if (process != null && process.isAlive()) {
            return;
        }

        Path binaryPath = resolveStockfishBinary();
        if (binaryPath == null) {
            throw new IllegalStateException(
                    "Stockfish binary not found. Set stockfish.path to an existing file (e.g. stockfish.exe)."
            );
        }

        ProcessBuilder builder = new ProcessBuilder(binaryPath.toAbsolutePath().toString());
        builder.redirectErrorStream(true);

        process = builder.start();
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    private void restartEngineQuietly() {
        try {
            if (process != null) {
                process.destroyForcibly();
            }
        } finally {
            process = null;
            writer = null;
            reader = null;
            uciInitialized = false;
        }
    }

    private void initUciIfNeeded() throws IOException {
        if (uciInitialized) {
            return;
        }

        // Since this method is called inside the lock, no other thread can interfere.
        sendLine("uci");
        readUntil(line -> line.equals("uciok"), config.getTimeoutMs());

        sendLine("isready");
        readUntil(line -> line.equals("readyok"), config.getTimeoutMs());
        uciInitialized = true;
    }

    private void sendLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    private EngineAnalysis readUntilBestMove(long timeoutMs) throws IOException {
        try {
            Future<EngineAnalysis> future = readExecutor.submit(new Callable<EngineAnalysis>() {
                @Override
                public EngineAnalysis call() throws Exception {
                    int lastDepth = 0;
                    Integer lastCpScore = null;
                    Integer lastMateScore = null;
                    String lastPv = "";

                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            throw new IOException("Stockfish process ended unexpectedly.");
                        }

                        if (line.startsWith("info")) {
                            // Example:
                            // info depth 15 seldepth 18 multipv 1 score cp 34 pv e2e4 e7e5 ...
                            String[] tokens = line.trim().split("\\s+");
                            // Depth
                            for (int i = 0; i < tokens.length - 1; i++) {
                                if ("depth".equals(tokens[i])) {
                                    lastDepth = parseIntSafe(tokens[i + 1], lastDepth);
                                }
                            }

                            // Score
                            lastCpScore = null;
                            lastMateScore = null;
                            for (int i = 0; i < tokens.length - 3; i++) {
                                if ("score".equals(tokens[i]) && i + 2 < tokens.length) {
                                    String type = tokens[i + 1];
                                    String val = tokens[i + 2];
                                    if ("cp".equals(type)) {
                                        lastCpScore = parseIntSafe(val, 0);
                                    } else if ("mate".equals(type)) {
                                        lastMateScore = parseIntSafe(val, 0);
                                    }
                                }
                            }

                            // PV
                            int pvIdx = indexOf(tokens, "pv");
                            if (pvIdx >= 0) {
                                List<String> pvTokens = new ArrayList<>();
                                for (int i = pvIdx + 1; i < tokens.length; i++) {
                                    pvTokens.add(tokens[i]);
                                }
                                // Keep PV short for UI readability
                                int maxMoves = 6; // 3 moves (white+black) in UCI pairs
                                if (pvTokens.size() > maxMoves) {
                                    pvTokens = pvTokens.subList(0, maxMoves);
                                }
                                lastPv = String.join(" ", pvTokens);
                            }
                        }

                        if (line.startsWith("bestmove")) {
                            String bestMove = extractBestMove(line);
                            int eval;
                            if (lastCpScore != null) {
                                eval = lastCpScore;
                            } else if (lastMateScore != null) {
                                // Map mate to a large cp value (sign matters for classification).
                                // Stockfish mate is in moves-to-mate; larger magnitude => stronger.
                                eval = lastMateScore > 0 ? 100000 : -100000;
                            } else {
                                eval = 0;
                            }
                            return new EngineAnalysis(eval, lastDepth, bestMove, lastPv);
                        }
                    }
                }
            });

            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Stockfish timed out after " + timeoutMs + "ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Stockfish read interrupted");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw new RuntimeException("Stockfish analysis failed: " + e.getMessage(), e);
        }
    }

    private interface LinePredicate {
        boolean matches(String line);
    }

    private void readUntil(LinePredicate predicate, long timeoutMs) throws IOException {
        try {
            Future<Void> future = readExecutor.submit(() -> {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        throw new IOException("Stockfish process ended unexpectedly.");
                    }
                    if (predicate.matches(line.trim())) {
                        return null;
                    }
                }
            });
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out waiting for Stockfish response.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Timed out waiting for Stockfish response.");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            throw new RuntimeException("Stockfish init failed: " + e.getMessage(), e);
        }
    }

    private static int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int indexOf(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (target.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    private static String extractBestMove(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length >= 2) {
            return tokens[1].trim();
        }
        return "";
    }

    private Path resolveStockfishBinary() {
        // If user explicitly set a path in config, prefer it (absolute or relative lookup).
        if (StringUtils.hasText(config.getPath())) {
            Path configured = Path.of(config.getPath());

            List<Path> candidates = new ArrayList<>();
            if (configured.isAbsolute()) {
                candidates.add(configured);
            } else {
                // 1) relative to current working dir
                candidates.add(Path.of(System.getProperty("user.dir")).resolve(configured));
                // 2) relative to common "backend/" subfolder
                candidates.add(Path.of(System.getProperty("user.dir"), "backend").resolve(configured));
                // 3) relative to repo-level "chess-analyzer/backend/"
                candidates.add(Path.of(System.getProperty("user.dir"), "chess-analyzer", "backend").resolve(configured));
            }

            for (Path candidate : candidates) {
                File file = candidate.toFile();
                if (file.isFile()) {
                    return candidate;
                }

                // If the path is a directory, look for stockfish binary inside it
                if (file.isDirectory()) {
                    String execName = System.getProperty("os.name").toLowerCase().contains("win") ? "stockfish.exe" : "stockfish";
                    Path dirCandidate = candidate.resolve(execName);
                    if (dirCandidate.toFile().isFile()) {
                        return dirCandidate;
                    }
                }

                // Convenience: if user pointed to a binary without extension, try adding .exe (common on Windows).
                if (!candidate.toString().toLowerCase().endsWith(".exe")) {
                    Path exeCandidate = Path.of(candidate.toString() + ".exe");
                    if (exeCandidate.toFile().exists()) {
                        return exeCandidate;
                    }
                }
            }
        }

        // 2) Try environment variable STOCKFISH_PATH
        String envPath = System.getenv("STOCKFISH_PATH");
        if (StringUtils.hasText(envPath)) {
            Path p = Path.of(envPath);
            if (p.toFile().exists()) {
                return p;
            }
            if (!p.toString().toLowerCase().endsWith(".exe")) {
                Path exeP = Path.of(p.toString() + ".exe");
                if (exeP.toFile().exists()) {
                    return exeP;
                }
            }
        }

        // 3) Search the system PATH for a stockfish executable
        String pathEnv = System.getenv("PATH");
        if (StringUtils.hasText(pathEnv)) {
            String execName = System.getProperty("os.name").toLowerCase().contains("win") ? "stockfish.exe" : "stockfish";
            String[] parts = pathEnv.split(File.pathSeparator);
            for (String dir : parts) {
                try {
                    Path p = Path.of(dir).resolve(execName);
                    if (p.toFile().exists()) {
                        return p;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return null;
    }
}

