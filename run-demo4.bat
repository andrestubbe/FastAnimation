@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Demo4
echo ===========================================
echo.

cd examples\Demo4
call mvn compile exec:java -Dexec.mainClass="fastanimation.Demo4" -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    pause
)

cd ..\..
