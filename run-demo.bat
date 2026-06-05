@echo off
cls

echo âš¡ Building Main FastTerminal Library...

echo ðŸ”§ Compiling Demo...
cd examples\Demo
call mvn compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )

echo ðŸš€ Running Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*" fastterminal.Demo
