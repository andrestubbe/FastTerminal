@echo off
:: Configure Windows Console to UTF-8 to support True-Color and Grid rendering cleanly
chcp 65001 > nul

echo [INFO] Building Main FastTerminal Library...
call mvn -q package -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )

echo [INFO] Compiling Demo...
cd examples\Demo
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & pause & exit /b )

echo [INFO] Running Interactive Component Palette Slideshow Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*;../../target/fastterminal-0.1.0.jar" fastterminal.InteractiveUIDemo

cd ..\..
pause
