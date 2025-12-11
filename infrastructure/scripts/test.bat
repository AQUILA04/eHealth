@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo eHealth Monorepo - Test Script
echo ==========================================

REM Tests des services Java
echo.
echo Execution des tests Java...
call mvn test
if %errorlevel% neq 0 (
    echo X Tests Java ont echoue
    exit /b 1
)

REM Tests des services Node.js
echo.
echo Execution des tests Node.js...
call pnpm test
if %errorlevel% neq 0 (
    echo X Tests Node.js ont echoue
    exit /b 1
)

echo.
echo ==========================================
echo OK Tests complets avec succes!
echo ==========================================
