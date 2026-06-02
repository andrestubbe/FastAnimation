@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==========================================
echo   FastAnimation v0.1.0 - Demo
echo ==========================================
echo.
echo Installing FastAnimation locally...
call mvn -q install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed.
    pause
    exit /b
)

echo.
echo Running Demo (deps from JitPack, FastAnimation local)...
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
