package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Produit;
import com.entreprise.gestion.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProduitController {
    
    private final ProduitService produitService;
    
    @GetMapping
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(produitService.getAllProduits());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Produit> getProduitById(@PathVariable Long id) {
        return produitService.getProduitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Produit> createProduit(@Valid @RequestBody Produit produit) {
        Produit createdProduit = produitService.createProduit(produit);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduit);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Produit> updateProduit(@PathVariable Long id, @Valid @RequestBody Produit produit) {
        try {
            Produit updatedProduit = produitService.updateProduit(id, produit);
            return ResponseEntity.ok(updatedProduit);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        try {
            produitService.deleteProduit(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Produit>> searchProduits(@RequestParam String search) {
        return ResponseEntity.ok(produitService.searchProduits(search));
    }
    
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Produit>> getProduitsByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(produitService.getProduitsByCategorie(categorie));
    }
    
    @GetMapping("/stock")
    public ResponseEntity<List<Produit>> getProduitsEnStock() {
        return ResponseEntity.ok(produitService.getProduitsEnStock());
    }
}

