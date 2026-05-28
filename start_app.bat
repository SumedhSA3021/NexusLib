@echo off
title WE-देसी Library Management System - Startup
echo =========================================================================
echo               WE-देसी — LIBRARY MANAGEMENT SYSTEM STARTUP
echo =========================================================================
echo.

:: 1. Check if MySQL is running on port 3306
echo [1/3] Verifying MySQL database status...
netstat -ano | findstr :3306 >nul
if %errorlevel% neq 0 (
    echo [WARNING] MySQL does not seem to be running on port 3306.
    echo Please make sure WAMP/XAMPP or local MySQL is started!
    echo.
    pause
) else (
    echo [OK] MySQL is active and listening on port 3306.
)
echo.

:: 2. Launch frontend in the default web browser
echo [2/3] Opening user interface in browser...
start "" "%~dp0frontend\index.html"
echo [OK] UI launched.
echo.

:: 3. Launch Spring Boot REST Backend
echo [3/3] Launching Spring Boot REST Backend on port 8080...
echo Keep this window open while using the application.
echo -------------------------------------------------------------------------
call "%~dp0maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
