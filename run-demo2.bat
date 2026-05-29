@echo off
chcp 65001 >nul

echo âš¡ Building Main Project...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )
echo ðŸš€ Running Demo 2...
cd examples\Demo2
call mvn -q compile exec:java -Dexec.mainClass=fastanimation.Demo2
cd ..\..
pause
