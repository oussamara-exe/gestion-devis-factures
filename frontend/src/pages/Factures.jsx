import { useEffect, useState } from 'react';
import { factureService, clientService, produitService, devisService } from '../services/api';
import { Search, Trash2, CheckCircle, Receipt, Plus, Edit, Download, X, Eye } from 'lucide-react';

const Factures = () => {
  const [factures, setFactures] = useState([]);
  const [clients, setClients] = useState([]);
  const [produits, setProduits] = useState([]);
  const [devis, setDevis] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [editingFacture, setEditingFacture] = useState(null);
  const [selectedFacture, setSelectedFacture] = useState(null);
  const [formData, setFormData] = useState({
    client: null,
    date: new Date().toISOString().split('T')[0],
    dateEcheance: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    modePaiement: null,
    statut: 'EMISE',
    details: [],
  });
  const [newDetail, setNewDetail] = useState({ produit: null, quantite: 1 });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [facturesRes, clientsRes, produitsRes, devisRes] = await Promise.all([
        factureService.getAll(),
        clientService.getAll(),
        produitService.getAll(),
        devisService.getAll(),
      ]);
      setFactures(facturesRes.data);
      setClients(clientsRes.data);
      setProduits(produitsRes.data);
      setDevis(devisRes.data.filter(d => d.statut === 'VALIDE' || d.statut === 'ACCEPTE'));
    } catch (error) {
      console.error('Erreur lors du chargement:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.client) {
      alert('Veuillez sélectionner un client');
      return;
    }
    
    if (!formData.details || formData.details.length === 0) {
      alert('Veuillez ajouter au moins un produit à la facture');
      return;
    }
    
    try {
      const data = {
        client: { id: parseInt(formData.client) },
        date: formData.date,
        dateEcheance: formData.dateEcheance,
        modePaiement: formData.modePaiement,
        statut: formData.statut,
        details: formData.details.map(d => {
          const produitId = typeof d.produit === 'object' ? d.produit?.id : d.produit;
          return {
            produit: { id: parseInt(produitId) },
            quantite: parseInt(d.quantite) || 1,
          };
        }),
      };
      
      if (editingFacture) {
        await factureService.update(editingFacture.id, data);
      } else {
        await factureService.create(data);
      }
      setShowModal(false);
      setEditingFacture(null);
      setFormData({ client: null, date: new Date().toISOString().split('T')[0], dateEcheance: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], modePaiement: null, statut: 'EMISE', details: [] });
      setNewDetail({ produit: null, quantite: 1 });
      loadData();
    } catch (error) {
      console.error('Erreur lors de la sauvegarde:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Erreur lors de la sauvegarde de la facture';
      alert(errorMessage);
    }
  };

  const addDetail = (e) => {
    if (e) {
      e.preventDefault();
      e.stopPropagation();
    }
    
    const produitId = newDetail.produit;
    const quantite = parseInt(newDetail.quantite) || 1;
    
    if (!produitId || produitId === '' || produitId === null || produitId === 'null' || produitId === undefined) {
      alert('Veuillez sélectionner un produit');
      return;
    }
    
    if (quantite <= 0 || isNaN(quantite)) {
      alert('La quantité doit être supérieure à 0');
      return;
    }
    
    const produitExistant = (formData.details || []).find(d => {
      const existingId = typeof d.produit === 'object' ? d.produit?.id : d.produit;
      return String(existingId) === String(produitId);
    });
    
    if (produitExistant) {
      alert('Ce produit est déjà ajouté à la facture');
      return;
    }
    
    const currentDetails = formData.details || [];
    const newDetails = [...currentDetails, { 
      produit: produitId, 
      quantite: quantite 
    }];
    
    setFormData(prev => ({
      ...prev,
      details: newDetails,
    }));
    
    setTimeout(() => {
      setNewDetail({ produit: '', quantite: 1 });
    }, 100);
  };

  const removeDetail = (index) => {
    setFormData({
      ...formData,
      details: formData.details.filter((_, i) => i !== index),
    });
  };

  const handleMarquerPayee = async (id) => {
    if (window.confirm('Marquer cette facture comme payée ?')) {
      try {
        await factureService.marquerPayee(id);
        loadData();
      } catch (error) {
        console.error('Erreur lors du paiement:', error);
        alert('Erreur lors du paiement de la facture');
      }
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) {
      try {
        await factureService.delete(id);
        loadData();
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression de la facture');
      }
    }
  };

  const handleDownloadPdf = async (id) => {
    try {
      const response = await factureService.downloadPdf(id);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `facture-${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Erreur lors du téléchargement du PDF:', error);
      alert('Erreur lors du téléchargement du PDF');
    }
  };

  const handleViewDetails = (facture) => {
    setSelectedFacture(facture);
    setShowDetailModal(true);
  };

  const handleEdit = (facture) => {
    setEditingFacture(facture);
    setFormData({
      client: facture.client?.id || null,
      date: facture.date || new Date().toISOString().split('T')[0],
      dateEcheance: facture.dateEcheance || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      modePaiement: facture.modePaiement || null,
      statut: facture.statut || 'EMISE',
      details: facture.details || [],
    });
    setShowModal(true);
  };

  const handleCreateFromDevis = (devis) => {
    setFormData({
      client: devis.client?.id || null,
      date: new Date().toISOString().split('T')[0],
      dateEcheance: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      modePaiement: null,
      statut: 'EMISE',
      details: devis.details?.map(d => ({
        produit: d.produit?.id || d.produit,
        quantite: d.quantite || 1,
      })) || [],
    });
    setShowModal(true);
  };

  const getStatusColor = (statut) => {
    const colors = {
      EMISE: 'bg-gray-100 text-gray-800',
      ENVOYEE: 'bg-blue-100 text-blue-800',
      PAYEE: 'bg-green-100 text-green-800',
      EN_RETARD: 'bg-red-100 text-red-800',
      ANNULEE: 'bg-gray-100 text-gray-800',
    };
    return colors[statut] || 'bg-gray-100 text-gray-800';
  };

  const filteredFactures = factures.filter(f => {
    const client = clients.find(c => c.id === f.client?.id);
    return !searchTerm || 
           f.numeroFacture.toLowerCase().includes(searchTerm.toLowerCase()) ||
           client?.nom.toLowerCase().includes(searchTerm.toLowerCase());
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Factures</h1>
          <p className="text-gray-600">Gérez vos factures</p>
        </div>
        <button
          onClick={() => {
            setEditingFacture(null);
            setFormData({ client: null, date: new Date().toISOString().split('T')[0], dateEcheance: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], modePaiement: null, statut: 'EMISE', details: [] });
            setNewDetail({ produit: null, quantite: 1 });
            setShowModal(true);
          }}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          Nouvelle facture
        </button>
      </div>

      <div className="card">
        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Rechercher une facture..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input-field pl-10"
          />
        </div>

        <div className="grid grid-cols-1 gap-4">
          {filteredFactures.map((facture) => {
            const client = clients.find(c => c.id === facture.client?.id);
            const isRetard = facture.dateEcheance && new Date(facture.dateEcheance) < new Date() && facture.statut !== 'PAYEE';
            return (
              <div key={facture.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <Receipt className="text-primary-600" size={24} />
                      <div>
                        <h3 className="text-lg font-bold">{facture.numeroFacture}</h3>
                        <p className="text-sm text-gray-600">
                          {client?.nom || 'Client inconnu'} • {new Date(facture.date).toLocaleDateString('fr-FR')}
                        </p>
                        {facture.dateEcheance && (
                          <p className={`text-xs mt-1 ${
                            isRetard ? 'text-red-600 font-semibold' : 'text-gray-500'
                          }`}>
                            Échéance: {new Date(facture.dateEcheance).toLocaleDateString('fr-FR')}
                            {isRetard && ' (EN RETARD)'}
                          </p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-4 mt-3 flex-wrap">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(facture.statut)}`}>
                        {facture.statut}
                      </span>
                      <span className="text-lg font-bold text-primary-600">
                        {parseFloat(facture.montantTtc || 0).toFixed(2)} € TTC
                      </span>
                      {facture.modePaiement && (
                        <span className="text-sm text-gray-600">
                          Paiement: {facture.modePaiement}
                        </span>
                      )}
                      {facture.details && facture.details.length > 0 && (
                        <span className="text-sm text-gray-500">
                          {facture.details.length} produit{facture.details.length > 1 ? 's' : ''}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleViewDetails(facture)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Voir les détails"
                    >
                      <Eye size={18} />
                    </button>
                    <button
                      onClick={() => handleDownloadPdf(facture.id)}
                      className="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                      title="Télécharger PDF"
                    >
                      <Download size={18} />
                    </button>
                    {facture.statut !== 'PAYEE' && (
                      <>
                        <button
                          onClick={() => handleEdit(facture)}
                          className="p-2 text-yellow-600 hover:bg-yellow-50 rounded-lg transition-colors"
                          title="Modifier"
                        >
                          <Edit size={18} />
                        </button>
                        <button
                          onClick={() => handleMarquerPayee(facture.id)}
                          className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                          title="Marquer comme payée"
                        >
                          <CheckCircle size={18} />
                        </button>
                        <button
                          onClick={() => handleDelete(facture.id)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Supprimer"
                        >
                          <Trash2 size={18} />
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Modal de création/édition */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-4xl animate-slide-up max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold">
                {editingFacture ? 'Modifier la facture' : 'Nouvelle facture'}
              </h2>
              <button
                onClick={() => setShowModal(false)}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Client *</label>
                  <select
                    required
                    value={formData.client || ''}
                    onChange={(e) => setFormData({ ...formData, client: e.target.value })}
                    className="input-field"
                  >
                    <option value="">Sélectionner un client</option>
                    {clients.map(c => (
                      <option key={c.id} value={c.id}>{c.nom}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Date *</label>
                  <input
                    type="date"
                    required
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="input-field"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Date d'échéance</label>
                  <input
                    type="date"
                    value={formData.dateEcheance}
                    onChange={(e) => setFormData({ ...formData, dateEcheance: e.target.value })}
                    className="input-field"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Mode de paiement</label>
                  <select
                    value={formData.modePaiement || ''}
                    onChange={(e) => setFormData({ ...formData, modePaiement: e.target.value || null })}
                    className="input-field"
                  >
                    <option value="">Sélectionner un mode</option>
                    <option value="ESPECE">Espèces</option>
                    <option value="CHEQUE">Chèque</option>
                    <option value="VIREMENT">Virement</option>
                    <option value="CARTE_BANCAIRE">Carte bancaire</option>
                    <option value="AUTRE">Autre</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Statut</label>
                  <select
                    value={formData.statut}
                    onChange={(e) => setFormData({ ...formData, statut: e.target.value })}
                    className="input-field"
                  >
                    <option value="EMISE">Émise</option>
                    <option value="ENVOYEE">Envoyée</option>
                    <option value="PAYEE">Payée</option>
                    <option value="EN_RETARD">En retard</option>
                    <option value="ANNULEE">Annulée</option>
                  </select>
                </div>
              </div>

              <div className="border-t pt-4">
                <h3 className="font-semibold mb-3">Produits</h3>
                <div className="flex gap-2 mb-4">
                  <select
                    value={newDetail.produit || ''}
                    onChange={(e) => setNewDetail(prev => ({ ...prev, produit: e.target.value }))}
                    className="input-field flex-1"
                  >
                    <option value="">Sélectionner un produit</option>
                    {produits.map(p => (
                      <option key={p.id} value={String(p.id)}>
                        {p.nom} - {parseFloat(p.prixUnitaire || 0).toFixed(2)} € 
                        {p.stock !== undefined && ` (Stock: ${p.stock})`}
                      </option>
                    ))}
                  </select>
                  <input
                    type="number"
                    min="1"
                    value={newDetail.quantite || 1}
                    onChange={(e) => setNewDetail(prev => ({ ...prev, quantite: parseInt(e.target.value) || 1 }))}
                    className="input-field w-24"
                    placeholder="Qté"
                  />
                  <button
                    type="button"
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      addDetail(e);
                    }}
                    disabled={!newDetail.produit || newDetail.produit === '' || newDetail.produit === null}
                    className={`btn-primary ${!newDetail.produit || newDetail.produit === '' || newDetail.produit === null ? 'opacity-50 cursor-not-allowed' : ''}`}
                  >
                    Ajouter
                  </button>
                </div>

                <div className="space-y-2">
                  <div className="text-xs text-gray-400 mb-2">
                    Produits ajoutés: {formData.details?.length || 0}
                  </div>
                  {!formData.details || formData.details.length === 0 ? (
                    <p className="text-sm text-gray-500 italic text-center py-4">
                      Aucun produit ajouté. Sélectionnez un produit ci-dessus et cliquez sur "Ajouter".
                    </p>
                  ) : (
                    formData.details.map((detail, index) => {
                      const produitId = typeof detail.produit === 'object' ? detail.produit?.id : detail.produit;
                      const produit = produits.find(p => String(p.id) === String(produitId));
                      const quantite = parseInt(detail.quantite) || 0;
                      const prixUnitaire = produit ? parseFloat(produit.prixUnitaire || 0) : 0;
                      const montantHt = quantite * prixUnitaire;
                      const tauxTva = produit ? parseFloat(produit.tauxTva || 20) : 20;
                      const montantTva = montantHt * (tauxTva / 100);
                      const montantTtc = montantHt + montantTva;
                      
                      return (
                        <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <span className="font-medium">{produit?.nom || `Produit ID: ${produitId}`}</span>
                              {produit && produit.stock !== undefined && (
                                <span className={`text-xs px-2 py-0.5 rounded ${
                                  produit.stock > 10 ? 'bg-green-100 text-green-800' :
                                  produit.stock > 0 ? 'bg-yellow-100 text-yellow-800' :
                                  'bg-red-100 text-red-800'
                                }`}>
                                  Stock: {produit.stock}
                                </span>
                              )}
                            </div>
                            <div className="text-sm text-gray-600 space-y-1">
                              <div>
                                {quantite} x {prixUnitaire.toFixed(2)} € = <strong>{montantHt.toFixed(2)} € HT</strong>
                              </div>
                              <div className="text-xs text-gray-500">
                                TVA ({tauxTva}%): {montantTva.toFixed(2)} € | <strong>TTC: {montantTtc.toFixed(2)} €</strong>
                              </div>
                            </div>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeDetail(index)}
                            className="p-1 text-red-600 hover:bg-red-50 rounded ml-2"
                          >
                            <X size={16} />
                          </button>
                        </div>
                      );
                    })
                  )}
                </div>
                
                {/* Récapitulatif des totaux */}
                {formData.details && formData.details.length > 0 && (() => {
                  const totalHt = formData.details.reduce((sum, detail) => {
                    const produitId = typeof detail.produit === 'object' ? detail.produit?.id : detail.produit;
                    const produit = produits.find(p => String(p.id) === String(produitId));
                    const quantite = parseInt(detail.quantite) || 0;
                    const prixUnitaire = produit ? parseFloat(produit.prixUnitaire || 0) : 0;
                    return sum + (quantite * prixUnitaire);
                  }, 0);
                  
                  const totalTva = formData.details.reduce((sum, detail) => {
                    const produitId = typeof detail.produit === 'object' ? detail.produit?.id : detail.produit;
                    const produit = produits.find(p => String(p.id) === String(produitId));
                    const quantite = parseInt(detail.quantite) || 0;
                    const prixUnitaire = produit ? parseFloat(produit.prixUnitaire || 0) : 0;
                    const tauxTva = produit ? parseFloat(produit.tauxTva || 20) : 20;
                    const montantHt = quantite * prixUnitaire;
                    return sum + (montantHt * (tauxTva / 100));
                  }, 0);
                  
                  const totalTtc = totalHt + totalTva;
                  
                  return (
                    <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
                      <h4 className="font-semibold mb-2 text-gray-700">Récapitulatif</h4>
                      <div className="space-y-1 text-sm">
                        <div className="flex justify-between">
                          <span className="text-gray-600">Total HT:</span>
                          <span className="font-medium">{totalHt.toFixed(2)} €</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Total TVA:</span>
                          <span className="font-medium">{totalTva.toFixed(2)} €</span>
                        </div>
                        <div className="flex justify-between pt-2 border-t border-blue-300">
                          <span className="font-bold text-gray-800">Total TTC:</span>
                          <span className="font-bold text-lg text-primary-600">{totalTtc.toFixed(2)} €</span>
                        </div>
                      </div>
                    </div>
                  );
                })()}
              </div>

              <div className="flex gap-3 pt-4 border-t">
                <button type="submit" className="btn-primary flex-1">
                  {editingFacture ? 'Modifier' : 'Créer'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn-secondary flex-1"
                >
                  Annuler
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de détails */}
      {showDetailModal && selectedFacture && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-4xl animate-slide-up max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold">Détails de la facture</h2>
              <button
                onClick={() => setShowDetailModal(false)}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <X size={20} />
              </button>
            </div>
            
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-700">Numéro</label>
                  <p className="text-lg font-bold">{selectedFacture.numeroFacture}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700">Date</label>
                  <p>{new Date(selectedFacture.date).toLocaleDateString('fr-FR')}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700">Client</label>
                  <p className="font-semibold">{clients.find(c => c.id === selectedFacture.client?.id)?.nom || 'Client inconnu'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700">Statut</label>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedFacture.statut)}`}>
                    {selectedFacture.statut}
                  </span>
                </div>
                {selectedFacture.dateEcheance && (
                  <div>
                    <label className="text-sm font-medium text-gray-700">Date d'échéance</label>
                    <p>{new Date(selectedFacture.dateEcheance).toLocaleDateString('fr-FR')}</p>
                  </div>
                )}
                {selectedFacture.modePaiement && (
                  <div>
                    <label className="text-sm font-medium text-gray-700">Mode de paiement</label>
                    <p>{selectedFacture.modePaiement}</p>
                  </div>
                )}
              </div>

              <div className="border-t pt-4">
                <h3 className="font-semibold mb-3">Produits</h3>
                <div className="space-y-2">
                  {selectedFacture.details && selectedFacture.details.length > 0 ? (
                    selectedFacture.details.map((detail, index) => {
                      const produit = detail.produit;
                      return (
                        <div key={index} className="p-3 bg-gray-50 rounded-lg">
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <div className="font-medium mb-1">{produit?.nom || 'Produit inconnu'}</div>
                              <div className="text-sm text-gray-600 space-y-1">
                                <div>
                                  Quantité: {detail.quantite} x {parseFloat(detail.prixUnitaire || 0).toFixed(2)} €
                                </div>
                                <div>
                                  TVA: {parseFloat(detail.tauxTva || 0).toFixed(2)}%
                                </div>
                                <div className="flex gap-4 mt-2">
                                  <span>HT: <strong>{parseFloat(detail.montantHt || 0).toFixed(2)} €</strong></span>
                                  <span>TVA: <strong>{parseFloat(detail.montantTva || 0).toFixed(2)} €</strong></span>
                                  <span>TTC: <strong className="text-primary-600">{parseFloat(detail.montantTtc || 0).toFixed(2)} €</strong></span>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <p className="text-sm text-gray-500 italic text-center py-4">Aucun produit</p>
                  )}
                </div>
              </div>

              <div className="border-t pt-4">
                <div className="flex justify-end space-x-4 text-right">
                  <div>
                    <label className="text-sm text-gray-600">Total HT</label>
                    <p className="text-lg font-semibold">{parseFloat(selectedFacture.montantHt || 0).toFixed(2)} €</p>
                  </div>
                  <div>
                    <label className="text-sm text-gray-600">Total TVA</label>
                    <p className="text-lg font-semibold">{parseFloat(selectedFacture.montantTva || 0).toFixed(2)} €</p>
                  </div>
                  <div>
                    <label className="text-sm text-gray-600">Total TTC</label>
                    <p className="text-2xl font-bold text-primary-600">{parseFloat(selectedFacture.montantTtc || 0).toFixed(2)} €</p>
                  </div>
                </div>
              </div>

              <div className="flex gap-3 pt-4 border-t">
                <button
                  onClick={() => handleDownloadPdf(selectedFacture.id)}
                  className="btn-primary flex items-center gap-2"
                >
                  <Download size={18} />
                  Télécharger PDF
                </button>
                {selectedFacture.statut !== 'PAYEE' && (
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      handleEdit(selectedFacture);
                    }}
                    className="btn-secondary flex items-center gap-2"
                  >
                    <Edit size={18} />
                    Modifier
                  </button>
                )}
                <button
                  onClick={() => setShowDetailModal(false)}
                  className="btn-secondary"
                >
                  Fermer
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Factures;
