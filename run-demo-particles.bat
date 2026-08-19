@echo off
setlocal
cd /d "%~dp0"

echo [FastAnimation] Compiling and starting ParticleTimelineDemo (50k particles)...
call mvn clean compile exec:java -Dexec.mainClass="fastanimation.demo.ParticleTimelineDemo" -q
