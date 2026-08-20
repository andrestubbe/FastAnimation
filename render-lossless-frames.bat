@echo off
chcp 65001 >nul

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED
if defined VULKAN_SDK set PATH=%VULKAN_SDK%\Bin;%PATH%
if exist "C:\Program Files\VulkanSDK\1.4.357.0\Bin" set PATH=C:\Program Files\VulkanSDK\1.4.357.0\Bin;%PATH%

echo Building Core Library and Recorder...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Lossless 60 FPS Frame Sequence Render (3600 Frames -> docs\render_frames)...
cd examples\Demo
call mvn compile exec:java -Dexec.mainClass="fastanimation.ParticleGPURecorder" -q
if %ERRORLEVEL% NEQ 0 (
    echo Render failed.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
echo.
echo All 3600 frames rendered to FastAnimation\docs\render_frames\
pause
