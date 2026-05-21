@echo off
chcp 65001 > nul

echo [INFO] Building FastTerminal library...
call mvn -q package -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo Build failed. & pause & exit /b )

echo [INFO] Compiling Demo...
cd examples\Demo
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )
cd ..\..

echo [INFO] Launching Terminal Overlay Demo...
java -Dfile.encoding=UTF-8 --enable-native-access=ALL-UNNAMED -cp "examples\Demo\target\classes;examples\Demo\target\dependency\*" fastterminal.Overlay
