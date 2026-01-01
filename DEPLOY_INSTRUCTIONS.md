# 🚀 Instructions de Déploiement

## Option A : Déploiement Séparé (Recommandé - Plus Simple)

### 1️⃣ Frontend sur Vercel

#### Étape 1 : Créer un compte Vercel
1. Allez sur https://vercel.com
2. Créez un compte ou connectez-vous avec GitHub

#### Étape 2 : Importer le projet
1. Cliquez sur **"New Project"**
2. Sélectionnez votre repository GitHub
3. Dans les paramètres du projet :
   - **Framework Preset** : Vite
   - **Root Directory** : `frontend`
   - **Build Command** : `npm run build`
   - **Output Directory** : `dist`
   - **Install Command** : `npm install`

#### Étape 3 : Configurer les variables d'environnement
Dans **Settings → Environment Variables**, ajoutez :
- `VITE_API_URL` : URL de votre backend (à définir après le déploiement du backend)
  - Exemple : `https://gestion-devis-factures-backend.onrender.com`

#### Étape 4 : Déployer
1. Cliquez sur **"Deploy"**
2. Attendez la fin du déploiement
3. Notez l'URL de votre frontend (ex: `https://votre-app.vercel.app`)

---

### 2️⃣ Backend sur Render.com

#### Étape 1 : Créer un compte Render
1. Allez sur https://render.com
2. Créez un compte ou connectez-vous avec GitHub

#### Étape 2 : Créer la base de données MySQL
1. Cliquez sur **"New +"** → **"PostgreSQL"** (ou MySQL si disponible)
2. Configurez :
   - **Name** : `gestion-devis-factures-db`
   - **Database** : `gestion_devis_factures`
   - **User** : `appuser`
   - **Region** : Choisissez le plus proche (ex: Frankfurt)
3. Cliquez sur **"Create Database"**
4. **Notez les informations de connexion** (Internal Database URL)

#### Étape 3 : Créer le Web Service (Backend)
1. Cliquez sur **"New +"** → **"Web Service"**
2. Connectez votre repository GitHub
3. Configurez :
   - **Name** : `gestion-devis-factures-backend`
   - **Environment** : Docker
   - **Region** : Même que la base de données
   - **Branch** : `main` (ou votre branche principale)
   - **Root Directory** : `/` (racine du projet)
   - **Dockerfile Path** : `Dockerfile`
   - **Docker Context** : `.`

#### Étape 4 : Variables d'environnement
Dans **Environment**, ajoutez :

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:3306/gestion_devis_factures?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=appuser
SPRING_DATASOURCE_PASSWORD=[PASSWORD_FROM_DATABASE]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://[VOTRE_FRONTEND].vercel.app
SERVER_PORT=8080
```

**Important** : Remplacez `[HOST]`, `[PASSWORD_FROM_DATABASE]` et `[VOTRE_FRONTEND]` par vos vraies valeurs.

#### Étape 5 : Déployer
1. Cliquez sur **"Create Web Service"**
2. Le build démarre automatiquement
3. Attendez la fin du déploiement
4. Notez l'URL de votre backend (ex: `https://gestion-devis-factures-backend.onrender.com`)

#### Étape 6 : Mettre à jour le frontend
1. Retournez sur Vercel
2. Mettez à jour la variable `VITE_API_URL` avec l'URL de votre backend Render
3. Redéployez le frontend (ou attendez le redéploiement automatique)

---

## Option B : Déploiement via Railway.app (Tout en un)

### Étape 1 : Créer un compte Railway
1. Allez sur https://railway.app
2. Connectez-vous avec GitHub

### Étape 2 : Créer un nouveau projet
1. Cliquez sur **"New Project"**
2. Sélectionnez **"Deploy from GitHub repo"**
3. Choisissez votre repository

### Étape 3 : Ajouter les services

#### Service 1 : Base de données MySQL
1. Cliquez sur **"+ New"** → **"Database"** → **"Add MySQL"**
2. Railway crée automatiquement la base de données
3. Notez les variables d'environnement générées

#### Service 2 : Backend
1. Cliquez sur **"+ New"** → **"GitHub Repo"**
2. Sélectionnez votre repository
3. Railway détecte automatiquement le Dockerfile
4. Dans **Variables**, ajoutez :
   ```
   SPRING_DATASOURCE_URL=${{MySQL.DATABASE_URL}}
   SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
   SPRING_WEB_CORS_ALLOWED_ORIGINS=https://[VOTRE_FRONTEND].vercel.app
   ```

#### Service 3 : Frontend (optionnel sur Railway)
Ou continuez avec Vercel pour le frontend comme dans l'Option A.

---

## 🔐 Configuration des Secrets GitHub pour CI/CD

Pour activer le pipeline CI/CD automatique :

1. Allez sur votre repository GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Ajoutez ces secrets :
   - `DOCKER_USERNAME` : Votre nom d'utilisateur Docker Hub
   - `DOCKER_PASSWORD` : Token Docker Hub (créez-le sur hub.docker.com)
   - `VITE_API_URL` : URL de votre backend en production (optionnel)

### Obtenir un token Docker Hub :
1. Connectez-vous sur https://hub.docker.com
2. **Account Settings** → **Security** → **New Access Token**
3. Donnez un nom et copiez le token
4. Ajoutez-le comme secret `DOCKER_PASSWORD` dans GitHub

---

## ✅ Checklist de Déploiement

- [ ] Repository GitHub créé et code poussé
- [ ] Frontend déployé sur Vercel
- [ ] Base de données créée (Render ou Railway)
- [ ] Backend déployé (Render ou Railway)
- [ ] Variables d'environnement configurées
- [ ] CORS configuré avec l'URL du frontend
- [ ] Frontend mis à jour avec l'URL du backend
- [ ] Tests de connectivité effectués
- [ ] Secrets GitHub configurés (pour CI/CD)

---

## 🧪 Tests Post-Déploiement

### Tester le Backend
```bash
curl https://votre-backend.onrender.com/api/clients
# Devrait retourner : []
```

### Tester le Frontend
1. Ouvrez https://votre-frontend.vercel.app
2. Vérifiez que l'application se charge
3. Testez une fonctionnalité (création de client, etc.)

### Vérifier les logs
- **Vercel** : Onglet "Logs" dans le dashboard
- **Render** : Onglet "Logs" dans le service
- **Railway** : Onglet "Deployments" → Voir les logs

---

## 🆘 Dépannage

### Backend ne démarre pas
- Vérifiez les variables d'environnement
- Vérifiez les logs dans le dashboard
- Assurez-vous que la base de données est accessible

### Erreur CORS
- Vérifiez que `SPRING_WEB_CORS_ALLOWED_ORIGINS` contient l'URL exacte du frontend
- N'oubliez pas le `https://` et pas de slash final

### Frontend ne peut pas contacter le backend
- Vérifiez que `VITE_API_URL` est bien configuré dans Vercel
- Vérifiez que le backend est accessible publiquement
- Vérifiez les logs du navigateur (Console développeur)

---

**Bon déploiement ! 🚀**

