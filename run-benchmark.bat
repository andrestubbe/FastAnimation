@echo off
chcp 65001 >nul


echo 🚀 Running Benchmark...
cd examples\Benchmark
java --sun-misc-unsafe-memory-access=allow -jar target\benchmarks.jar
cd ..\..
pause
