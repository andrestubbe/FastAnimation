@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Benchmark (v0.1.0)
echo ===========================================
echo.

cd examples
:: Run with -q to hide Maven noise
call mvn compile exec:java -Dexec.mainClass="fastanimation.Benchmark" -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Benchmark failed to launch. 
    pause
)

cd ..
