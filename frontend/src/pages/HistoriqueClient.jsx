import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { clientService, devisService, factureService } from '../services/api';
import { ArrowLeft, FileText, Receipt, Download, Calendar, DollarSign } from 'lucide-react';

const HistoriqueClient = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [client, setClient] = useState(null);
  const [devis, setDevis] = useState([]);
  const [factures, setFactures] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      const [clientRes, devisRes, facturesRes] = await Promise.all([
        clientService.getById(id),
        devisService.getByClient(id),
        factureService.getByClient(id),
      ]);
      setClient(clientRes.data);
      setDevis(devisRes.data);
      setFactures(facturesRes.data);
    } catch (error) {
      console.error('Erreur lors du chargement:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadDevisPdf = async (devisId) => {
    try {
      const response = await devisService.downloadPdf(devisId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `devis-${devisId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Erreur lors du téléchargement:', error);
      alert('Erreur lors du téléchargement du PDF');
    }
  };

  const handleDownloadFacturePdf = async (factureId) => {
    try {
      const response = await factureService.downloadPdf(factureId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `facture-${factureId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Erreur lors du téléchargement:', error);
      alert('Erreur lors du téléchargement du PDF');
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
      EMISE: 'bg-gray-100 text-gray-800',
      ENVOYEE: 'bg-blue-100 text-blue-800',
      PAYEE: 'bg-green-100 text-green-800',
      EN_RETARD: 'bg-red-100 text-red-800',
      ANNULEE: 'bg-gray-100 text-gray-800',
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

  if (!client) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-600">Client non trouvé</p>
        <button onClick={() => navigate('/clients')} className="btn-primary mt-4">
          Retour aux clients
        </button>
      </div>
    );
  }

  const totalDevis = devis.reduce((sum, d) => sum + parseFloat(d.totalTtc || 0), 0);
  const totalFactures = factures.reduce((sum, f) => sum + parseFloat(f.montantTtc || 0), 0);
  const facturesPayees = factures.filter(f => f.statut === 'PAYEE').length;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center gap-4 mb-6">
        <button
          onClick={() => navigate('/clients')}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Historique - {client.nom}</h1>
          <p className="text-gray-600">Toutes les opérations de ce client</p>
        </div>
      </div>

      {/* Informations client */}
      <div className="card bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <p className="text-sm text-blue-700 mb-1">Email</p>
            <p className="font-semibold text-blue-900">{client.email || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-blue-700 mb-1">Téléphone</p>
            <p className="font-semibold text-blue-900">{client.telephone || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-blue-700 mb-1">Adresse</p>
            <p className="font-semibold text-blue-900">
              {client.adresse ? `${client.adresse}, ${client.codePostal || ''} ${client.ville || ''}` : 'N/A'}
            </p>
          </div>
        </div>
      </div>

      {/* Statistiques */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Devis</p>
              <p className="text-2xl font-bold text-gray-900">{devis.length}</p>
              <p className="text-sm text-gray-500 mt-1">{totalDevis.toFixed(2)} MAD</p>
            </div>
            <FileText className="text-blue-600" size={32} />
          </div>
        </div>
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Total Factures</p>
              <p className="text-2xl font-bold text-gray-900">{factures.length}</p>
              <p className="text-sm text-gray-500 mt-1">{totalFactures.toFixed(2)} MAD</p>
            </div>
            <Receipt className="text-purple-600" size={32} />
          </div>
        </div>
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Factures Payées</p>
              <p className="text-2xl font-bold text-green-600">{facturesPayees}</p>
            </div>
            <DollarSign className="text-green-600" size={32} />
          </div>
        </div>
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600 mb-1">Taux de conversion</p>
              <p className="text-2xl font-bold text-indigo-600">
                {devis.length > 0 ? ((factures.length / devis.length) * 100).toFixed(1) : 0}%
              </p>
            </div>
            <Calendar className="text-indigo-600" size={32} />
          </div>
        </div>
      </div>

      {/* Devis */}
      <div className="card">
        <h2 className="text-xl font-bold mb-4 text-gray-800">Devis ({devis.length})</h2>
        {devis.length === 0 ? (
          <p className="text-gray-500 text-center py-8">Aucun devis pour ce client</p>
        ) : (
          <div className="space-y-3">
            {devis.map((d) => (
              <div key={d.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <FileText className="text-blue-600" size={20} />
                      <div>
                        <h3 className="font-bold">{d.numeroDevis}</h3>
                        <p className="text-sm text-gray-600">
                          {new Date(d.date).toLocaleDateString('fr-FR')}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 mt-2">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(d.statut)}`}>
                        {d.statut}
                      </span>
                      <span className="font-semibold text-primary-600">
                        {parseFloat(d.totalTtc || 0).toFixed(2)} MAD TTC
                      </span>
                      {d.details && d.details.length > 0 && (
                        <span className="text-sm text-gray-500">
                          {d.details.length} produit{d.details.length > 1 ? 's' : ''}
                        </span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => handleDownloadDevisPdf(d.id)}
                    className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Télécharger PDF"
                  >
                    <Download size={18} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Factures */}
      <div className="card">
        <h2 className="text-xl font-bold mb-4 text-gray-800">Factures ({factures.length})</h2>
        {factures.length === 0 ? (
          <p className="text-gray-500 text-center py-8">Aucune facture pour ce client</p>
        ) : (
          <div className="space-y-3">
            {factures.map((f) => (
              <div key={f.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <Receipt className="text-purple-600" size={20} />
                      <div>
                        <h3 className="font-bold">{f.numeroFacture}</h3>
                        <p className="text-sm text-gray-600">
                          {new Date(f.date).toLocaleDateString('fr-FR')}
                          {f.dateEcheance && (
                            <span className={`ml-2 ${
                              new Date(f.dateEcheance) < new Date() && f.statut !== 'PAYEE' 
                                ? 'text-red-600 font-semibold' 
                                : 'text-gray-500'
                            }`}>
                              • Échéance: {new Date(f.dateEcheance).toLocaleDateString('fr-FR')}
                            </span>
                          )}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 mt-2">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(f.statut)}`}>
                        {f.statut}
                      </span>
                      <span className="font-semibold text-primary-600">
                        {parseFloat(f.montantTtc || 0).toFixed(2)} MAD TTC
                      </span>
                      {f.modePaiement && (
                        <span className="text-sm text-gray-500">
                          Paiement: {f.modePaiement}
                        </span>
                      )}
                      {f.details && f.details.length > 0 && (
                        <span className="text-sm text-gray-500">
                          {f.details.length} produit{f.details.length > 1 ? 's' : ''}
                        </span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => handleDownloadFacturePdf(f.id)}
                    className="p-2 text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                    title="Télécharger PDF"
                  >
                    <Download size={18} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default HistoriqueClient;

