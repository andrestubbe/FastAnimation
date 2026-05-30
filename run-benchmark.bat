@echo off

echo --- Building Main Project ---
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )

echo --- Running Benchmark ---
cd examples\Benchmark
call mvn -q clean package
java --sun-misc-unsafe-memory-access=allow -jar target\benchmarks.jar
cd ..\..
pause
