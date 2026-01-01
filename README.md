# Application de Gestion des Devis et Factures

Application web complète pour gérer les devis, factures, clients et produits d'une entreprise.

## Technologies

### Backend
- **Spring Boot 3.2.0** - Framework Java
- **Spring Data JPA** - Persistance des données
- **MySQL** - Base de données
- **Maven** - Gestion des dépendances
- **Lombok** - Réduction du code boilerplate

### Frontend
- **React 18** - Bibliothèque JavaScript
- **Vite** - Build tool moderne
- **Tailwind CSS** - Framework CSS utilitaire
- **React Router** - Navigation
- **Axios** - Client HTTP
- **Lucide React** - Icônes modernes

## Structure du Projet

```
projet_springboot_react/
├── src/                          # Backend Spring Boot
│   └── main/
│       ├── java/com/entreprise/gestion/
│       │   ├── model/            # Entités JPA
│       │   ├── repository/       # Repositories JPA
│       │   ├── service/          # Services métier
│       │   ├── controller/       # Controllers REST
│       │   └── config/           # Configuration
│       └── resources/
│           └── application.properties
├── frontend/                     # Frontend React
│   ├── src/
│   │   ├── components/           # Composants React
│   │   ├── pages/                # Pages de l'application
│   │   ├── services/             # Services API
│   │   └── App.jsx
│   └── package.json
└── pom.xml                       # Configuration Maven
```

## Installation

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+
- Node.js 18+ et npm
- MySQL 8.0+

### Backend

1. Créer la base de données MySQL :
```sql
CREATE DATABASE gestion_devis_factures;
```

2. Configurer `src/main/resources/application.properties` avec vos identifiants MySQL :
```properties
spring.datasource.username=votre_username
spring.datasource.password=votre_password
```

3. Compiler et lancer l'application :
```bash
mvn clean install
mvn spring-boot:run
```

Le backend sera accessible sur `http://localhost:8080`

### Frontend

1. Installer les dépendances :
```bash
cd frontend
npm install
```

2. Lancer le serveur de développement :
```bash
npm run dev
```

Le frontend sera accessible sur `http://localhost:5173`

## Fonctionnalités

### Clients
- ✅ Création, modification, suppression de clients
- ✅ Recherche de clients
- ✅ Gestion des informations complètes (nom, email, téléphone, adresse)

### Produits
- ✅ Gestion du catalogue de produits
- ✅ Suivi des stocks
- ✅ Gestion des catégories et taux de TVA
- ✅ Recherche de produits

### Devis
- ✅ Création de devis avec plusieurs produits
- ✅ Génération automatique du numéro de devis
- ✅ Calcul automatique des totaux (HT, TVA, TTC)
- ✅ Validation des devis
- ✅ Conversion d'un devis en facture
- ✅ Gestion des statuts (Brouillon, Validé, Envoyé, Accepté, Refusé, Annulé)

### Factures
- ✅ Création de factures (directe ou depuis un devis)
- ✅ Génération automatique du numéro de facture
- ✅ Calcul automatique des totaux
- ✅ Suivi du paiement
- ✅ Gestion des échéances
- ✅ Mise à jour automatique des stocks lors de la création

### Statistiques
- ✅ Chiffre d'affaires global
- ✅ Chiffre d'affaires par période
- ✅ Dashboard avec vue d'ensemble

## API REST

### Clients
- `GET /api/clients` - Liste des clients
- `GET /api/clients/{id}` - Détail d'un client
- `POST /api/clients` - Créer un client
- `PUT /api/clients/{id}` - Modifier un client
- `DELETE /api/clients/{id}` - Supprimer un client
- `GET /api/clients/search?search=...` - Rechercher des clients

### Produits
- `GET /api/produits` - Liste des produits
- `GET /api/produits/{id}` - Détail d'un produit
- `POST /api/produits` - Créer un produit
- `PUT /api/produits/{id}` - Modifier un produit
- `DELETE /api/produits/{id}` - Supprimer un produit
- `GET /api/produits/search?search=...` - Rechercher des produits
- `GET /api/produits/stock` - Produits en stock

### Devis
- `GET /api/devis` - Liste des devis
- `GET /api/devis/{id}` - Détail d'un devis
- `POST /api/devis` - Créer un devis
- `PUT /api/devis/{id}` - Modifier un devis
- `DELETE /api/devis/{id}` - Supprimer un devis
- `PUT /api/devis/{id}/valider` - Valider un devis
- `POST /api/devis/{id}/convertir-facture` - Convertir en facture

### Factures
- `GET /api/factures` - Liste des factures
- `GET /api/factures/{id}` - Détail d'une facture
- `POST /api/factures` - Créer une facture
- `PUT /api/factures/{id}` - Modifier une facture
- `DELETE /api/factures/{id}` - Supprimer une facture
- `PUT /api/factures/{id}/payer` - Marquer comme payée
- `GET /api/factures/client/{clientId}` - Factures d'un client

### Statistiques
- `GET /api/statistiques/ca` - Chiffre d'affaires global
- `GET /api/statistiques/ca/period?dateDebut=...&dateFin=...` - CA par période

## Design

L'interface utilisateur est moderne et professionnelle avec :
- Design responsive et adaptatif
- Animations fluides
- Palette de couleurs cohérente
- Navigation intuitive avec sidebar
- Modales pour les formulaires
- Feedback visuel pour les actions

## Notes

- Les numéros de devis suivent le format `DEV-00001`
- Les numéros de factures suivent le format `FAC-00001`
- Le taux de TVA par défaut est de 20%
- Les stocks sont automatiquement déduits lors de la création d'une facture
- Les calculs (HT, TVA, TTC) sont effectués automatiquement

## Développement

Pour le développement, les deux serveurs doivent être lancés simultanément :
- Backend Spring Boot sur le port 8080
- Frontend React sur le port 5173

Le CORS est configuré pour permettre la communication entre les deux applications.

