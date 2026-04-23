@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo eHealth Monorepo - Build Script
echo ==========================================

REM Build des services Java
echo.
echo Build des services Java...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo X Build Java a echoue
    exit /b 1
)

REM Build des services Node.js
echo.
echo Build des services Node.js...
call pnpm build
if %errorlevel% neq 0 (
    echo X Build Node.js a echoue
    exit /b 1
)

echo.
echo ==========================================
echo OK Build complete avec succes!
echo ==========================================
