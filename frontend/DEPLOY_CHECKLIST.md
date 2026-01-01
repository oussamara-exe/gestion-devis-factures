# ✅ Checklist de Déploiement

## Phase 1 : Préparation
- [x] ✅ Repository Git initialisé
- [x] ✅ Fichiers de configuration créés
- [x] ✅ Documentation complète générée
- [x] ✅ Tests locaux passés (Docker)

## Phase 2 : GitHub
- [ ] Créer un repository sur GitHub
- [ ] Pousser le code : `git push -u origin main`
- [ ] Configurer les secrets GitHub Actions :
  - [ ] `DOCKER_USERNAME`
  - [ ] `DOCKER_PASSWORD`

## Phase 3 : Frontend (Vercel)
- [ ] Créer un compte Vercel
- [ ] Connecter le repository GitHub
- [ ] Configurer le projet :
  - [ ] Root Directory : `frontend`
  - [ ] Framework : Vite
  - [ ] Build Command : `npm run build`
  - [ ] Output Directory : `dist`
- [ ] Déployer et noter l'URL

## Phase 4 : Backend (Render.com)
- [ ] Créer un compte Render.com
- [ ] Créer la base de données MySQL
- [ ] Créer le Web Service (Docker)
- [ ] Configurer les variables d'environnement
- [ ] Noter l'URL du backend

## Phase 5 : Configuration Finale
- [ ] Mettre à jour `VITE_API_URL` dans Vercel
- [ ] Mettre à jour `SPRING_WEB_CORS_ALLOWED_ORIGINS` dans Render
- [ ] Redéployer les services
- [ ] Tester l'application complète

## Phase 6 : Validation
- [ ] Frontend accessible publiquement
- [ ] Backend API répond correctement
- [ ] Base de données fonctionnelle
- [ ] Pas d'erreurs CORS
- [ ] Application fonctionnelle end-to-end

---
**Temps estimé : 15-20 minutes**
