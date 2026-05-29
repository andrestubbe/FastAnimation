$utf8NoBom = New-Object System.Text.UTF8Encoding $false

function Write-Bat ($file, $demoName, $projectPath, $mainClass) {
    $text = "@echo off`r`nchcp 65001 >nul`r`n`r`necho ⚡ Building Main Project...`r`ncall mvn -q clean install -DskipTests`r`nif `%ERRORLEVEL`% NEQ 0 ( pause & exit /b )`r`necho 🚀 Running $demoName...`r`ncd examples\$projectPath`r`ncall mvn -q compile exec:java -Dexec.mainClass=fastanimation.$mainClass`r`ncd ..\..`r`npause`r`n"
    [System.IO.File]::WriteAllText("C:\Users\andre\Documents\2026-05-17-Work-FastJava\FastAnimation\$file", $text, $utf8NoBom)
}

Write-Bat "run-benchmark.bat" "Benchmark" "Benchmark" "Benchmark"
Write-Bat "run-demo1.bat" "Demo 1" "Demo1" "Demo1"
Write-Bat "run-demo2.bat" "Demo 2" "Demo2" "Demo2"
Write-Bat "run-demo3.bat" "Demo 3" "Demo3" "Demo3"
Write-Bat "run-demo4.bat" "Demo 4" "Demo4" "Demo4"
