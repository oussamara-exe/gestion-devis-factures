import axios from 'axios';

// Utilisation de la variable d'environnement Vite avec valeur par défaut
// En développement: http://localhost:8080/api
// En production: défini via VITE_API_URL (doit inclure /api)
const API_BASE_URL = import.meta.env.VITE_API_URL 
  ? `${import.meta.env.VITE_API_URL}/api`
  : 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Clients
export const clientService = {
  getAll: () => api.get('/clients'),
  getById: (id) => api.get(`/clients/${id}`),
  create: (data) => api.post('/clients', data),
  update: (id, data) => api.put(`/clients/${id}`, data),
  delete: (id) => api.delete(`/clients/${id}`),
  search: (search) => api.get(`/clients/search?search=${search}`),
};

// Produits
export const produitService = {
  getAll: () => api.get('/produits'),
  getById: (id) => api.get(`/produits/${id}`),
  create: (data) => api.post('/produits', data),
  update: (id, data) => api.put(`/produits/${id}`, data),
  delete: (id) => api.delete(`/produits/${id}`),
  search: (search) => api.get(`/produits/search?search=${search}`),
  getByCategorie: (categorie) => api.get(`/produits/categorie/${categorie}`),
  getEnStock: () => api.get('/produits/stock'),
};

// Devis
export const devisService = {
  getAll: () => api.get('/devis'),
  getById: (id) => api.get(`/devis/${id}`),
  create: (data) => api.post('/devis', data),
  update: (id, data) => api.put(`/devis/${id}`, data),
  delete: (id) => api.delete(`/devis/${id}`),
  valider: (id) => api.put(`/devis/${id}/valider`),
  convertirEnFacture: (id) => api.post(`/devis/${id}/convertir-facture`),
  downloadPdf: (id) => api.get(`/devis/${id}/pdf`, { responseType: 'blob' }),
  getByClient: (clientId) => api.get(`/devis/client/${clientId}`),
};

// Factures
export const factureService = {
  getAll: () => api.get('/factures'),
  getById: (id) => api.get(`/factures/${id}`),
  create: (data) => api.post('/factures', data),
  update: (id, data) => api.put(`/factures/${id}`, data),
  delete: (id) => api.delete(`/factures/${id}`),
  marquerPayee: (id) => api.put(`/factures/${id}/payer`),
  getByClient: (clientId) => api.get(`/factures/client/${clientId}`),
  downloadPdf: (id) => api.get(`/factures/${id}/pdf`, { responseType: 'blob' }),
};

// Statistiques
export const statistiquesService = {
  getChiffreAffaires: () => api.get('/statistiques/ca'),
  getChiffreAffairesByPeriod: (dateDebut, dateFin) => 
    api.get(`/statistiques/ca/period?dateDebut=${dateDebut}&dateFin=${dateFin}`),
  getDashboardStats: () => api.get('/statistiques/dashboard'),
};

export default api;

