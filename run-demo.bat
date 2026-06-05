@echo off

echo ==========================================
echo   FastAnimation v0.1.0 - Demo
echo ==========================================
echo.

echo.
echo Running Demo (fetching dependencies strictly from JitPack)...
echo.
cd examples\Demo
call mvn compile exec:java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
)
cd ..\..
pause
