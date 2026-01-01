package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Devis;
import com.entreprise.gestion.model.Devis.StatutDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {
    
    Optional<Devis> findByNumeroDevis(String numeroDevis);
    
    List<Devis> findByClientId(Long clientId);
    
    List<Devis> findByStatut(StatutDevis statut);
    
    @Query("SELECT d FROM Devis d WHERE d.date BETWEEN :dateDebut AND :dateFin")
    List<Devis> findByDateBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
    
    @Query("SELECT MAX(CAST(SUBSTRING(d.numeroDevis, 7) AS int)) FROM Devis d WHERE d.numeroDevis LIKE 'DEV-%'")
    Integer findMaxNumeroDevis();
}

