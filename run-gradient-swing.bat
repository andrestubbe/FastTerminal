@echo off
cls

echo Building Main FastTerminal Library...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo Install failed. & pause & exit /b )

echo Compiling Gradient Demo...
cd examples\Palette
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )

echo Running Gradient Demo (Swing Mode)...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*" fastterminal.RunGradient --swing %*

cd ..\..
