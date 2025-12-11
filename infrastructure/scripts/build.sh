#!/bin/bash
set -e

echo "=========================================="
echo "eHealth Monorepo - Build Script"
echo "=========================================="

# Build des services Java
echo ""
echo "Build des services Java..."
mvn clean package -DskipTests

# Build des services Node.js
echo ""
echo "Build des services Node.js..."
pnpm build

echo ""
echo "=========================================="
echo "✅ Build complété avec succès!"
echo "=========================================="
