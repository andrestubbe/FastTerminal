@echo off
setlocal EnableDelayedExpansion

REM Change to script directory
cd /d "%~dp0"

echo ===========================================
echo FastTerminal Benchmark Runner
echo ===========================================

echo [INFO] Building Main FastTerminal Project...
    echo [ERROR] Main project build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [INFO] Building Benchmark...
cd examples\Benchmark
    echo [ERROR] Benchmark build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ===========================================
echo FastTerminal Benchmark (120x30 Console)
echo ===========================================
echo.
echo [INFO] Initializing JMH Runner...
echo This will take a few minutes to complete as JMH isolates, 
echo warms up, and measures throughput rigorously.
echo.

REM Run JMH Benchmark (EXTRA verbosity disables the broken # progress bar on Windows)
java -jar target\benchmarks.jar -v EXTRA

echo.
echo [DONE] Benchmark Complete.
