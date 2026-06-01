@echo off
setlocal EnableDelayedExpansion

REM Change to script directory
cd /d "%~dp0"

echo ===========================================
echo FastTerminal Benchmark Runner
echo ===========================================

echo [INFO] Building Main FastTerminal Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Main project build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [INFO] Building Benchmark...
cd examples\Benchmark
call mvn clean package -q
if %ERRORLEVEL% neq 0 (
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

REM Run JMH Benchmark (Hide Experimental VM warnings)
java -jar target\benchmarks.jar | findstr /V /C:"NOTE: Current JVM experimentally supports Compiler Blackholes" /C:"extra caution when trusting the results" /C:"works, and factor in a small probability" /C:"different JVMs are already problematic" /C:"modes can be very significant"

echo.
echo [DONE] Benchmark Complete.
