package com.entreprise.gestion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facture")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_facture", unique = true, nullable = false)
    private String numeroFacture;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_devis")
    @JsonIgnore
    private Devis devis;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
    
    @Column(name = "montant_ht", precision = 10, scale = 2)
    private BigDecimal montantHt = BigDecimal.ZERO;
    
    @Column(name = "montant_tva", precision = 10, scale = 2)
    private BigDecimal montantTva = BigDecimal.ZERO;
    
    @Column(name = "montant_ttc", precision = 10, scale = 2)
    private BigDecimal montantTtc = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement")
    private ModePaiement modePaiement;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.EMISE;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FactureDetail> details = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (date == null) {
            date = LocalDate.now();
        }
        if (dateEcheance == null) {
            dateEcheance = date.plusDays(30); // Échéance par défaut 30 jours
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum ModePaiement {
        ESPECE,
        CHEQUE,
        VIREMENT,
        CARTE_BANCAIRE,
        AUTRE
    }
    
    public enum StatutFacture {
        EMISE,
        ENVOYEE,
        PAYEE,
        EN_RETARD,
        ANNULEE
    }
}

