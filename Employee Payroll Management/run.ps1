# PayrollPulse Launcher Script for Windows PowerShell
$ErrorActionPreference = "Stop"

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "   PayrollPulse - Employee Payroll Management System   " -ForegroundColor Yellow
Write-Host "=======================================================" -ForegroundColor Cyan

$javaBin = "C:\Program Files\Java\jdk-25.0.3\bin"
if (Test-Path "$javaBin\javac.exe") {
    $javac = "$javaBin\javac.exe"
    $java = "$javaBin\java.exe"
} else {
    $javac = "javac"
    $java = "java"
}

Write-Host "[1/3] Compiling Java backend..." -ForegroundColor Green
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }

& $javac -d bin -sourcepath src src/com/payroll/PayrollServer.java
Write-Host "[OK] Compilation successful!" -ForegroundColor Green

Write-Host "[2/3] Launching web dashboard in default browser..." -ForegroundColor Green
Start-Process "http://localhost:8080/"

Write-Host "[3/3] Running PayrollPulse server on port 8080..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor Gray
& $java -cp bin com.payroll.PayrollServer
