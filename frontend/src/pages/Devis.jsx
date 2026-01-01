import { useEffect, useState } from 'react';
import { devisService, clientService, produitService } from '../services/api';
import { Plus, Search, Edit, Trash2, Check, FileText, X, Download } from 'lucide-react';

const Devis = () => {
  const [devis, setDevis] = useState([]);
  const [clients, setClients] = useState([]);
  const [produits, setProduits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingDevis, setEditingDevis] = useState(null);
  const [formData, setFormData] = useState({
    client: null,
    date: new Date().toISOString().split('T')[0],
    details: [],
  });
  const [newDetail, setNewDetail] = useState({ produit: null, quantite: 1 });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [devisRes, clientsRes, produitsRes] = await Promise.all([
        devisService.getAll(),
        clientService.getAll(),
        produitService.getAll(),
      ]);
      setDevis(devisRes.data);
      setClients(clientsRes.data);
      setProduits(produitsRes.data);
    } catch (error) {
      console.error('Erreur lors du chargement:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    console.log('handleSubmit appelé, formData:', formData);
    console.log('formData.details:', formData.details);
    console.log('formData.details.length:', formData.details.length);
    
    if (!formData.client) {
      alert('Veuillez sélectionner un client');
      return;
    }
    
    if (!formData.details || formData.details.length === 0) {
      alert('Veuillez ajouter au moins un produit au devis');
      return;
    }
    
    // Vérifier que tous les détails ont un produit valide et vérifier le stock
    const detailsValides = formData.details.filter(d => {
      const produitId = typeof d.produit === 'object' ? d.produit?.id : d.produit;
      if (!produitId || produitId === '' || produitId === null || produitId === 'null') {
        return false;
      }
      
      // Vérifier le stock
      const produit = produits.find(p => String(p.id) === String(produitId));
      const quantite = parseInt(d.quantite) || 0;
      if (produit && produit.stock !== undefined && quantite > produit.stock) {
        alert(`Stock insuffisant pour ${produit.nom}. Stock disponible: ${produit.stock}, Quantité demandée: ${quantite}`);
        return false;
      }
      
      return true;
    });
    
    if (detailsValides.length === 0) {
      alert('Veuillez ajouter au moins un produit valide au devis');
      return;
    }
    
    try {
      const data = {
        client: { id: parseInt(formData.client) },
        date: formData.date,
        details: detailsValides.map(d => {
          const produitId = typeof d.produit === 'object' ? d.produit?.id : d.produit;
          // Le backend récupère automatiquement le prix et le taux TVA depuis le produit
          return {
            produit: { id: parseInt(produitId) },
            quantite: parseInt(d.quantite) || 1,
          };
        }),
      };
      
      console.log('Données à envoyer:', JSON.stringify(data, null, 2));
      
      if (editingDevis) {
        await devisService.update(editingDevis.id, data);
      } else {
        await devisService.create(data);
      }
      setShowModal(false);
      setEditingDevis(null);
      setFormData({ client: null, date: new Date().toISOString().split('T')[0], details: [] });
      setNewDetail({ produit: null, quantite: 1 });
      loadData();
    } catch (error) {
      console.error('Erreur lors de la sauvegarde:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Erreur lors de la sauvegarde du devis';
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
    
    console.log('=== addDetail appelé ===');
    console.log('produitId:', produitId, 'type:', typeof produitId);
    console.log('quantite:', quantite);
    console.log('newDetail:', newDetail);
    console.log('formData actuel:', formData);
    console.log('formData.details actuel:', formData.details);
    
    if (!produitId || produitId === '' || produitId === null || produitId === 'null' || produitId === undefined) {
      alert('Veuillez sélectionner un produit');
      return;
    }
    
    if (quantite <= 0 || isNaN(quantite)) {
      alert('La quantité doit être supérieure à 0');
      return;
    }
    
    // Vérifier que le produit n'est pas déjà dans la liste
    const produitExistant = (formData.details || []).find(d => {
      const existingId = typeof d.produit === 'object' ? d.produit?.id : d.produit;
      return String(existingId) === String(produitId);
    });
    
    if (produitExistant) {
      alert('Ce produit est déjà ajouté au devis');
      return;
    }
    
    const currentDetails = formData.details || [];
    const newDetails = [...currentDetails, { 
      produit: produitId, 
      quantite: quantite 
    }];
    
    console.log('Nouveaux détails à ajouter:', newDetails);
    console.log('Nombre de détails:', newDetails.length);
    
    setFormData(prev => {
      const updated = {
        ...prev,
        details: newDetails,
      };
      console.log('=== formData mis à jour ===');
      console.log('updated.details:', updated.details);
      console.log('updated.details.length:', updated.details.length);
      return updated;
    });
    
    // Réinitialiser le select manuellement
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

  const handleValider = async (id) => {
    try {
      await devisService.valider(id);
      loadData();
      alert('Devis validé avec succès !');
    } catch (error) {
      console.error('Erreur lors de la validation:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Erreur lors de la validation du devis';
      alert(errorMessage);
    }
  };

  const handleConvertir = async (id) => {
    if (window.confirm('Voulez-vous convertir ce devis en facture ?')) {
      try {
        await devisService.convertirEnFacture(id);
        alert('Devis converti en facture avec succès !');
        loadData();
      } catch (error) {
        console.error('Erreur lors de la conversion:', error);
        alert('Erreur lors de la conversion du devis');
      }
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce devis ?')) {
      try {
        await devisService.delete(id);
        loadData();
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression du devis');
      }
    }
  };

  const getStatusColor = (statut) => {
    const colors = {
      BROUILLON: 'bg-gray-100 text-gray-800',
      VALIDE: 'bg-green-100 text-green-800',
      ENVOYE: 'bg-blue-100 text-blue-800',
      ACCEPTE: 'bg-purple-100 text-purple-800',
      REFUSE: 'bg-red-100 text-red-800',
      ANNULE: 'bg-gray-100 text-gray-800',
    };
    return colors[statut] || 'bg-gray-100 text-gray-800';
  };

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
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Devis</h1>
          <p className="text-gray-600">Gérez vos devis</p>
        </div>
        <button
          onClick={() => {
            setEditingDevis(null);
            setFormData({ client: null, date: new Date().toISOString().split('T')[0], details: [] });
            setShowModal(true);
          }}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          Nouveau devis
        </button>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {devis.map((d) => {
          const client = clients.find(c => c.id === d.client?.id);
          return (
            <div key={d.id} className="card">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center gap-3 mb-2">
                    <FileText className="text-primary-600" size={24} />
                    <div>
                      <h3 className="text-lg font-bold">{d.numeroDevis}</h3>
                      <p className="text-sm text-gray-600">
                        {client?.nom || 'Client inconnu'} • {new Date(d.date).toLocaleDateString('fr-FR')}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 mt-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(d.statut)}`}>
                      {d.statut}
                    </span>
                    <span className="text-lg font-bold text-primary-600">
                      {parseFloat(d.totalTtc || 0).toFixed(2)} € TTC
                    </span>
                    {d.details && d.details.length > 0 && (
                      <span className="text-sm text-gray-500">
                        {d.details.length} produit{d.details.length > 1 ? 's' : ''}
                      </span>
                    )}
                    {(!d.details || d.details.length === 0) && (
                      <span className="text-sm text-red-500 font-medium">
                        ⚠ Aucun produit
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleDownloadPdf(d.id)}
                    className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Télécharger PDF"
                  >
                    <Download size={18} />
                  </button>
                  {d.statut === 'BROUILLON' && (
                    <button
                      onClick={() => handleValider(d.id)}
                      disabled={!d.details || d.details.length === 0}
                      className={`p-2 rounded-lg transition-colors ${
                        (!d.details || d.details.length === 0)
                          ? 'text-gray-400 cursor-not-allowed'
                          : 'text-green-600 hover:bg-green-50'
                      }`}
                      title={(!d.details || d.details.length === 0) ? "Ajoutez des produits avant de valider" : "Valider"}
                    >
                      <Check size={18} />
                    </button>
                  )}
                  {(d.statut === 'VALIDE' || d.statut === 'ACCEPTE') && !d.facture && (
                    <button
                      onClick={() => handleConvertir(d.id)}
                      className="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                      title="Convertir en facture"
                    >
                      <FileText size={18} />
                    </button>
                  )}
                  <button
                    onClick={() => handleDelete(d.id)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Supprimer"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-3xl animate-slide-up max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold">
                {editingDevis ? 'Modifier le devis' : 'Nouveau devis'}
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
              </div>

              <div className="border-t pt-4">
                <h3 className="font-semibold mb-3">Produits</h3>
                <div className="flex gap-2 mb-4">
                  <select
                    value={newDetail.produit || ''}
                    onChange={(e) => {
                      const selectedValue = e.target.value;
                      console.log('Produit sélectionné dans select:', selectedValue);
                      setNewDetail(prev => ({ ...prev, produit: selectedValue }));
                    }}
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
                      console.log('Bouton Ajouter cliqué, newDetail:', newDetail);
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
                      
                      console.log('Affichage détail:', { index, detail, produitId, produit, quantite, prixUnitaire, montantHt });
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
                  {editingDevis ? 'Modifier' : 'Créer'}
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
    </div>
  );
};

export default Devis;

