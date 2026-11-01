@echo off
setlocal enabledelayedexpansion
title PayrollPulse - Employee Payroll Management System

echo ========================================================
echo   PayrollPulse - Employee Payroll Management System
echo ========================================================
echo.

rem Look for javac/java in standard paths or JDK folders
set "JAVA_CMD=java"
set "JAVAC_CMD=javac"

if exist "C:\Program Files\Java\jdk-25.0.3\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-25.0.3\bin\javac.exe"
    set "JAVA_CMD=C:\Program Files\Java\jdk-25.0.3\bin\java.exe"
)

echo [1/3] Compiling Java backend sources...
if not exist "bin" mkdir "bin"

"%JAVAC_CMD%" -d bin -sourcepath src src\com\payroll\PayrollServer.java
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ or JDK 25 is installed.
    pause
    exit /b %ERRORLEVEL%
)
echo [OK] Compilation successful!
echo.

echo [2/3] Starting PayrollPulse Server on http://localhost:8080/ ...
start "" "http://localhost:8080/"

echo [3/3] Server running. Close this window or press Ctrl+C to terminate.
echo.
"%JAVA_CMD%" -cp bin com.payroll.PayrollServer

pause
