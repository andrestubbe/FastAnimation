@echo off
chcp 65001 >nul

echo ⚡ Building Main Project...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )
echo 🚀 Running Benchmark...
cd examples\Benchmark
call mvn -q compile exec:java -Dexec.mainClass=fastanimation.Benchmark
cd ..\..
pause
