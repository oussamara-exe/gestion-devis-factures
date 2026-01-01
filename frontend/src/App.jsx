import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Clients from './pages/Clients';
import Produits from './pages/Produits';
import Devis from './pages/Devis';
import Factures from './pages/Factures';
import HistoriqueClient from './pages/HistoriqueClient';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/clients" element={<Clients />} />
          <Route path="/produits" element={<Produits />} />
          <Route path="/devis" element={<Devis />} />
          <Route path="/factures" element={<Factures />} />
          <Route path="/clients/:id/historique" element={<HistoriqueClient />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
