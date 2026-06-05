@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==========================================
echo   FastAnimation v0.1.0 - Demo
echo ==========================================
echo.

echo.
echo Running Demo (fetching dependencies strictly from JitPack)...
echo.
cd examples\Demo
call mvn -q compile exec:java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
)
cd ..\..
pause
