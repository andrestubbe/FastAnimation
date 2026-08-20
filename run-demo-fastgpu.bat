@echo off
chcp 65001 >nul

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED
if defined VULKAN_SDK set PATH=%VULKAN_SDK%\Bin;%PATH%
if exist "C:\Program Files\VulkanSDK\1.4.357.0\Bin" set PATH=C:\Program Files\VulkanSDK\1.4.357.0\Bin;%PATH%

echo Building Core Library and FastGPU Demo...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Running FastGPU Particle Timeline Demo (100,000 Particles)...
cd examples\Demo
call mvn compile exec:java -Dexec.mainClass="fastanimation.ParticleGPUDemo" -q
if %ERRORLEVEL% NEQ 0 (
    echo Demo failed.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
pause
