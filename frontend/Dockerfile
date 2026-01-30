# Étape 1 : Build - Construction de l'application React avec Vite
FROM node:20-alpine AS builder

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de dépendances (package.json et package-lock.json)
# Cette étape permet de mettre en cache les dépendances si ces fichiers ne changent pas
COPY package*.json ./

# Installer les dépendances
# Utilisation de npm ci pour une installation plus rapide et fiable en production
RUN npm ci --only=production=false

# Copier le reste des fichiers de l'application
COPY . .

# Construire l'application pour la production
# Vite génère les fichiers optimisés dans le dossier dist/
# VITE_API_URL est passée comme argument de build pour être disponible au moment du build
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Étape 2 : Production - Servir l'application avec Nginx
FROM nginx:alpine

# Copier les fichiers buildés depuis l'étape builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copier la configuration Nginx personnalisée (optionnel)
# Si vous avez besoin de configurer Nginx (SPA routing, proxy API, etc.)
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Créer un fichier de configuration Nginx simple pour React Router (SPA)
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    # Cache pour les assets statiques \
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ { \
        expires 1y; \
        add_header Cache-Control "public, immutable"; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Exposer le port 80
EXPOSE 80

# Démarrage de Nginx
CMD ["nginx", "-g", "daemon off;"]

