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
public class DevisService {
    
    private final DevisRepository devisRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final FactureRepository factureRepository;
    private final ProduitService produitService;
    
    public Optional<Devis> getDevisById(Long id) {
        Optional<Devis> devisOpt = devisRepository.findById(id);
        if (devisOpt.isPresent()) {
            Devis devis = devisOpt.get();
            // Recalculer les montants si nécessaire
            boolean needsRecalculation = false;
            if (devis.getDetails() != null) {
                for (DevisDetail detail : devis.getDetails()) {
                    if (detail.getMontantHt() == null || detail.getMontantTva() == null || detail.getMontantTtc() == null) {
                        calculateDetailMontants(detail);
                        needsRecalculation = true;
                    }
                }
            }
            if (needsRecalculation || devis.getTotalHt() == null || devis.getTotalTtc() == null || 
                (devis.getTotalHt().compareTo(BigDecimal.ZERO) == 0 && devis.getDetails() != null && !devis.getDetails().isEmpty())) {
                calculateTotals(devis);
                devisRepository.save(devis);
            }
        }
        return devisOpt;
    }
    
    public List<Devis> getDevisByClient(Long clientId) {
        return devisRepository.findByClientId(clientId);
    }
    
    public List<Devis> getAllDevis() {
        List<Devis> devisList = devisRepository.findAll();
        // Recalculer les totaux pour les devis qui en ont besoin
        for (Devis devis : devisList) {
            boolean needsRecalculation = false;
            if (devis.getDetails() != null) {
                for (DevisDetail detail : devis.getDetails()) {
                    if (detail.getMontantHt() == null || detail.getMontantTva() == null || detail.getMontantTtc() == null) {
                        calculateDetailMontants(detail);
                        needsRecalculation = true;
                    }
                }
            }
            if (needsRecalculation || devis.getTotalHt() == null || devis.getTotalTtc() == null || 
                (devis.getTotalHt().compareTo(BigDecimal.ZERO) == 0 && devis.getDetails() != null && !devis.getDetails().isEmpty())) {
                calculateTotals(devis);
                devisRepository.save(devis);
            }
        }
        return devisList;
    }
    
    public Devis createDevis(Devis devis) {
        // Générer le numéro de devis
        String numeroDevis = generateNumeroDevis();
        devis.setNumeroDevis(numeroDevis);
        
        // Vérifier que le client existe
        if (devis.getClient() != null && devis.getClient().getId() != null) {
            Client client = clientRepository.findById(devis.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            devis.setClient(client);
        }
        
        // Traiter les détails du devis
        if (devis.getDetails() != null && !devis.getDetails().isEmpty()) {
            for (DevisDetail detail : devis.getDetails()) {
                if (detail.getProduit() != null && detail.getProduit().getId() != null) {
                    Produit produit = produitRepository.findById(detail.getProduit().getId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + detail.getProduit().getId()));
                    detail.setProduit(produit);
                    // Toujours utiliser le prix et le taux TVA du produit depuis la base de données
                    detail.setPrixUnitaire(produit.getPrixUnitaire());
                    if (produit.getTauxTva() != null) {
                        detail.setTauxTva(produit.getTauxTva());
                    } else {
                        detail.setTauxTva(new BigDecimal("20.00"));
                    }
                }
                detail.setDevis(devis);
                
                // Calculer les montants manuellement avant la sauvegarde
                calculateDetailMontants(detail);
            }
        }
        
        // Calculer les totaux
        calculateTotals(devis);
        
        return devisRepository.save(devis);
    }
    
