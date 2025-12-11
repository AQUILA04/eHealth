#!/bin/bash
set -e

echo "=========================================="
echo "eHealth Monorepo - Setup Script"
echo "=========================================="

# Vérifier les prérequis
echo "Vérification des prérequis..."

if ! command -v node &> /dev/null; then
    echo "❌ Node.js n'est pas installé"
    exit 1
fi

if ! command -v pnpm &> /dev/null; then
    echo "❌ pnpm n'est pas installé"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java n'est pas installé"
    exit 1
fi

echo "✅ Tous les prérequis sont installés"

# Installer les dépendances Node.js
echo ""
echo "Installation des dépendances Node.js..."
pnpm install

# Installer les dépendances Java
echo ""
echo "Installation des dépendances Java..."
mvn clean install -DskipTests

echo ""
echo "=========================================="
echo "✅ Setup complété avec succès!"
echo "=========================================="
echo ""
echo "Commandes disponibles:"
echo "  pnpm dev              - Démarrer le développement"
echo "  pnpm build            - Compiler tous les services"
echo "  pnpm test             - Exécuter les tests"
echo "  pnpm lint             - Vérifier le linting"
echo "  pnpm format           - Formater le code"
echo "  pnpm docker:up        - Démarrer Docker Compose"
echo "  pnpm docker:down      - Arrêter Docker Compose"
