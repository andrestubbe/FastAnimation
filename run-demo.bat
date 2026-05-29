@echo off
echo ==============================================
echo   Starting FastAnimation Demo (120 FPS Lock)
echo ==============================================
cd examples\Demo
mvn compile exec:java
pause
