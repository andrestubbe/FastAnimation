@echo off
chcp 65001 >nul

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED

echo Building Core Library...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Running Pure 50,000 Particle Cloud Demo...
cd examples\Demo
call mvn compile exec:java -Dexec.mainClass="fastanimation.PureParticleCloudDemo" -q
if %ERRORLEVEL% NEQ 0 (
    echo Demo failed.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
pause
