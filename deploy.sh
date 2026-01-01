#!/bin/bash
# Script d'aide au déploiement

set -e

echo "🚀 Script de Déploiement - Gestion Devis et Factures"
echo "======================================================"
echo ""

# Vérification Git
if [ ! -d ".git" ]; then
    echo "📦 Initialisation du repository Git..."
    git init
    git add .
    git commit -m "Initial commit - Setup DevOps complet"
    echo "✅ Repository Git initialisé"
    echo ""
fi

# Vérification du remote GitHub
if ! git remote | grep -q "origin"; then
    echo "⚠️  Aucun remote GitHub configuré"
    echo ""
    echo "Pour connecter votre repository GitHub :"
    echo "  1. Créez un nouveau repository sur https://github.com/new"
    echo "  2. Exécutez :"
    echo "     git remote add origin https://github.com/VOTRE_USERNAME/VOTRE_REPO.git"
    echo "     git branch -M main"
    echo "     git push -u origin main"
    echo ""
else
    echo "✅ Remote GitHub configuré :"
    git remote -v
    echo ""
fi

echo "📋 Checklist de Déploiement :"
echo ""
echo "1. ✅ Repository Git initialisé"
echo "2. ⏭️  Pousser le code sur GitHub"
echo "3. ⏭️  Configurer Vercel pour le frontend"
echo "4. ⏭️  Configurer Render/Railway pour le backend"
echo "5. ⏭️  Configurer les variables d'environnement"
echo ""
echo "📚 Consultez DEPLOY_INSTRUCTIONS.md pour les instructions détaillées"
echo ""

