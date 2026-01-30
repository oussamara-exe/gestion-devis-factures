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
@Table(name = "devis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Devis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_devis", unique = true, nullable = false)
    private String numeroDevis;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "total_ht", precision = 10, scale = 2)
    private BigDecimal totalHt = BigDecimal.ZERO;
    
    @Column(name = "total_tva", precision = 10, scale = 2)
    private BigDecimal totalTva = BigDecimal.ZERO;
    
    @Column(name = "total_ttc", precision = 10, scale = 2)
    private BigDecimal totalTtc = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDevis statut = StatutDevis.BROUILLON;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DevisDetail> details = new ArrayList<>();
    
    @OneToOne(mappedBy = "devis", cascade = CascadeType.ALL)
    @JsonIgnore
    private Facture facture;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (date == null) {
            date = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum StatutDevis {
        BROUILLON,
        VALIDE,
        ENVOYE,
        ACCEPTE,
        REFUSE,
        ANNULE
    }
}

