package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Client;
import com.entreprise.gestion.model.Facture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FactureRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FactureRepository factureRepository;

    private Client client;
    private Facture facture;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setNom("Client Test");
        client.setEmail("client@example.com");
        client = entityManager.persistAndFlush(client);

        facture = new Facture();
        facture.setNumeroFacture("FAC-00001");
        facture.setClient(client);
        facture.setDate(LocalDate.now());
        facture.setStatut(Facture.StatutFacture.EMISE);
        facture.setMontantHt(new BigDecimal("200.00"));
        facture.setMontantTva(new BigDecimal("40.00"));
        facture.setMontantTtc(new BigDecimal("240.00"));
        facture = entityManager.persistAndFlush(facture);
    }

    @Test
    void testFindByNumeroFacture() {
        // When
        Optional<Facture> found = factureRepository.findByNumeroFacture("FAC-00001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("FAC-00001", found.get().getNumeroFacture());
    }

    @Test
    void testFindByClientId() {
        // When
        List<Facture> results = factureRepository.findByClientId(client.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("FAC-00001", results.get(0).getNumeroFacture());
    }

    @Test
    void testFindByStatut() {
        // Given
        Facture facture2 = new Facture();
        facture2.setNumeroFacture("FAC-00002");
        facture2.setClient(client);
        facture2.setDate(LocalDate.now());
        facture2.setStatut(Facture.StatutFacture.PAYEE);
        facture2.setMontantTtc(new BigDecimal("500.00"));
        entityManager.persistAndFlush(facture2);

        // When
        List<Facture> results = factureRepository.findByStatut(Facture.StatutFacture.PAYEE);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(Facture.StatutFacture.PAYEE, results.get(0).getStatut());
    }

    @Test
    void testFindByDateBetween() {
        // Given
        LocalDate dateDebut = LocalDate.now().minusDays(5);
        LocalDate dateFin = LocalDate.now().plusDays(5);

        // When
        List<Facture> results = factureRepository.findByDateBetween(dateDebut, dateFin);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testFindMaxNumeroFacture() {
        // Given
        Facture facture2 = new Facture();
        facture2.setNumeroFacture("FAC-00010");
        facture2.setClient(client);
        facture2.setDate(LocalDate.now());
        facture2.setStatut(Facture.StatutFacture.EMISE);
        entityManager.persistAndFlush(facture2);

        // When
        Integer maxNum = factureRepository.findMaxNumeroFacture();

        // Then
        assertNotNull(maxNum);
        assertEquals(10, maxNum);
    }

    @Test
    void testSumMontantTtcByStatut() {
        // Given
        Facture facture2 = new Facture();
        facture2.setNumeroFacture("FAC-00002");
        facture2.setClient(client);
        facture2.setDate(LocalDate.now());
        facture2.setStatut(Facture.StatutFacture.PAYEE);
        facture2.setMontantTtc(new BigDecimal("500.00"));
        entityManager.persistAndFlush(facture2);

        // When
        Double sum = factureRepository.sumMontantTtcByStatut(Facture.StatutFacture.PAYEE);

        // Then
        assertNotNull(sum);
        assertEquals(500.00, sum, 0.01);
    }
}

