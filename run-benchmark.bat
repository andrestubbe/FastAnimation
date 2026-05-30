@echo off
chcp 65001 >nul

echo ðŸš€ Building Main Project...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )

echo âš¡ Running Benchmark...
cd examples\Benchmark
call mvn -q clean package
java -jar target\benchmarks.jar
cd ..\..
pause
