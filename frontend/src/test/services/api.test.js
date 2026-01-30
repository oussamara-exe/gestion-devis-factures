import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { clientService, produitService, devisService, factureService } from '../../services/api'

// Mock axios
vi.mock('axios')

describe('API Services', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('clientService', () => {
    it('should get all clients', async () => {
      const mockClients = [{ id: 1, nom: 'Test Client' }]
      axios.get.mockResolvedValue({ data: mockClients })

      const result = await clientService.getAll()

      expect(axios.get).toHaveBeenCalledWith('/clients')
      expect(result.data).toEqual(mockClients)
    })

    it('should create a client', async () => {
      const newClient = { nom: 'New Client', email: 'new@example.com' }
      axios.post.mockResolvedValue({ data: newClient })

      const result = await clientService.create(newClient)

      expect(axios.post).toHaveBeenCalledWith('/clients', newClient)
      expect(result.data).toEqual(newClient)
    })

    it('should search clients', async () => {
      const mockClients = [{ id: 1, nom: 'Test Client' }]
      axios.get.mockResolvedValue({ data: mockClients })

      const result = await clientService.search('Test')

      expect(axios.get).toHaveBeenCalledWith('/clients/search?search=Test')
      expect(result.data).toEqual(mockClients)
    })
  })

  describe('produitService', () => {
    it('should get all produits', async () => {
      const mockProduits = [{ id: 1, nom: 'Produit Test' }]
      axios.get.mockResolvedValue({ data: mockProduits })

      const result = await produitService.getAll()

      expect(axios.get).toHaveBeenCalledWith('/produits')
      expect(result.data).toEqual(mockProduits)
    })

    it('should get produits en stock', async () => {
      const mockProduits = [{ id: 1, nom: 'Produit Test', stock: 10 }]
      axios.get.mockResolvedValue({ data: mockProduits })

      const result = await produitService.getEnStock()

      expect(axios.get).toHaveBeenCalledWith('/produits/stock')
      expect(result.data).toEqual(mockProduits)
    })
  })

  describe('devisService', () => {
    it('should validate a devis', async () => {
      const mockDevis = { id: 1, numeroDevis: 'DEV-00001', statut: 'VALIDE' }
      axios.put.mockResolvedValue({ data: mockDevis })

      const result = await devisService.valider(1)

      expect(axios.put).toHaveBeenCalledWith('/devis/1/valider')
      expect(result.data).toEqual(mockDevis)
    })

    it('should convert devis to facture', async () => {
      const mockFacture = { id: 1, numeroFacture: 'FAC-00001' }
      axios.post.mockResolvedValue({ data: mockFacture })

      const result = await devisService.convertirEnFacture(1)

      expect(axios.post).toHaveBeenCalledWith('/devis/1/convertir-facture')
      expect(result.data).toEqual(mockFacture)
    })
  })

  describe('factureService', () => {
    it('should mark facture as paid', async () => {
      const mockFacture = { id: 1, numeroFacture: 'FAC-00001', statut: 'PAYEE' }
      axios.put.mockResolvedValue({ data: mockFacture })

      const result = await factureService.marquerPayee(1)

      expect(axios.put).toHaveBeenCalledWith('/factures/1/payer')
      expect(result.data).toEqual(mockFacture)
    })
  })
})

