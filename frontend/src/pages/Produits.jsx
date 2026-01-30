import { useEffect, useState } from 'react';
import { produitService } from '../services/api';
import { Plus, Search, Edit, Trash2, Package } from 'lucide-react';

const Produits = () => {
  const [produits, setProduits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingProduit, setEditingProduit] = useState(null);
  const [formData, setFormData] = useState({
    nom: '',
    description: '',
    prixUnitaire: '',
    stock: '',
    categorie: '',
    tauxTva: '20.00',
  });

  useEffect(() => {
    loadProduits();
  }, []);

  const loadProduits = async () => {
    try {
      const response = await produitService.getAll();
      setProduits(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement des produits:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    const term = e.target.value;
    setSearchTerm(term);
    if (term.trim()) {
      try {
        const response = await produitService.search(term);
        setProduits(response.data);
      } catch (error) {
        console.error('Erreur lors de la recherche:', error);
      }
    } else {
      loadProduits();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = {
        ...formData,
        prixUnitaire: parseFloat(formData.prixUnitaire),
        stock: parseInt(formData.stock),
        tauxTva: parseFloat(formData.tauxTva),
      };
      if (editingProduit) {
        await produitService.update(editingProduit.id, data);
      } else {
        await produitService.create(data);
      }
      setShowModal(false);
      setEditingProduit(null);
      setFormData({ nom: '', description: '', prixUnitaire: '', stock: '', categorie: '', tauxTva: '20.00' });
      loadProduits();
    } catch (error) {
      console.error('Erreur lors de la sauvegarde:', error);
      alert('Erreur lors de la sauvegarde du produit');
    }
  };

  const handleEdit = (produit) => {
    setEditingProduit(produit);
    setFormData({
      nom: produit.nom || '',
      description: produit.description || '',
      prixUnitaire: produit.prixUnitaire || '',
      stock: produit.stock || '',
      categorie: produit.categorie || '',
      tauxTva: produit.tauxTva || '20.00',
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')) {
      try {
        await produitService.delete(id);
        loadProduits();
      } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        alert('Erreur lors de la suppression du produit');
      }
    }
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
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Produits</h1>
          <p className="text-gray-600">Gérez votre catalogue de produits</p>
        </div>
        <button
          onClick={() => {
            setEditingProduit(null);
            setFormData({ nom: '', description: '', prixUnitaire: '', stock: '', categorie: '', tauxTva: '20.00' });
            setShowModal(true);
          }}
          className="btn-primary flex items-center gap-2"
        >
          <Plus size={20} />
          Ajouter un produit
        </button>
      </div>

      <div className="card">
        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Rechercher un produit..."
            value={searchTerm}
            onChange={handleSearch}
            className="input-field pl-10"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200">
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Nom</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Prix unitaire</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Stock</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">Catégorie</th>
                <th className="text-left py-3 px-4 font-semibold text-gray-700">TVA</th>
                <th className="text-right py-3 px-4 font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {produits.map((produit) => (
                <tr key={produit.id} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                  <td className="py-4 px-4">
                    <div className="flex items-center gap-2">
                      <Package size={18} className="text-gray-400" />
                      <span className="font-medium">{produit.nom}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4">{parseFloat(produit.prixUnitaire).toFixed(2)} MAD</td>
                  <td className="py-4 px-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      produit.stock > 10 ? 'bg-green-100 text-green-800' :
                      produit.stock > 0 ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {produit.stock}
                    </span>
                  </td>
                  <td className="py-4 px-4 text-gray-600">{produit.categorie || '-'}</td>
                  <td className="py-4 px-4 text-gray-600">{parseFloat(produit.tauxTva).toFixed(2)}%</td>
                  <td className="py-4 px-4">
                    <div className="flex justify-end gap-2">
                      <button
                        onClick={() => handleEdit(produit)}
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      >
                        <Edit size={18} />
                      </button>
                      <button
                        onClick={() => handleDelete(produit.id)}
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-md animate-slide-up max-h-[90vh] overflow-y-auto">
            <h2 className="text-2xl font-bold mb-4">
              {editingProduit ? 'Modifier le produit' : 'Nouveau produit'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nom *</label>
                <input
                  type="text"
                  required
                  value={formData.nom}
                  onChange={(e) => setFormData({ ...formData, nom: e.target.value })}
                  className="input-field"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="input-field"
                  rows="3"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Prix unitaire (MAD) *</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={formData.prixUnitaire}
                    onChange={(e) => setFormData({ ...formData, prixUnitaire: e.target.value })}
                    className="input-field"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Stock *</label>
                  <input
                    type="number"
                    required
                    value={formData.stock}
                    onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                    className="input-field"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Catégorie</label>
                  <input
                    type="text"
                    value={formData.categorie}
                    onChange={(e) => setFormData({ ...formData, categorie: e.target.value })}
                    className="input-field"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Taux TVA (%)</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.tauxTva}
                    onChange={(e) => setFormData({ ...formData, tauxTva: e.target.value })}
                    className="input-field"
                  />
                </div>
              </div>
              <div className="flex gap-3 pt-4">
                <button type="submit" className="btn-primary flex-1">
                  {editingProduit ? 'Modifier' : 'Créer'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowModal(false);
                    setEditingProduit(null);
                  }}
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

export default Produits;

