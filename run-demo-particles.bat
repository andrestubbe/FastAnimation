@echo off
chcp 65001 >nul

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED

echo ⚡ Building Core Library...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Particle Timeline Demo (50,000 Particles)...
cd examples\Demo
call mvn compile exec:java -Dexec.mainClass=fastanimation.demo.ParticleTimelineDemo -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Demo failed. & pause & exit /b %ERRORLEVEL% )

cd ..\..
pause
