# 🚀 Déploiement Rapide - Guide Express

## 📋 Prérequis
- Compte GitHub
- Compte Vercel (gratuit)
- Compte Render.com (gratuit) ou Railway.app (gratuit)

---

## 🎯 Déploiement en 5 étapes

### Étape 1 : Pousser sur GitHub

```bash
# Si pas encore fait
git init
git add .
git commit -m "Setup DevOps complet"

# Créer un nouveau repository sur https://github.com/new
# Puis :
git remote add origin https://github.com/VOTRE_USERNAME/VOTRE_REPO.git
git branch -M main
git push -u origin main
```

### Étape 2 : Déployer le Frontend sur Vercel (2 minutes)

1. **Allez sur https://vercel.com** et connectez-vous avec GitHub
2. **Cliquez sur "New Project"**
3. **Sélectionnez votre repository**
4. **Configurez** :
   - **Framework Preset** : Vite
   - **Root Directory** : `frontend`
   - **Build Command** : `npm run build`
   - **Output Directory** : `dist`
5. **Variables d'environnement** (à faire après le backend) :
   - Ajoutez `VITE_API_URL` = URL de votre backend
6. **Cliquez sur "Deploy"**
7. **Notez l'URL** du frontend (ex: `https://votre-app.vercel.app`)

### Étape 3 : Déployer le Backend sur Render.com (5 minutes)

#### 3.1 Créer la base de données
1. **Allez sur https://render.com** et connectez-vous avec GitHub
2. **New +** → **PostgreSQL** (ou MySQL si disponible)
3. Configurez :
   - **Name** : `gestion-devis-factures-db`
   - **Database** : `gestion_devis_factures`
   - **User** : `appuser`
   - **Region** : Frankfurt (ou plus proche)
4. **Create Database**
5. **Notez** l'URL de connexion interne

#### 3.2 Créer le Web Service Backend
1. **New +** → **Web Service**
2. **Connectez votre repository GitHub**
3. Configurez :
   - **Name** : `gestion-devis-factures-backend`
   - **Environment** : Docker
   - **Region** : Même que la DB
   - **Branch** : `main`
   - **Dockerfile Path** : `Dockerfile`
4. **Variables d'environnement** :
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://[HOST]:3306/gestion_devis_factures?createDatabaseIfNotExist=true&useSSL=true
SPRING_DATASOURCE_USERNAME=appuser
SPRING_DATASOURCE_PASSWORD=[PASSWORD_DE_LA_DB]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_WEB_CORS_ALLOWED_ORIGINS=https://[VOTRE_FRONTEND].vercel.app
SERVER_PORT=8080
```
5. **Create Web Service**
6. **Attendez** le déploiement (5-10 minutes)
7. **Notez l'URL** du backend (ex: `https://gestion-devis-factures-backend.onrender.com`)

### Étape 4 : Mettre à jour le Frontend

1. **Retournez sur Vercel**
2. **Settings** → **Environment Variables**
3. **Mettez à jour** `VITE_API_URL` avec l'URL de votre backend Render
4. **Redeploy** (automatique ou manuel)

### Étape 5 : Tester

1. **Ouvrez** https://votre-frontend.vercel.app
2. **Vérifiez** que l'application se charge
3. **Testez** une fonctionnalité (créer un client, etc.)

---

## ✅ Vérification

- [ ] Code poussé sur GitHub
- [ ] Frontend déployé sur Vercel
- [ ] Base de données créée sur Render
- [ ] Backend déployé sur Render
- [ ] Variables d'environnement configurées
- [ ] CORS configuré avec l'URL du frontend
- [ ] Frontend mis à jour avec l'URL du backend
- [ ] Application testée et fonctionnelle

---

## 🆘 Problèmes Courants

### Backend ne démarre pas
- Vérifiez les logs dans Render
- Vérifiez que la base de données est accessible
- Vérifiez les variables d'environnement

### Erreur CORS
- Assurez-vous que `SPRING_WEB_CORS_ALLOWED_ORIGINS` contient exactement l'URL du frontend
- Pas de slash final dans l'URL

### Frontend ne peut pas contacter le backend
- Vérifiez que `VITE_API_URL` est bien configuré dans Vercel
- Vérifiez que le backend est accessible publiquement

---

**Temps estimé total : 15-20 minutes** ⏱️

