@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Demo2
echo ===========================================
echo.

cd examples\Demo2
call mvn compile exec:java -Dexec.mainClass="fastanimation.Demo2" -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    pause
)

cd ..\..
