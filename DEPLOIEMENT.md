# Guide de Déploiement DevOps

Ce document explique comment déployer l'application de gestion de devis et factures en utilisant les différentes options disponibles.

## 🐳 Utilisation en Local avec Docker Compose

### Prérequis
- Docker et Docker Compose installés
- Ports 3000, 8080, et 3306 disponibles

### Lancement

1. **Copiez le fichier d'environnement :**
   ```bash
   cp env.example .env
   ```

2. **Modifiez les variables dans `.env` si nécessaire**

3. **Lancez tous les services :**
   ```bash
   docker-compose up -d
   ```

4. **Vérifiez les logs :**
   ```bash
   docker-compose logs -f
   ```

5. **Accédez à l'application :**
   - Frontend : http://localhost:3000
   - Backend API : http://localhost:8080
   - MySQL : localhost:3306

### Arrêt des services
```bash
docker-compose down
```

### Suppression des données (volumes)
```bash
docker-compose down -v
```

## 📦 Option A : Déploiement Séparé (Recommandé pour débuter)

### Frontend sur Vercel

1. **Connectez votre repository GitHub à Vercel :**
   - Allez sur https://vercel.com
   - Cliquez sur "New Project"
   - Importez votre repository

2. **Configuration du projet :**
   - **Root Directory** : `frontend`
   - **Framework Preset** : Vite
   - **Build Command** : `npm run build`
   - **Output Directory** : `dist`

3. **Variables d'environnement :**
   - Ajoutez `VITE_API_URL` avec l'URL de votre backend en production

4. **Déploiement automatique :**
   - Vercel déploiera automatiquement à chaque push sur `main`

### Backend sur Render.com

1. **Créez un compte sur https://render.com**

2. **Créez une nouvelle Web Service :**
   - Connectez votre repository GitHub
   - Type : Web Service
   - Build Command : `mvn clean package -DskipTests`
   - Start Command : `java -jar target/gestion-devis-factures-1.0.0.jar`

3. **Configurez les variables d'environnement :**
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://votre-host-mysql:3306/gestion_devis_factures
   SPRING_DATASOURCE_USERNAME=votre_user
   SPRING_DATASOURCE_PASSWORD=votre_password
   SPRING_WEB_CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app
   ```

4. **Créez une base de données MySQL :**
   - Dans Render, créez un nouveau "PostgreSQL" (ou MySQL si disponible)
   - Copiez l'URL de connexion et utilisez-la dans les variables d'environnement du backend

5. **Déploiement :**
   - Render déploiera automatiquement à chaque push sur `main`

### Alternative : Backend sur Railway.app

1. **Créez un compte sur https://railway.app**

2. **Nouveau projet :**
   - "Deploy from GitHub repo"
   - Sélectionnez votre repository

3. **Configuration :**
   - Railway détectera automatiquement Spring Boot
   - Ajoutez une base de données MySQL

4. **Variables d'environnement :**
   - Railway génère automatiquement les variables pour la base de données
   - Ajoutez : `SPRING_WEB_CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app`

## 🚀 Option B : Déploiement via Docker (Plus DevOps)

### Sur Render.com avec Docker

1. **Créez un nouveau "Web Service" sur Render**

2. **Configuration :**
   - Connectez votre repository GitHub
   - Type : Docker
   - Dockerfile : `Dockerfile` (pour le backend)
   - Dockerfile : `frontend/Dockerfile` (créer deux services séparés)

3. **Variables d'environnement :**
   - Configurez toutes les variables nécessaires

### Sur Fly.io

1. **Installez Fly CLI :**
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Authentifiez-vous :**
   ```bash
   fly auth login
   ```

3. **Initialisez l'application :**
   ```bash
   fly launch
   ```

4. **Configurez les services :**
   - Backend et Frontend comme deux apps séparées
   - Base de données MySQL (flyctl postgres create ou utiliser un service externe)

### Sur DigitalOcean App Platform

1. **Créez un compte sur https://cloud.digitalocean.com**

2. **Nouvelle App :**
   - "Create App" → "GitHub"
   - Sélectionnez votre repository

3. **Configuration :**
   - Backend : Dockerfile
   - Frontend : Dockerfile (dans sous-dossier)
   - Ajoutez une base de données MySQL

## 🔐 Configuration des Secrets GitHub

Pour que le pipeline CI/CD fonctionne, configurez ces secrets dans GitHub :

1. **Allez dans votre repository GitHub → Settings → Secrets and variables → Actions**

2. **Ajoutez les secrets suivants :**
   - `DOCKER_USERNAME` : Votre nom d'utilisateur Docker Hub
   - `DOCKER_PASSWORD` : Votre token Docker Hub
   - `VITE_API_URL` : URL de votre API backend (optionnel)
   - `DEPLOYMENT_URL` : URL de votre application déployée (optionnel)

### Obtenir un token Docker Hub :
1. Connectez-vous sur https://hub.docker.com
2. Account Settings → Security → New Access Token
3. Copiez le token et ajoutez-le comme secret

## 🔄 Pipeline CI/CD

Le pipeline GitHub Actions s'exécute automatiquement :

- **Sur chaque Pull Request :** Tests backend et frontend
- **Sur push vers main :** Tests + Build + Push des images Docker
- **Déploiement automatique :** (à configurer selon votre plateforme)

## 📝 Notes Importantes

1. **Sécurité en Production :**
   - Changez tous les mots de passe par défaut
   - Utilisez des variables d'environnement pour les secrets
   - Activez HTTPS (automatique sur Vercel, Render, etc.)

2. **Base de données :**
   - En production, utilisez une base de données gérée (Render, Railway, AWS RDS, etc.)
   - Ne stockez jamais les credentials dans le code

3. **CORS :**
   - Configurez correctement `SPRING_WEB_CORS_ALLOWED_ORIGINS` avec votre URL frontend

4. **Monitoring :**
   - Surveillez les logs de votre application
   - Configurez des alertes en cas d'erreur

## 🆘 Dépannage

### Les conteneurs ne démarrent pas
```bash
docker-compose logs -f [service-name]
```

### Erreur de connexion à la base de données
- Vérifiez que MySQL est démarré : `docker-compose ps`
- Vérifiez les variables d'environnement dans `.env`

### Build Docker échoue
- Vérifiez que tous les fichiers nécessaires sont présents
- Vérifiez les logs : `docker-compose build --no-cache`

