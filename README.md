# Chess Game Analyzer

A full-stack chess analysis application with Stockfish integration.

## Features
- **PGN Import**: Paste any PGN and get move-by-move analysis
- **Stockfish Integration**: Engine evaluation at depth 15
- **Move Classification**: Brilliant, Good, Inaccuracy, Mistake, Blunder
- **Accuracy Scores**: Per-player accuracy percentage
- **Interactive Board**: Click through moves with evaluation arrows
- **Game History**: Save and review past analyses

## Tech Stack
- **Backend**: Java 21 + Spring Boot 3.3.2 + Spring Data JPA
- **Frontend**: React 18 + Vite 5 + Tailwind CSS
- **Database**: H2 (in-memory, default) or PostgreSQL
- **Chess Engine**: Stockfish (UCI protocol via subprocess)
- **Chess Library**: chesslib (Java) + chess.js (JavaScript)

## Quick Start

### Prerequisites
| Requirement | Version | Check Command |
|-------------|---------|---------------|
| Java | 21+ | `java -version` |
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |
| Maven | 3.9+ | `mvn -version` |

### Option 1: Using run.bat (Windows)
```batch
cd chess-analyzer
.\run.bat
```

This will:
1. Detect Maven (from PATH, TEMP, or Maven wrapper)
2. Install frontend dependencies if needed
3. Start backend on http://localhost:8080
4. Start frontend on http://localhost:5173
5. Open browser to the frontend
6. Verify Stockfish binary

### Option 2: Manual Start

**Backend:**
```bash
cd chess-analyzer/backend
mvn spring-boot:run
```

**Frontend (in a new terminal):**
```bash
cd chess-analyzer/frontend
npm install  # only needed first time
npm run dev
```

## Stockfish Setup

### Windows
1. Download Stockfish from [official site](https://stockfishchess.org/download/)
2. Place `stockfish.exe` at: `backend/src/main/resources/stockfish/stockfish.exe`

### Linux/Mac
1. Install via package manager: `apt install stockfish` or `brew install stockfish`
2. Set path in `backend/src/main/resources/application.properties`:
   ```properties
   stockfish.path=/usr/games/stockfish
   ```

### Alternative: System PATH
If Stockfish is in your system PATH, the application will find it automatically.

### Configuration
Edit `backend/src/main/resources/application.properties`:
```properties
stockfish.path=src/main/resources/stockfish/stockfish.exe  # default
stockfish.depth=15                                          # search depth
stockfish.timeoutMs=8000                                    # analysis timeout
```

## API Endpoints

### Analysis
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/analysis/analyze` | Analyze a PGN game |
| POST | `/api/analysis/position` | Analyze a single FEN position |
| GET | `/api/analysis/{gameId}` | Get saved analysis by game ID |

### Games
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/games/import` | Import/save a PGN game |
| GET | `/api/games` | List all saved games |
| GET | `/api/games/{id}` | Get a specific game |
| DELETE | `/api/games/{id}` | Delete a game |

### Board
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/board/validate` | Check if a move is legal |
| POST | `/api/board/legal-moves` | Get all legal moves for a FEN |
| POST | `/api/board/fen-from-pgn` | Get board position after N moves |

## Database Configuration

Default: In-memory H2 database (data lost on restart).

To use PostgreSQL, set environment variables:
```bash
DB_URL=jdbc:postgresql://localhost:5432/chess_analyzer
DB_USER=postgres
DB_PASSWORD=your_password
DB_DRIVER=org.postgresql.Driver
DB_DDL_AUTO=update
```

H2 Console (dev only): http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:chess_analyzer`
- Username: `sa`
- Password: (empty)

## Move Classification

| Classification | Eval Drop | Quality Score |
|----------------|-----------|---------------|
| Brilliant | Gain ≥ 50cp | 100 |
| Good | -50cp < change < 50cp | 85 |
| Inaccuracy | Drop 50-99cp | 70 |
| Mistake | Drop 100-199cp | 50 |
| Blunder | Drop ≥ 200cp | 20 |

## Troubleshooting

### "Maven not found"
- Install Maven: `winget install Apache.Maven`
- Or download from [maven.apache.org](https://maven.apache.org/download.cgi)
- Extract to a folder and add `bin` to PATH

### "Stockfish not found"
- Download Stockfish binary
- Place at `backend/src/main/resources/stockfish/stockfish.exe`
- Or set `stockfish.path` in `application.properties`

### "Port 8080 already in use"
- Kill the process: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`
- Or change port in `application.properties`: `server.port=8081`

### "Port 5173 already in use"
- Change port in `frontend/vite.config.js`: `port: 5174`

### Frontend can't connect to backend
- Ensure backend is running on port 8080
- Check CORS settings in `CorsConfig.java`
- Verify `BASE` URL in `frontend/src/services/api.js`

### Java version errors
- Requires Java 21 or higher
- Check: `java -version`
- Download from [Adoptium](https://adoptium.net/)

## Project Structure

```
chess-analyzer/
├── run.bat                    # One-click launcher
├── README.md                  # This file
├── backend/                   # Spring Boot application
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/chessanalyzer/
│       │   ├── ChessAnalyzerApplication.java
│       │   ├── config/        # CORS, Stockfish config
│       │   ├── controller/    # REST API endpoints
│       │   ├── model/         # JPA entities
│       │   ├── repository/    # Spring Data repositories
│       │   └── service/       # Business logic
│       └── resources/
│           ├── application.properties
│           └── stockfish/     # Stockfish binary location
└── frontend/                  # React + Vite application
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── components/        # Reusable UI components
        ├── pages/             # Page components
        └── services/          # API client
```

## License

MIT