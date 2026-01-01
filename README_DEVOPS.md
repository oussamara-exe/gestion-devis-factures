# 🚀 Setup DevOps - Guide Rapide

## Fichiers créés

✅ **Dockerfile** - Backend Spring Boot optimisé multi-stage  
✅ **frontend/Dockerfile** - Frontend React/Vite optimisé avec Nginx  
✅ **docker-compose.yml** - Orchestration complète (frontend, backend, MySQL)  
✅ **.github/workflows/ci-cd.yml** - Pipeline CI/CD GitHub Actions  
✅ **.github/dependabot.yml** - Mises à jour automatiques de sécurité  
✅ **.dockerignore** & **frontend/.dockerignore** - Optimisation des builds  
✅ **.gitignore** - Exclusion des fichiers sensibles  
✅ **env.example** - Template de variables d'environnement  

## 🚀 Démarrage rapide

### 1. Local avec Docker
```bash
cp env.example .env
docker-compose up -d
```

### 2. Configuration GitHub Secrets
Ajoutez dans GitHub → Settings → Secrets :
- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`

### 3. Déploiement
Consultez `DEPLOIEMENT.md` pour les options détaillées.

## 📚 Documentation complète
Voir `DEPLOIEMENT.md` pour tous les détails.

