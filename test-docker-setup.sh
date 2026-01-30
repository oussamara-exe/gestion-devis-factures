#!/bin/bash
# Script de test pour valider le setup Docker

set -e

echo "🐳 Test du setup DevOps Docker"
echo "================================"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Vérification des fichiers
echo "📋 Vérification des fichiers..."
files=(
  "Dockerfile"
  "frontend/Dockerfile"
  "docker-compose.yml"
  ".github/workflows/ci-cd.yml"
  ".github/dependabot.yml"
  ".dockerignore"
  "frontend/.dockerignore"
  ".gitignore"
  "env.example"
)

missing_files=0
for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo -e "${GREEN}✅${NC} $file"
  else
    echo -e "${RED}❌${NC} $file (manquant)"
    missing_files=$((missing_files + 1))
  fi
done

echo ""

if [ $missing_files -gt 0 ]; then
  echo -e "${RED}❌ $missing_files fichier(s) manquant(s)${NC}"
  exit 1
fi

# Vérification de la syntaxe docker-compose
echo "🔍 Vérification de la syntaxe docker-compose.yml..."
if docker-compose config --quiet > /dev/null 2>&1; then
  echo -e "${GREEN}✅${NC} docker-compose.yml valide"
else
  echo -e "${RED}❌${NC} Erreur dans docker-compose.yml"
  docker-compose config
  exit 1
fi

echo ""

# Test de build Docker (optionnel - peut prendre du temps)
if [ "$1" == "--build" ]; then
  echo "🏗️  Test de build des images Docker..."
  echo -e "${YELLOW}⚠️  Cela peut prendre plusieurs minutes...${NC}"
  echo ""
  
  echo "Building backend..."
  if docker build -t test-backend:local . > /dev/null 2>&1; then
    echo -e "${GREEN}✅${NC} Backend build réussi"
  else
    echo -e "${RED}❌${NC} Backend build échoué"
    exit 1
  fi
  
  echo "Building frontend..."
  if docker build -t test-frontend:local ./frontend > /dev/null 2>&1; then
    echo -e "${GREEN}✅${NC} Frontend build réussi"
  else
    echo -e "${RED}❌${NC} Frontend build échoué"
    exit 1
  fi
fi

echo ""
echo -e "${GREEN}✅ Tous les tests sont passés !${NC}"
echo ""
echo "Prochaines étapes :"
echo "1. Copiez env.example vers .env : cp env.example .env"
echo "2. Modifiez .env selon vos besoins"
echo "3. Lancez l'application : docker-compose up -d"
echo "4. Vérifiez les logs : docker-compose logs -f"

