@echo off
chcp 65001 >nul

echo ⚡ Building Main Project...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Benchmark...
cd examples\Benchmark
java --sun-misc-unsafe-memory-access=allow -jar target\benchmarks.jar
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )


cd ..\..
pause
