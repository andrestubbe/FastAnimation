@echo off
chcp 65001 >nul

echo âš¡ Building Main Project...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )
echo ðŸš€ Running Demo 3...
cd examples\Demo3
call mvn -q compile exec:java -Dexec.mainClass=fastanimation.Demo3
cd ..\..
pause
