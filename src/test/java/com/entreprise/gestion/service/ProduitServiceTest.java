package com.entreprise.gestion.service;

import com.entreprise.gestion.model.Produit;
import com.entreprise.gestion.repository.ProduitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private ProduitService produitService;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Produit Test");
        produit.setDescription("Description du produit");
        produit.setPrixUnitaire(new BigDecimal("99.99"));
        produit.setStock(50);
        produit.setCategorie("Électronique");
        produit.setTauxTva(new BigDecimal("20.00"));
    }

    @Test
    void testGetAllProduits() {
        // Given
        List<Produit> produits = Arrays.asList(produit);
        when(produitRepository.findAll()).thenReturn(produits);

        // When
        List<Produit> result = produitService.getAllProduits();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Produit Test", result.get(0).getNom());
        verify(produitRepository, times(1)).findAll();
    }

    @Test
    void testGetProduitById() {
        // Given
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));

        // When
        Optional<Produit> result = produitService.getProduitById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Produit Test", result.get().getNom());
        verify(produitRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateProduit() {
        // Given
        Produit newProduit = new Produit();
        newProduit.setNom("Nouveau Produit");
        newProduit.setPrixUnitaire(new BigDecimal("49.99"));
        newProduit.setStock(100);
        when(produitRepository.save(any(Produit.class))).thenReturn(newProduit);

        // When
        Produit result = produitService.createProduit(newProduit);

        // Then
        assertNotNull(result);
        assertEquals("Nouveau Produit", result.getNom());
        assertNotNull(result.getTauxTva()); // TVA par défaut doit être défini
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testCreateProduitWithNullStock() {
        // Given
        Produit newProduit = new Produit();
        newProduit.setNom("Produit Sans Stock");
        newProduit.setPrixUnitaire(new BigDecimal("29.99"));
        newProduit.setStock(null);
        when(produitRepository.save(any(Produit.class))).thenReturn(newProduit);

        // When
        Produit result = produitService.createProduit(newProduit);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getStock()); // Stock doit être initialisé à 0
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testUpdateProduit() {
        // Given
        Produit updatedProduit = new Produit();
        updatedProduit.setNom("Produit Modifié");
        updatedProduit.setPrixUnitaire(new BigDecimal("79.99"));
        updatedProduit.setStock(75);

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(updatedProduit);

        // When
        Produit result = produitService.updateProduit(1L, updatedProduit);

        // Then
        assertNotNull(result);
        verify(produitRepository, times(1)).findById(1L);
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testUpdateProduitNotFound() {
        // Given
        Produit updatedProduit = new Produit();
        when(produitRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> produitService.updateProduit(999L, updatedProduit));
        verify(produitRepository, times(1)).findById(999L);
        verify(produitRepository, never()).save(any(Produit.class));
    }

    @Test
    void testDeleteProduit() {
        // Given
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        doNothing().when(produitRepository).delete(any(Produit.class));

        // When
        produitService.deleteProduit(1L);

        // Then
        verify(produitRepository, times(1)).findById(1L);
        verify(produitRepository, times(1)).delete(any(Produit.class));
    }

    @Test
    void testUpdateStock() {
        // Given
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any(Produit.class))).thenReturn(produit);

        // When
        produitService.updateStock(1L, 10);

        // Then
        verify(produitRepository, times(1)).findById(1L);
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    @Test
    void testUpdateStockInsufficient() {
        // Given
        produit.setStock(5);
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));

        // When & Then
        assertThrows(RuntimeException.class, () -> produitService.updateStock(1L, 10));
        verify(produitRepository, times(1)).findById(1L);
        verify(produitRepository, never()).save(any(Produit.class));
    }

    @Test
    void testSearchProduits() {
        // Given
        List<Produit> produits = Arrays.asList(produit);
        when(produitRepository.searchProduits("Test")).thenReturn(produits);

        // When
        List<Produit> result = produitService.searchProduits("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(produitRepository, times(1)).searchProduits("Test");
    }

    @Test
    void testGetProduitsEnStock() {
        // Given
        List<Produit> produits = Arrays.asList(produit);
        when(produitRepository.findByStockGreaterThan(0)).thenReturn(produits);

        // When
        List<Produit> result = produitService.getProduitsEnStock();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(produitRepository, times(1)).findByStockGreaterThan(0);
    }
}

