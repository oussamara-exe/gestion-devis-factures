import { useEffect, useState } from 'react';
import { clientService, produitService, devisService, factureService, statistiquesService } from '../services/api';
import { 
  Users, Package, FileText, Receipt, TrendingUp, DollarSign, 
  AlertCircle, CheckCircle, Clock, ArrowUp, ArrowDown 
} from 'lucide-react';
import {
  LineChart, Line, AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

const Dashboard = () => {
  const [stats, setStats] = useState({
    clients: 0,
    produits: 0,
    devis: 0,
    factures: 0,
    chiffreAffaires: 0,
  });
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [clientsRes, produitsRes, devisRes, facturesRes, caRes, dashboardRes] = await Promise.all([
        clientService.getAll(),
        produitService.getAll(),
        devisService.getAll(),
        factureService.getAll(),
        statistiquesService.getChiffreAffaires(),
        statistiquesService.getDashboardStats(),
      ]);

      setStats({
        clients: clientsRes.data.length,
        produits: produitsRes.data.length,
        devis: devisRes.data.length,
        factures: facturesRes.data.length,
        chiffreAffaires: caRes.data.chiffreAffaires || 0,
      });
      
      setDashboardData(dashboardRes.data);
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error);
    } finally {
      setLoading(false);
    }
  };

  const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'];

  const statCards = [
    {
      title: 'Clients',
      value: stats.clients,
      icon: Users,
      color: 'bg-blue-500',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600',
    },
    {
      title: 'Produits',
      value: stats.produits,
      icon: Package,
      color: 'bg-green-500',
      bgColor: 'bg-green-50',
      textColor: 'text-green-600',
    },
    {
      title: 'Devis',
      value: stats.devis,
      icon: FileText,
      color: 'bg-yellow-500',
      bgColor: 'bg-yellow-50',
      textColor: 'text-yellow-600',
    },
    {
      title: 'Factures',
      value: stats.factures,
      icon: Receipt,
      color: 'bg-purple-500',
      bgColor: 'bg-purple-50',
      textColor: 'text-purple-600',
    },
    {
      title: 'Chiffre d\'affaires',
      value: `${parseFloat(stats.chiffreAffaires).toFixed(2)} €`,
      icon: TrendingUp,
      color: 'bg-indigo-500',
      bgColor: 'bg-indigo-50',
      textColor: 'text-indigo-600',
    },
  ];

  // Données pour les graphiques (exemple - à remplacer par les vraies données)
  const caParMoisData = dashboardData?.caParMois?.map(item => ({
    mois: item.mois?.substring(5) || '',
    ca: parseFloat(item.chiffreAffaires || 0),
  })) || [
    { mois: 'Jan', ca: 0 },
    { mois: 'Fév', ca: 0 },
    { mois: 'Mar', ca: 0 },
    { mois: 'Avr', ca: 0 },
    { mois: 'Mai', ca: 0 },
    { mois: 'Juin', ca: 0 },
  ];

  const facturesParStatutData = dashboardData?.facturesParStatut ? 
    Object.entries(dashboardData.facturesParStatut).map(([name, value]) => ({
      name,
      value: Number(value),
    })) : [];

  const devisParStatutData = dashboardData?.devisParStatut ? 
    Object.entries(dashboardData.devisParStatut).map(([name, value]) => ({
      name,
      value: Number(value),
    })) : [];

  const topClientsData = dashboardData?.topClients?.slice(0, 5).map((client, index) => ({
    nom: client.nom || 'Client',
    ca: parseFloat(client.chiffreAffaires || 0),
  })) || [];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
        <p className="text-gray-600">Vue d'ensemble de votre activité</p>
      </div>

      {/* Cartes de statistiques */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
        {statCards.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <div
              key={index}
              className="card animate-slide-up hover:shadow-lg transition-shadow"
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 mb-1">{stat.title}</p>
                  <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                </div>
                <div className={`${stat.bgColor} p-3 rounded-lg`}>
                  <Icon className={`${stat.color} text-white`} size={24} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Indicateurs de performance */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="card bg-gradient-to-br from-green-50 to-green-100 border border-green-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-green-700 mb-1">Factures payées</p>
              <p className="text-2xl font-bold text-green-900">
                {dashboardData?.facturesPayees || 0}
              </p>
            </div>
            <CheckCircle className="text-green-600" size={32} />
          </div>
        </div>

        <div className="card bg-gradient-to-br from-red-50 to-red-100 border border-red-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-red-700 mb-1">Factures en retard</p>
              <p className="text-2xl font-bold text-red-900">
                {dashboardData?.facturesEnRetard || 0}
              </p>
              {dashboardData?.montantEnRetard && (
                <p className="text-xs text-red-600 mt-1">
                  {parseFloat(dashboardData.montantEnRetard).toFixed(2)} €
                </p>
              )}
            </div>
            <AlertCircle className="text-red-600" size={32} />
          </div>
        </div>

        <div className="card bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-blue-700 mb-1">Taux de conversion</p>
              <p className="text-2xl font-bold text-blue-900">
                {stats.devis > 0 ? ((stats.factures / stats.devis) * 100).toFixed(1) : 0}%
              </p>
            </div>
            <TrendingUp className="text-blue-600" size={32} />
          </div>
        </div>

        <div className="card bg-gradient-to-br from-purple-50 to-purple-100 border border-purple-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-purple-700 mb-1">Panier moyen</p>
              <p className="text-2xl font-bold text-purple-900">
                {stats.factures > 0 
                  ? (parseFloat(stats.chiffreAffaires) / stats.factures).toFixed(2) 
                  : 0} €
              </p>
            </div>
            <DollarSign className="text-purple-600" size={32} />
          </div>
        </div>
      </div>

      {/* Graphiques */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chiffre d'affaires par mois */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">Chiffre d'affaires (6 derniers mois)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={caParMoisData}>
              <defs>
                <linearGradient id="colorCa" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.8}/>
                  <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis 
                dataKey="mois" 
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <YAxis 
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `${value}€`}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#fff', 
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
                formatter={(value) => [`${parseFloat(value).toFixed(2)} €`, 'CA']}
              />
              <Area 
                type="monotone" 
                dataKey="ca" 
                stroke="#3B82F6" 
                fillOpacity={1} 
                fill="url(#colorCa)" 
                strokeWidth={2}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Répartition des factures par statut */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">Répartition des factures par statut</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={facturesParStatutData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {facturesParStatutData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Répartition des devis par statut */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">Répartition des devis par statut</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={devisParStatutData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis 
                dataKey="name" 
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <YAxis 
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#fff', 
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
              <Bar dataKey="value" fill="#8B5CF6" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Top 5 clients */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">Top 5 clients par chiffre d'affaires</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={topClientsData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis 
                type="number"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `${value}€`}
              />
              <YAxis 
                type="category" 
                dataKey="nom" 
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                width={100}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: '#fff', 
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
                formatter={(value) => [`${parseFloat(value).toFixed(2)} €`, 'CA']}
              />
              <Bar dataKey="ca" fill="#10B981" radius={[0, 8, 8, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Tableau des top clients */}
      {topClientsData.length > 0 && (
        <div className="card">
          <h3 className="text-lg font-semibold mb-4 text-gray-800">Top clients</h3>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Rang</th>
                  <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">Client</th>
                  <th className="text-right py-3 px-4 text-sm font-semibold text-gray-700">Chiffre d'affaires</th>
                </tr>
              </thead>
              <tbody>
                {topClientsData.map((client, index) => (
                  <tr key={index} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                    <td className="py-3 px-4">
                      <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-primary-100 text-primary-600 font-semibold text-sm">
                        {index + 1}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-medium text-gray-900">{client.nom}</td>
                    <td className="py-3 px-4 text-right font-semibold text-primary-600">
                      {client.ca.toFixed(2)} €
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
