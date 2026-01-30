package com.entreprise.gestion.service;

import com.entreprise.gestion.model.*;
import com.entreprise.gestion.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevisServiceTest {

    @Mock
    private DevisRepository devisRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private FactureRepository factureRepository;

    @Mock
    private ProduitService produitService;

    @InjectMocks
    private DevisService devisService;

    private Client client;
    private Produit produit;
    private Devis devis;
    private DevisDetail devisDetail;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Client Test");

        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Produit Test");
        produit.setPrixUnitaire(new BigDecimal("100.00"));
        produit.setTauxTva(new BigDecimal("20.00"));
        produit.setStock(100);

        devis = new Devis();
        devis.setId(1L);
        devis.setNumeroDevis("DEV-00001");
        devis.setClient(client);
        devis.setDate(LocalDate.now());
        devis.setStatut(Devis.StatutDevis.BROUILLON);

        devisDetail = new DevisDetail();
        devisDetail.setId(1L);
        devisDetail.setDevis(devis);
        devisDetail.setProduit(produit);
        devisDetail.setQuantite(2);
        devisDetail.setPrixUnitaire(new BigDecimal("100.00"));
        devisDetail.setTauxTva(new BigDecimal("20.00"));
        devisDetail.setMontantHt(new BigDecimal("200.00"));
        devisDetail.setMontantTva(new BigDecimal("40.00"));
        devisDetail.setMontantTtc(new BigDecimal("240.00"));

        devis.setDetails(new ArrayList<>(Arrays.asList(devisDetail)));
        devis.setTotalHt(new BigDecimal("200.00"));
        devis.setTotalTva(new BigDecimal("40.00"));
        devis.setTotalTtc(new BigDecimal("240.00"));
    }

    @Test
    void testGetAllDevis() {
        // Given
        List<Devis> devisList = Arrays.asList(devis);
        when(devisRepository.findAll()).thenReturn(devisList);

        // When
        List<Devis> result = devisService.getAllDevis();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(devisRepository, times(1)).findAll();
    }

    @Test
    void testCreateDevis() {
        // Given
        Devis newDevis = new Devis();
        newDevis.setClient(client);
        newDevis.setDate(LocalDate.now());
        newDevis.setDetails(new ArrayList<>());

        when(devisRepository.findMaxNumeroDevis()).thenReturn(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(devisRepository.save(any(Devis.class))).thenReturn(newDevis);

        // When
        Devis result = devisService.createDevis(newDevis);

        // Then
        assertNotNull(result);
        assertNotNull(result.getNumeroDevis());
        verify(devisRepository, times(1)).findMaxNumeroDevis();
        verify(devisRepository, times(1)).save(any(Devis.class));
    }

    @Test
    void testCreateDevisWithClientNotFound() {
        // Given
        Devis newDevis = new Devis();
        newDevis.setClient(new Client());
        newDevis.getClient().setId(999L);
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> devisService.createDevis(newDevis));
        verify(clientRepository, times(1)).findById(999L);
        verify(devisRepository, never()).save(any(Devis.class));
    }

    @Test
    void testUpdateDevis() {
        // Given
        Devis updatedDevis = new Devis();
        updatedDevis.setClient(client);
        updatedDevis.setDate(LocalDate.now());
        updatedDevis.setDetails(Arrays.asList(devisDetail));

        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(devisRepository.save(any(Devis.class))).thenReturn(updatedDevis);

        // When
        Devis result = devisService.updateDevis(1L, updatedDevis);

        // Then
        assertNotNull(result);
        verify(devisRepository, times(1)).findById(1L);
        verify(devisRepository, times(1)).save(any(Devis.class));
    }

    @Test
    void testValiderDevis() {
        // Given
        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));
        when(devisRepository.save(any(Devis.class))).thenReturn(devis);

        // When
        Devis result = devisService.validerDevis(1L);

        // Then
        assertNotNull(result);
        assertEquals(Devis.StatutDevis.VALIDE, result.getStatut());
        verify(devisRepository, times(1)).findById(1L);
        verify(devisRepository, times(1)).save(any(Devis.class));
    }

    @Test
    void testValiderDevisWithoutDetails() {
        // Given
        devis.setDetails(new ArrayList<>());
        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));

        // When & Then
        assertThrows(RuntimeException.class, () -> devisService.validerDevis(1L));
        verify(devisRepository, times(1)).findById(1L);
        verify(devisRepository, never()).save(any(Devis.class));
    }

    @Test
    void testConvertirDevisEnFacture() {
        // Given
        devis.setStatut(Devis.StatutDevis.VALIDE);
        Facture facture = new Facture();
        facture.setId(1L);
        facture.setNumeroFacture("FAC-00001");

        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));
        when(factureRepository.findMaxNumeroFacture()).thenReturn(null);
        when(factureRepository.save(any(Facture.class))).thenReturn(facture);
        when(devisRepository.save(any(Devis.class))).thenReturn(devis);
        doNothing().when(produitService).updateStock(anyLong(), anyInt());

        // When
        Facture result = devisService.convertirDevisEnFacture(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getNumeroFacture());
        verify(devisRepository, times(1)).findById(1L);
        verify(factureRepository, times(1)).save(any(Facture.class));
        verify(produitService, times(1)).updateStock(anyLong(), anyInt());
    }

    @Test
    void testConvertirDevisEnFactureAlreadyConverted() {
        // Given
        devis.setStatut(Devis.StatutDevis.VALIDE);
        Facture existingFacture = new Facture();
        devis.setFacture(existingFacture);
        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));

        // When & Then
        assertThrows(RuntimeException.class, () -> devisService.convertirDevisEnFacture(1L));
        verify(devisRepository, times(1)).findById(1L);
        verify(factureRepository, never()).save(any(Facture.class));
    }

    @Test
    void testConvertirDevisEnFactureInvalidStatus() {
        // Given
        devis.setStatut(Devis.StatutDevis.BROUILLON);
        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));

        // When & Then
        assertThrows(RuntimeException.class, () -> devisService.convertirDevisEnFacture(1L));
        verify(devisRepository, times(1)).findById(1L);
        verify(factureRepository, never()).save(any(Facture.class));
    }

    @Test
    void testDeleteDevis() {
        // Given
        when(devisRepository.findById(1L)).thenReturn(Optional.of(devis));
        doNothing().when(devisRepository).delete(any(Devis.class));

        // When
        devisService.deleteDevis(1L);

        // Then
        verify(devisRepository, times(1)).findById(1L);
        verify(devisRepository, times(1)).delete(any(Devis.class));
    }

    @Test
    void testGenerateNumeroDevis() {
        // Given
        Devis newDevis = new Devis();
        newDevis.setClient(client);
        newDevis.setDate(LocalDate.now());
        newDevis.setDetails(new ArrayList<>());

        when(devisRepository.findMaxNumeroDevis()).thenReturn(5);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(devisRepository.save(any(Devis.class))).thenAnswer(invocation -> {
            Devis d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        // When
        Devis result = devisService.createDevis(newDevis);

        // Then
        assertNotNull(result);
        assertNotNull(result.getNumeroDevis());
        assertTrue(result.getNumeroDevis().startsWith("DEV-"));
        verify(devisRepository, times(1)).findMaxNumeroDevis();
    }
}

