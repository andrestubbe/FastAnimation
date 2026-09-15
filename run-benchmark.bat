@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
cd /d "%~dp0"

echo ================================================================================
echo   FastAnimation — Official JMH Performance Benchmark
echo ================================================================================
echo.

echo ⚡ Building Main Project (FastAnimation)...
call mvn install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Main build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Building Benchmark Uber-JAR...
cd examples\Benchmark
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark build failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Official JMH Benchmarks for FastAnimation...
java -Djmh.ignoreLock=true -jar target\benchmarks.jar

cd ..\..
echo.
pause
