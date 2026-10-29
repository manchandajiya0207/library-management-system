@echo off
title Libra Library Management System
cd /d "%~dp0"
echo ==============================================
echo   LIBRA - Library Management System
echo ==============================================
where javac >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java JDK not found. Install JDK 17 or 21 and configure PATH.
  echo Then close this window and double-click run.bat again.
  pause
  exit /b 1
)
if not exist out mkdir out
echo Compiling Core Java backend...
javac -d out backend\src\*.java
if errorlevel 1 (
  echo Compilation failed. Check Java installation and source files.
  pause
  exit /b 1
)
echo Starting website at http://localhost:8080
echo Keep this window open while using the website. Press Ctrl+C to stop.
start "" http://localhost:8080
java -cp out LibraryManagementSystem
pause
