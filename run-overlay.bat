@echo off
chcp 65001 > nul

echo âš¡ Building Main FastTerminal Library...

echo ðŸ”§ Compiling Demo...
cd examples\Demo
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )
cd ..\..

echo ðŸš€ Running Demo...
java --enable-native-access=ALL-UNNAMED -cp "examples\Demo\target\classes;examples\Demo\target\dependency\*" fastterminal.Overlay
