@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Demo1
echo ===========================================
echo.

cd examples\Demo1
call mvn compile exec:java -Dexec.mainClass="fastanimation.Demo1" -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    pause
)

cd ..\..
