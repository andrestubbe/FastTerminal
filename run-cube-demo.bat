@echo off
:: Configure Windows Console to UTF-8 to support True-Color and Grid rendering cleanly
chcp 65001 > nul

echo [INFO] Building Main FastTerminal Library...
call mvn -q clean package -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )

echo [INFO] Compiling Demo Classes...
cd examples\Demo
call mvn -q compile -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & pause & exit /b )

echo [INFO] Running Fullscreen 3D Rotating Cube Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;../../target/fastterminal-0.1.0.jar" fastterminal.CubeDemo

cd ..\..
pause
