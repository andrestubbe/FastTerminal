@echo off

echo [INFO] Building FastTerminal library...
call mvn -q package -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo Build failed. & pause & exit /b )

echo [INFO] Compiling Demo...
cd examples\Demo
call mvn -q compile dependency:copy-dependencies -DincludeScope=runtime -DskipTests
if %ERRORLEVEL% NEQ 0 ( cd ..\.. & echo Compile failed. & pause & exit /b )

echo [INFO] Launching UI Demo...
java --enable-native-access=ALL-UNNAMED -cp "target/classes;target/dependency/*;../../target/fastterminal-0.1.0.jar;../../../FastKeyboard/target/FastKeyboard-0.2.0.jar" fastterminal.UI

cd ..\..
pause
