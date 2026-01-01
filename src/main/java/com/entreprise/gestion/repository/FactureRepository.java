package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.model.Facture.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    
    Optional<Facture> findByNumeroFacture(String numeroFacture);
    
    List<Facture> findByClientId(Long clientId);
    
    List<Facture> findByStatut(StatutFacture statut);
    
    @Query("SELECT f FROM Facture f WHERE f.date BETWEEN :dateDebut AND :dateFin")
    List<Facture> findByDateBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT MAX(CAST(SUBSTRING(f.numeroFacture, 5) AS int)) FROM Facture f WHERE f.numeroFacture LIKE 'FAC-%'")
    Integer findMaxNumeroFacture();
    
    @Query("SELECT SUM(f.montantTtc) FROM Facture f WHERE f.statut = :statut")
    Double sumMontantTtcByStatut(@Param("statut") StatutFacture statut);
}

