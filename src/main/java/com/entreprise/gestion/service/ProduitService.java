package com.entreprise.gestion.service;

import com.entreprise.gestion.model.Produit;
import com.entreprise.gestion.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitService {
    
    private final ProduitRepository produitRepository;
    
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }
    
    public Optional<Produit> getProduitById(Long id) {
        return produitRepository.findById(id);
    }
    
    public Produit createProduit(Produit produit) {
        if (produit.getStock() == null) {
            produit.setStock(0);
        }
        if (produit.getTauxTva() == null) {
            produit.setTauxTva(new java.math.BigDecimal("20.00"));
        }
        return produitRepository.save(produit);
    }
    
    public Produit updateProduit(Long id, Produit produitDetails) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + id));
        
        produit.setNom(produitDetails.getNom());
        produit.setDescription(produitDetails.getDescription());
        produit.setPrixUnitaire(produitDetails.getPrixUnitaire());
        produit.setStock(produitDetails.getStock());
        produit.setCategorie(produitDetails.getCategorie());
        if (produitDetails.getTauxTva() != null) {
            produit.setTauxTva(produitDetails.getTauxTva());
        }
        
        return produitRepository.save(produit);
    }
    
    public void deleteProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + id));
        produitRepository.delete(produit);
    }
    
    public List<Produit> searchProduits(String search) {
        return produitRepository.searchProduits(search);
    }
    
    public List<Produit> getProduitsByCategorie(String categorie) {
        return produitRepository.findByCategorie(categorie);
    }
    
    public List<Produit> getProduitsEnStock() {
        return produitRepository.findByStockGreaterThan(0);
    }
    
    public void updateStock(Long produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + produitId));
        
        int nouveauStock = produit.getStock() - quantite;
        if (nouveauStock < 0) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + produit.getNom());
        }
        
        produit.setStock(nouveauStock);
        produitRepository.save(produit);
    }
}

