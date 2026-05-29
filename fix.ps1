$path = "run-benchmark.bat"
$text = "@echo off`r`nchcp 65001 >nul`r`n`r`necho ⚡ Building Main Project...`r`ncall mvn -q clean install -DskipTests`r`nif `%ERRORLEVEL`% NEQ 0 ( pause & exit /b )`r`necho 🚀 Running Benchmark...`r`ncd examples\Benchmark`r`ncall mvn -q compile exec:java -Dexec.mainClass=fastanimation.Benchmark`r`ncd ..\..`r`npause`r`n"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($path, $text, $utf8NoBom)
