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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactureServiceTest {

    @Mock
    private FactureRepository factureRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private ProduitService produitService;

    @InjectMocks
    private FactureService factureService;

    private Client client;
    private Produit produit;
    private Facture facture;
    private FactureDetail factureDetail;

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

        facture = new Facture();
        facture.setId(1L);
        facture.setNumeroFacture("FAC-00001");
        facture.setClient(client);
        facture.setDate(LocalDate.now());
        facture.setStatut(Facture.StatutFacture.EMISE);

        factureDetail = new FactureDetail();
        factureDetail.setId(1L);
        factureDetail.setFacture(facture);
        factureDetail.setProduit(produit);
        factureDetail.setQuantite(2);
        factureDetail.setPrixUnitaire(new BigDecimal("100.00"));
        factureDetail.setTauxTva(new BigDecimal("20.00"));
        factureDetail.setMontantHt(new BigDecimal("200.00"));
        factureDetail.setMontantTva(new BigDecimal("40.00"));
        factureDetail.setMontantTtc(new BigDecimal("240.00"));

        facture.setDetails(new ArrayList<>(Arrays.asList(factureDetail)));
        facture.setMontantHt(new BigDecimal("200.00"));
        facture.setMontantTva(new BigDecimal("40.00"));
        facture.setMontantTtc(new BigDecimal("240.00"));
    }

    @Test
    void testGetAllFactures() {
        // Given
        List<Facture> factures = Arrays.asList(facture);
        when(factureRepository.findAll()).thenReturn(factures);

        // When
        List<Facture> result = factureService.getAllFactures();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(factureRepository, times(1)).findAll();
    }

    @Test
    void testGetFactureById() {
        // Given
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));

        // When
        Optional<Facture> result = factureService.getFactureById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("FAC-00001", result.get().getNumeroFacture());
        verify(factureRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateFacture() {
        // Given
        Facture newFacture = new Facture();
        newFacture.setClient(client);
        newFacture.setDate(LocalDate.now());
        newFacture.setDetails(Arrays.asList(factureDetail));

        when(factureRepository.findMaxNumeroFacture()).thenReturn(null);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(factureRepository.save(any(Facture.class))).thenAnswer(invocation -> {
            Facture f = invocation.getArgument(0);
            if (f.getDetails() == null) {
                f.setDetails(new ArrayList<>());
            }
            return f;
        });
        doNothing().when(produitService).updateStock(anyLong(), anyInt());

        // When
        Facture result = factureService.createFacture(newFacture);

        // Then
        assertNotNull(result);
        assertNotNull(result.getNumeroFacture());
        verify(factureRepository, times(1)).findMaxNumeroFacture();
        verify(factureRepository, times(1)).save(any(Facture.class));
        verify(produitService, times(1)).updateStock(anyLong(), anyInt());
    }

    @Test
    void testUpdateFacture() {
        // Given
        Facture updatedFacture = new Facture();
        updatedFacture.setClient(client);
        updatedFacture.setDate(LocalDate.now());
        updatedFacture.setDetails(Arrays.asList(factureDetail));

        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(factureRepository.save(any(Facture.class))).thenReturn(updatedFacture);

        // When
        Facture result = factureService.updateFacture(1L, updatedFacture);

        // Then
        assertNotNull(result);
        verify(factureRepository, times(1)).findById(1L);
        verify(factureRepository, times(1)).save(any(Facture.class));
    }

    @Test
    void testUpdateFacturePayee() {
        // Given
        facture.setStatut(Facture.StatutFacture.PAYEE);
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));

        Facture updatedFacture = new Facture();

        // When & Then
        assertThrows(RuntimeException.class, () -> factureService.updateFacture(1L, updatedFacture));
        verify(factureRepository, times(1)).findById(1L);
        verify(factureRepository, never()).save(any(Facture.class));
    }

    @Test
    void testMarquerCommePayee() {
        // Given
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));
        when(factureRepository.save(any(Facture.class))).thenReturn(facture);

        // When
        Facture result = factureService.marquerCommePayee(1L);

        // Then
        assertNotNull(result);
        assertEquals(Facture.StatutFacture.PAYEE, result.getStatut());
        verify(factureRepository, times(1)).findById(1L);
        verify(factureRepository, times(1)).save(any(Facture.class));
    }

    @Test
    void testDeleteFacture() {
        // Given
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));
        doNothing().when(factureRepository).delete(any(Facture.class));

        // When
        factureService.deleteFacture(1L);

        // Then
        verify(factureRepository, times(1)).findById(1L);
        verify(factureRepository, times(1)).delete(any(Facture.class));
    }

    @Test
    void testDeleteFacturePayee() {
        // Given
        facture.setStatut(Facture.StatutFacture.PAYEE);
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));

        // When & Then
        assertThrows(RuntimeException.class, () -> factureService.deleteFacture(1L));
        verify(factureRepository, times(1)).findById(1L);
        verify(factureRepository, never()).delete(any(Facture.class));
    }

    @Test
    void testGetChiffreAffaires() {
        // Given
        Facture facturePayee = new Facture();
        facturePayee.setMontantTtc(new BigDecimal("500.00"));
        facturePayee.setStatut(Facture.StatutFacture.PAYEE);

        List<Facture> facturesPayees = Arrays.asList(facturePayee);
        when(factureRepository.findByStatut(Facture.StatutFacture.PAYEE)).thenReturn(facturesPayees);

        // When
        BigDecimal result = factureService.getChiffreAffaires();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result);
        verify(factureRepository, times(1)).findByStatut(Facture.StatutFacture.PAYEE);
    }

    @Test
    void testGetChiffreAffairesByDate() {
        // Given
        LocalDate dateDebut = LocalDate.now().minusDays(30);
        LocalDate dateFin = LocalDate.now();

        Facture facturePayee = new Facture();
        facturePayee.setMontantTtc(new BigDecimal("300.00"));
        facturePayee.setStatut(Facture.StatutFacture.PAYEE);
        facturePayee.setDate(LocalDate.now());

        List<Facture> factures = Arrays.asList(facturePayee);
        when(factureRepository.findByDateBetween(dateDebut, dateFin)).thenReturn(factures);

        // When
        BigDecimal result = factureService.getChiffreAffairesByDate(dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result);
        verify(factureRepository, times(1)).findByDateBetween(dateDebut, dateFin);
    }

    @Test
    void testGetFacturesByClient() {
        // Given
        List<Facture> factures = Arrays.asList(facture);
        when(factureRepository.findByClientId(1L)).thenReturn(factures);

        // When
        List<Facture> result = factureService.getFacturesByClient(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(factureRepository, times(1)).findByClientId(1L);
    }
}

