package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    
    @Query("SELECT p FROM Produit p WHERE p.nom LIKE %:search% OR p.categorie LIKE %:search%")
    List<Produit> searchProduits(@Param("search") String search);
    
    List<Produit> findByCategorie(String categorie);
    
    List<Produit> findByStockGreaterThan(Integer stock);
}

