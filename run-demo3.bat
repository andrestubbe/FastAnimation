@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Demo3
echo ===========================================
echo.

cd examples\Demo3
call mvn compile exec:java -Dexec.mainClass="fastanimation.Demo3" -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    pause
)

cd ..\..
