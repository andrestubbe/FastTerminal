@echo off
chcp 65001 > nul

echo ⚡ Building Main FastTerminal Library...
call mvn -q package -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo Build failed. & pause & exit /b )

echo 🔧 Compiling Demo...
cd examples\Demo
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )
cd ..\..

echo 🚀 Running Demo...
java --enable-native-access=ALL-UNNAMED -cp "examples\Demo\target\classes;examples\Demo\target\dependency\*" fastterminal.Overlay
