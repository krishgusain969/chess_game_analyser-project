@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"

echo ============================================================
echo Chess Analyzer - Run Full Stack
echo Backend:   Spring Boot (http://localhost:8080)
echo Frontend: Vite React   (http://localhost:5173)
echo ============================================================
echo.

rem Check for Maven
set "MAVEN_CMD=mvn"
set "MAVEN_FOUND=0"

where mvn >nul 2>nul
if not errorlevel 1 (
    set "MAVEN_FOUND=1"
    echo [OK] Maven found in PATH: mvn
)

if "!MAVEN_FOUND!"=="0" (
    if exist "%TEMP%\maven\apache-maven-3.9.8\bin\mvn.cmd" (
        set "MAVEN_CMD=%TEMP%\maven\apache-maven-3.9.8\bin\mvn.cmd"
        set "MAVEN_FOUND=1"
        echo [OK] Maven found in TEMP: %MAVEN_CMD%
    )
)

if "!MAVEN_FOUND!"=="0" (
    if exist "%ROOT%backend\mvnw.cmd" (
        set "MAVEN_CMD=%ROOT%backend\mvnw.cmd"
        set "MAVEN_FOUND=1"
        echo [OK] Maven wrapper found: %MAVEN_CMD%
    )
)

if "!MAVEN_FOUND!"=="0" (
    echo [ERROR] Maven is not available.
    echo Please install Maven or use the Maven wrapper.
    echo Download Maven from: https://maven.apache.org/download.cgi
    echo Or run: winget install Apache.Maven
    pause
    exit /b 1
)

rem Check for npm
where npm >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Node.js/NPM is not available.
    echo Please install Node.js from: https://nodejs.org/
    echo Or run: winget install OpenJS.NodeJS.LTS
    pause
    exit /b 1
)
echo [OK] NPM found

echo.

rem Ensure frontend dependencies are installed
if not exist "%ROOT%frontend\node_modules" (
    echo Installing frontend dependencies...
    pushd "%ROOT%frontend"
    call npm install
    if errorlevel 1 (
        echo [ERROR] Failed to install frontend dependencies
        popd
        pause
        exit /b 1
    )
    popd
) else (
    echo [OK] Frontend dependencies already installed
)

echo.
echo ============================================================
echo Starting services...
echo ============================================================
echo.

rem Start backend in a new window
echo Starting backend on http://localhost:8080...
start "Chess Analyzer Backend" cmd /k "cd /d \"%ROOT%backend\" ^&^& echo Backend starting... ^&^& call \"%MAVEN_CMD%\" spring-boot:run"

rem Wait a moment for backend to start
echo Waiting for backend to initialize (10 seconds)...
timeout /t 10 /nobreak >nul

rem Check if backend is running
call curl -s http://localhost:8080/actuator/health >nul 2>&1 || echo Backend may still be starting...

rem Start frontend in a new window
echo Starting frontend on http://localhost:5173...
start "Chess Analyzer Frontend" cmd /k "cd /d \"%ROOT%frontend\" ^&^& echo Frontend starting... ^&^& call npm run dev"

echo.
echo ============================================================
echo Services starting...
echo ============================================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo.

rem Check Stockfish
set "SF_PATH=%ROOT%backend\src\main\resources\stockfish\stockfish.exe"
if exist "%SF_PATH%" (
    echo [OK] Stockfish found: %SF_PATH%
) else (
    echo [WARN] stockfish.exe not found at: %SF_PATH%
    echo        Analysis features will fail until Stockfish is installed.
    echo        Download Stockfish from: https://stockfishchess.org/download/
    echo        Place it in: backend\src\main\resources\stockfish\stockfish.exe
    echo        Or set stockfish.path in application.properties
)

echo.
echo To stop: Close the backend and frontend command windows.
echo.
echo Opening frontend in browser...
start http://localhost:5173/

echo.
echo Launcher complete. Check the backend and frontend windows for status.
echo.
pause