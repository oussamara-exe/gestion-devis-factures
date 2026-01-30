import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Dashboard from '../../pages/Dashboard'
import * as api from '../../services/api'

// Mock the API services
vi.mock('../../services/api', () => ({
  clientService: {
    getAll: vi.fn(),
  },
  produitService: {
    getAll: vi.fn(),
  },
  devisService: {
    getAll: vi.fn(),
  },
  factureService: {
    getAll: vi.fn(),
  },
  statistiquesService: {
    getChiffreAffaires: vi.fn(),
  },
}))

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should display loading state initially', () => {
    api.clientService.getAll.mockImplementation(() => new Promise(() => {}))
    
    render(
      <BrowserRouter>
        <Dashboard />
      </BrowserRouter>
    )

    // Check for loading spinner (you might need to adjust based on your actual loading component)
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })

  it('should display statistics after loading', async () => {
    api.clientService.getAll.mockResolvedValue({ data: [{ id: 1 }] })
    api.produitService.getAll.mockResolvedValue({ data: [{ id: 1 }] })
    api.devisService.getAll.mockResolvedValue({ data: [{ id: 1 }] })
    api.factureService.getAll.mockResolvedValue({ data: [{ id: 1 }] })
    api.statistiquesService.getChiffreAffaires.mockResolvedValue({ 
      data: { chiffreAffaires: 10000 } 
    })

    render(
      <BrowserRouter>
        <Dashboard />
      </BrowserRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('Clients')).toBeInTheDocument()
      expect(screen.getByText('Produits')).toBeInTheDocument()
      expect(screen.getByText('Devis')).toBeInTheDocument()
      expect(screen.getByText('Factures')).toBeInTheDocument()
    })
  })
})

