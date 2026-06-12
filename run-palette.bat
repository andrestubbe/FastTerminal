@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠  Compiling Palette Demo...
cd examples\Palette
call mvn compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Palette Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*" fastterminal.RunPalette %*

cd ..\..
pause
