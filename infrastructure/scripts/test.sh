#!/bin/bash
set -e

echo "=========================================="
echo "eHealth Monorepo - Test Script"
echo "=========================================="

# Tests des services Java
echo ""
echo "Exécution des tests Java..."
mvn test

# Tests des services Node.js
echo ""
echo "Exécution des tests Node.js..."
pnpm test

echo ""
echo "=========================================="
echo "✅ Tests complétés avec succès!"
echo "=========================================="
