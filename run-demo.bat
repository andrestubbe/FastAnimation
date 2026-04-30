@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Demo (v0.1.0)
echo ===========================================
echo.

cd examples
:: Run with -q to hide Maven noise
call mvn compile exec:java -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    pause
)

cd ..
