@echo off
:: Configure Windows Console to UTF-8 to support True-Color Emojis cleanly
chcp 65001 > nul

echo ⚡ Building Main FastTerminal Library...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )

echo ⚡ Compiling Demo Classes...
cd examples\Demo
call mvn compile -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & pause & exit /b )

echo 🚀 Running Fullscreen True-Color Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;../../target/fastterminal-0.1.0.jar" fastterminal.Demo

cd ..\..
pause
