@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ===========================================
echo FastAnimation Builder (v0.1.0)
echo ===========================================
echo.

:: 1. Setup Java Environment
if not defined JAVA_HOME (
    echo JAVA_HOME not defined. Searching for JDK...
    for /d %%i in ("C:\Program Files\Java\jdk-*") do (
        set "JAVA_HOME=%%i"
    )
)

if not defined JAVA_HOME (
    echo ERROR: Could not find a JDK in C:\Program Files\Java.
    echo Please set JAVA_HOME manually.
    pause
    exit /b 1
)

echo Using JDK: %JAVA_HOME%

:: 2. Setup VS Environment (Blueprint Standard)
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
if not exist "%VSWHERE%" (
    echo [NOTE] vswhere.exe not found. Skipping native build (Pure Java module).
) else (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VS_INSTALL=%%i"
    )
    if defined VS_INSTALL (
        set "VCVARS=%VS_INSTALL%\VC\Auxiliary\Build\vcvars64.bat"
        echo Found Visual Studio at: !VS_INSTALL!
        call "!VCVARS!" > nul
    )
)

:: 3. Build Java Library (Quiet Mode)
echo.
echo Building Java Library...
call mvn clean install -DskipTests -q
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed.
    pause
    exit /b %errorlevel%
)

echo.
echo ===========================================
echo BUILD SUCCESSFUL! 
echo ===========================================
pause