    public Devis updateDevis(Long id, Devis devisDetails) {
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + id));
        
        if (devisDetails.getClient() != null && devisDetails.getClient().getId() != null) {
            Client client = clientRepository.findById(devisDetails.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));
            devis.setClient(client);
        }
        
        if (devisDetails.getDate() != null) {
            devis.setDate(devisDetails.getDate());
        }
        
        // Mettre à jour les détails
        if (devisDetails.getDetails() != null) {
            devis.getDetails().clear();
            for (DevisDetail detail : devisDetails.getDetails()) {
                if (detail.getProduit() != null && detail.getProduit().getId() != null) {
                    Produit produit = produitRepository.findById(detail.getProduit().getId())
                            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + detail.getProduit().getId()));
                    detail.setProduit(produit);
                    // Toujours utiliser le prix et le taux TVA du produit depuis la base de données
                    detail.setPrixUnitaire(produit.getPrixUnitaire());
                    if (produit.getTauxTva() != null) {
                        detail.setTauxTva(produit.getTauxTva());
                    } else {
                        detail.setTauxTva(new BigDecimal("20.00"));
                    }
                }
                detail.setDevis(devis);
                
                // Calculer les montants manuellement avant la sauvegarde
                calculateDetailMontants(detail);
                
                devis.getDetails().add(detail);
            }
        }
        
        calculateTotals(devis);
        return devisRepository.save(devis);
    }
    
    public void deleteDevis(Long id) {
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + id));
        devisRepository.delete(devis);
    }
    
    public Devis validerDevis(Long id) {
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + id));
        
        if (devis.getDetails() == null || devis.getDetails().isEmpty()) {
            throw new RuntimeException("Le devis doit contenir au moins un produit");
        }
        
        devis.setStatut(Devis.StatutDevis.VALIDE);
        return devisRepository.save(devis);
    }
    
    public Facture convertirDevisEnFacture(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + devisId));
        
        if (devis.getStatut() != Devis.StatutDevis.VALIDE && devis.getStatut() != Devis.StatutDevis.ACCEPTE) {
            throw new RuntimeException("Seuls les devis validés ou acceptés peuvent être convertis en facture");
        }
        
        if (devis.getFacture() != null) {
            throw new RuntimeException("Ce devis a déjà été converti en facture");
        }
        
        // Créer la facture
        Facture facture = new Facture();
        facture.setNumeroFacture(generateNumeroFacture());
        facture.setClient(devis.getClient());
        facture.setDevis(devis);
        facture.setDate(LocalDate.now());
        facture.setMontantHt(devis.getTotalHt());
        facture.setMontantTva(devis.getTotalTva());
        facture.setMontantTtc(devis.getTotalTtc());
        facture.setStatut(Facture.StatutFacture.EMISE);
        
        // Copier les détails
        for (DevisDetail devisDetail : devis.getDetails()) {
            FactureDetail factureDetail = new FactureDetail();
            factureDetail.setFacture(facture);
            factureDetail.setProduit(devisDetail.getProduit());
            factureDetail.setQuantite(devisDetail.getQuantite());
            factureDetail.setPrixUnitaire(devisDetail.getPrixUnitaire());
            factureDetail.setTauxTva(devisDetail.getTauxTva());
            factureDetail.setMontantHt(devisDetail.getMontantHt());
            factureDetail.setMontantTva(devisDetail.getMontantTva());
            factureDetail.setMontantTtc(devisDetail.getMontantTtc());
            facture.getDetails().add(factureDetail);
            
            // Mettre à jour le stock
            produitService.updateStock(devisDetail.getProduit().getId(), devisDetail.getQuantite());
        }
        
        facture = factureRepository.save(facture);
        devis.setFacture(facture);
        devisRepository.save(devis);
        
        return facture;
    }
    
    private void calculateDetailMontants(DevisDetail detail) {
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
    
    private void calculateTotals(Devis devis) {
        BigDecimal totalHt = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;
        
        if (devis.getDetails() != null) {
            for (DevisDetail detail : devis.getDetails()) {
                // Recalculer les montants si nécessaire
                if (detail.getMontantHt() == null || detail.getMontantTva() == null || detail.getMontantTtc() == null) {
                    calculateDetailMontants(detail);
                }
                
                if (detail.getMontantHt() != null) {
                    totalHt = totalHt.add(detail.getMontantHt());
                }
                if (detail.getMontantTva() != null) {
                    totalTva = totalTva.add(detail.getMontantTva());
                }
            }
        }
        
        devis.setTotalHt(totalHt);
        devis.setTotalTva(totalTva);
        devis.setTotalTtc(totalHt.add(totalTva));
    }
    
    private String generateNumeroDevis() {
        Integer maxNum = devisRepository.findMaxNumeroDevis();
        int nextNum = (maxNum == null ? 0 : maxNum) + 1;
        return String.format("DEV-%05d", nextNum);
    }
    
    private String generateNumeroFacture() {
        Integer maxNum = factureRepository.findMaxNumeroFacture();
        int nextNum = (maxNum == null ? 0 : maxNum) + 1;
        return String.format("FAC-%05d", nextNum);
    }
}

