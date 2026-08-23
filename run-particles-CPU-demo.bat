@echo off
chcp 65001 >nul

set MAVEN_OPTS=--enable-native-access=ALL-UNNAMED
if exist "C:\Users\andre\tools\apache-maven-3.9.9\bin" set PATH=C:\Users\andre\tools\apache-maven-3.9.9\bin;%PATH%

echo Building Core Library...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Running Particle Timeline CPU Demo (50,000 Particles)...
cd examples\Demo
call mvn compile exec:java -Dexec.mainClass="fastanimation.ParticleCPUDemo" -q
if %ERRORLEVEL% NEQ 0 (
    echo Demo failed.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
pause