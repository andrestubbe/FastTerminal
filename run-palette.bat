@echo off
if "%~1"==":utf8" goto :utf8
chcp 65001 >nul
cmd /c "%~f0" :utf8 %*
exit /b

:utf8
shift
cls

echo âš¡ Building Main FastTerminal Library...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo Install failed. & pause & exit /b )

echo ðŸ”§ Compiling Palette Demo...
cd examples\Palette
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )

echo ðŸš€ Running Palette Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*" fastterminal.RunPalette %1 %2 %3 %4 %5 %6 %7 %8 %9

cd ..\..
