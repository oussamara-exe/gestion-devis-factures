package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProduitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProduitRepository produitRepository;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = new Produit();
        produit.setNom("Produit Test");
        produit.setDescription("Description test");
        produit.setPrixUnitaire(new BigDecimal("99.99"));
        produit.setStock(50);
        produit.setCategorie("Électronique");
        produit.setTauxTva(new BigDecimal("20.00"));
        produit = entityManager.persistAndFlush(produit);
    }

    @Test
    void testFindById() {
        // When
        var found = produitRepository.findById(produit.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Produit Test", found.get().getNom());
        assertEquals(new BigDecimal("99.99"), found.get().getPrixUnitaire());
    }

    @Test
    void testSearchProduits() {
        // Given
        Produit produit2 = new Produit();
        produit2.setNom("Autre Produit");
        produit2.setPrixUnitaire(new BigDecimal("49.99"));
        produit2.setStock(30);
        produit2.setCategorie("Mobilier");
        entityManager.persistAndFlush(produit2);

        // When
        List<Produit> results = produitRepository.searchProduits("Test");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Produit Test", results.get(0).getNom());
    }

    @Test
    void testFindByCategorie() {
        // Given
        Produit produit2 = new Produit();
        produit2.setNom("Produit 2");
        produit2.setPrixUnitaire(new BigDecimal("29.99"));
        produit2.setStock(20);
        produit2.setCategorie("Électronique");
        entityManager.persistAndFlush(produit2);

        // When
        List<Produit> results = produitRepository.findByCategorie("Électronique");

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testFindByStockGreaterThan() {
        // Given
        Produit produit2 = new Produit();
        produit2.setNom("Produit Épuisé");
        produit2.setPrixUnitaire(new BigDecimal("19.99"));
        produit2.setStock(0);
        entityManager.persistAndFlush(produit2);

        // When
        List<Produit> results = produitRepository.findByStockGreaterThan(0);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Produit Test", results.get(0).getNom());
    }

    @Test
    void testSaveProduit() {
        // Given
        Produit newProduit = new Produit();
        newProduit.setNom("Nouveau Produit");
        newProduit.setPrixUnitaire(new BigDecimal("79.99"));
        newProduit.setStock(100);

        // When
        Produit saved = produitRepository.save(newProduit);
        entityManager.flush();

        // Then
        assertNotNull(saved.getId());
        var found = produitRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Nouveau Produit", found.get().getNom());
    }
}

