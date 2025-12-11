@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo eHealth Monorepo - Setup Script
echo ==========================================

REM Vérifier les prérequis
echo Verification des prerequis...

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo X Node.js n'est pas installe
    exit /b 1
)

where pnpm >nul 2>nul
if %errorlevel% neq 0 (
    echo X pnpm n'est pas installe
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo X Maven n'est pas installe
    exit /b 1
)

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo X Java n'est pas installe
    exit /b 1
)

echo OK Tous les prerequis sont installes

REM Installer les dépendances Node.js
echo.
echo Installation des dependances Node.js...
call pnpm install

REM Installer les dépendances Java
echo.
echo Installation des dependances Java...
call mvn clean install -DskipTests

echo.
echo ==========================================
echo OK Setup complete avec succes!
echo ==========================================
echo.
echo Commandes disponibles:
echo   pnpm dev              - Demarrer le developpement
echo   pnpm build            - Compiler tous les services
echo   pnpm test             - Executer les tests
echo   pnpm lint             - Verifier le linting
echo   pnpm format           - Formater le code
echo   pnpm docker:up        - Demarrer Docker Compose
echo   pnpm docker:down      - Arreter Docker Compose
