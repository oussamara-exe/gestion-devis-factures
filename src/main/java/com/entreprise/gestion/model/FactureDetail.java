package com.entreprise.gestion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "facture_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_facture", nullable = false)
    @JsonIgnore
    private Facture facture;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(nullable = false)
    private Integer quantite;
    
    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;
    
    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTva;
    
    @Column(name = "montant_ht", precision = 10, scale = 2)
    private BigDecimal montantHt;
    
    @Column(name = "montant_tva", precision = 10, scale = 2)
    private BigDecimal montantTva;
    
    @Column(name = "montant_ttc", precision = 10, scale = 2)
    private BigDecimal montantTtc;
    
    @PrePersist
    @PreUpdate
    protected void calculateMontants() {
        if (prixUnitaire != null && quantite != null) {
            montantHt = prixUnitaire.multiply(new BigDecimal(quantite));
            
            if (tauxTva == null) {
                tauxTva = produit != null ? produit.getTauxTva() : new BigDecimal("20.00");
            }
            
            montantTva = montantHt.multiply(tauxTva).divide(new BigDecimal("100"));
            montantTtc = montantHt.add(montantTva);
        }
    }
}

