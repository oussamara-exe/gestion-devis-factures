package com.entreprise.gestion.service;

import com.entreprise.gestion.model.*;
import com.entreprise.gestion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FactureService {
    
    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final ProduitService produitService;
    
    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }
    
    public Optional<Facture> getFactureById(Long id) {
        return factureRepository.findById(id);
    }
    
    public Facture createFacture(Facture facture) {
        // Générer le numéro de facture
        String numeroFacture = generateNumeroFacture();
        facture.setNumeroFacture(numeroFacture);
        
        // Vérifier que le client existe
        if (facture.getClient() != null && facture.getClient().getId() != null) {
            Client client = clientRepository.findById(facture.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            facture.setClient(client);
        }
        
        // Traiter les détails de la facture
        if (facture.getDetails() != null && !facture.getDetails().isEmpty()) {
            for (FactureDetail detail : facture.getDetails()) {
                if (detail.getProduit() != null && detail.getProduit().getId() != null) {
                    Produit produit = produitRepository.findById(detail.getProduit().getId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + detail.getProduit().getId()));
                    detail.setProduit(produit);
                    detail.setPrixUnitaire(produit.getPrixUnitaire());
                    if (produit.getTauxTva() != null) {
                        detail.setTauxTva(produit.getTauxTva());
                    } else {
                        detail.setTauxTva(new BigDecimal("20.00"));
                    }
                }
                detail.setFacture(facture);
                
                // Calculer les montants manuellement avant la sauvegarde
                calculateDetailMontants(detail);
            }
        }
        
        // Calculer les totaux
        calculateTotals(facture);
        
        // Mettre à jour les stocks
        if (facture.getDetails() != null) {
            for (FactureDetail detail : facture.getDetails()) {
                if (detail.getProduit() != null && detail.getProduit().getId() != null) {
                    produitService.updateStock(detail.getProduit().getId(), detail.getQuantite());
                }
            }
        }
        
        return factureRepository.save(facture);
    }
    
    public Facture updateFacture(Long id, Facture factureDetails) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + id));
        
        if (facture.getStatut() == Facture.StatutFacture.PAYEE) {
            throw new RuntimeException("Impossible de modifier une facture payée");
        }
        
        if (factureDetails.getClient() != null && factureDetails.getClient().getId() != null) {
            Client client = clientRepository.findById(factureDetails.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            facture.setClient(client);
        }
        
        if (factureDetails.getDate() != null) {
            facture.setDate(factureDetails.getDate());
        }
        
        if (factureDetails.getDateEcheance() != null) {
            facture.setDateEcheance(factureDetails.getDateEcheance());
        }
        
        if (factureDetails.getModePaiement() != null) {
            facture.setModePaiement(factureDetails.getModePaiement());
        }
        
        // Mettre à jour les détails
        if (factureDetails.getDetails() != null) {
            facture.getDetails().clear();
            for (FactureDetail detail : factureDetails.getDetails()) {
                if (detail.getProduit() != null && detail.getProduit().getId() != null) {
                    Produit produit = produitRepository.findById(detail.getProduit().getId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
                    detail.setProduit(produit);
                    detail.setPrixUnitaire(produit.getPrixUnitaire());
                    if (produit.getTauxTva() != null) {
                        detail.setTauxTva(produit.getTauxTva());
                    } else {
                        detail.setTauxTva(new BigDecimal("20.00"));
                    }
                }
                detail.setFacture(facture);
                
                // Calculer les montants manuellement avant la sauvegarde
                calculateDetailMontants(detail);
                
                facture.getDetails().add(detail);
            }
        }
        
        calculateTotals(facture);
        return factureRepository.save(facture);
    }
    
    public void deleteFacture(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + id));
        
        if (facture.getStatut() == Facture.StatutFacture.PAYEE) {
            throw new RuntimeException("Impossible de supprimer une facture payée");
        }
        
        factureRepository.delete(facture);
    }
    
    public Facture marquerCommePayee(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + id));
        
        facture.setStatut(Facture.StatutFacture.PAYEE);
        return factureRepository.save(facture);
    }
    
    public List<Facture> getFacturesByClient(Long clientId) {
        return factureRepository.findByClientId(clientId);
    }
    
    public BigDecimal getChiffreAffaires() {
        List<Facture> facturesPayees = factureRepository.findByStatut(Facture.StatutFacture.PAYEE);
        return facturesPayees.stream()
                .map(Facture::getMontantTtc)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getChiffreAffairesByDate(LocalDate dateDebut, LocalDate dateFin) {
        List<Facture> factures = factureRepository.findByDateBetween(dateDebut, dateFin);
        return factures.stream()
                .filter(f -> f.getStatut() == Facture.StatutFacture.PAYEE)
                .map(Facture::getMontantTtc)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void calculateDetailMontants(FactureDetail detail) {
        if (detail.getPrixUnitaire() != null && detail.getQuantite() != null) {
            // Calculer montant HT
            detail.setMontantHt(detail.getPrixUnitaire().multiply(new BigDecimal(detail.getQuantite())));
            
            // S'assurer que le taux TVA est défini
            if (detail.getTauxTva() == null) {
                if (detail.getProduit() != null && detail.getProduit().getTauxTva() != null) {
                    detail.setTauxTva(detail.getProduit().getTauxTva());
                } else {
                    detail.setTauxTva(new BigDecimal("20.00"));
                }
            }
            
            // Calculer montant TVA
            BigDecimal montantHt = detail.getMontantHt();
            BigDecimal tauxTva = detail.getTauxTva();
            detail.setMontantTva(montantHt.multiply(tauxTva).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP));
            
            // Calculer montant TTC
            detail.setMontantTtc(montantHt.add(detail.getMontantTva()));
        }
    }
    
    private void calculateTotals(Facture facture) {
        BigDecimal montantHt = BigDecimal.ZERO;
        BigDecimal montantTva = BigDecimal.ZERO;
        
        if (facture.getDetails() != null) {
            for (FactureDetail detail : facture.getDetails()) {
                // Recalculer les montants si nécessaire
                if (detail.getMontantHt() == null || detail.getMontantTva() == null || detail.getMontantTtc() == null) {
                    calculateDetailMontants(detail);
                }
                
                if (detail.getMontantHt() != null) {
                    montantHt = montantHt.add(detail.getMontantHt());
                }
                if (detail.getMontantTva() != null) {
                    montantTva = montantTva.add(detail.getMontantTva());
                }
            }
        }
        
        facture.setMontantHt(montantHt);
        facture.setMontantTva(montantTva);
        facture.setMontantTtc(montantHt.add(montantTva));
    }
    
    private String generateNumeroFacture() {
        Integer maxNum = factureRepository.findMaxNumeroFacture();
        int nextNum = (maxNum == null ? 0 : maxNum) + 1;
        return String.format("FAC-%05d", nextNum);
    }
}

