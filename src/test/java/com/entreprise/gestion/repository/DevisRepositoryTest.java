package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Client;
import com.entreprise.gestion.model.Devis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DevisRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DevisRepository devisRepository;

    private Client client;
    private Devis devis;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setNom("Client Test");
        client.setEmail("client@example.com");
        client = entityManager.persistAndFlush(client);

        devis = new Devis();
        devis.setNumeroDevis("DEV-00001");
        devis.setClient(client);
        devis.setDate(LocalDate.now());
        devis.setStatut(Devis.StatutDevis.BROUILLON);
        devis = entityManager.persistAndFlush(devis);
    }

    @Test
    void testFindByNumeroDevis() {
        // When
        Optional<Devis> found = devisRepository.findByNumeroDevis("DEV-00001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("DEV-00001", found.get().getNumeroDevis());
    }

    @Test
    void testFindByClientId() {
        // When
        List<Devis> results = devisRepository.findByClientId(client.getId());

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DEV-00001", results.get(0).getNumeroDevis());
    }

    @Test
    void testFindByStatut() {
        // Given
        Devis devis2 = new Devis();
        devis2.setNumeroDevis("DEV-00002");
        devis2.setClient(client);
        devis2.setDate(LocalDate.now());
        devis2.setStatut(Devis.StatutDevis.VALIDE);
        entityManager.persistAndFlush(devis2);

        // When
        List<Devis> results = devisRepository.findByStatut(Devis.StatutDevis.VALIDE);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(Devis.StatutDevis.VALIDE, results.get(0).getStatut());
    }

    @Test
    void testFindByDateBetween() {
        // Given
        LocalDate dateDebut = LocalDate.now().minusDays(5);
        LocalDate dateFin = LocalDate.now().plusDays(5);

        // When
        List<Devis> results = devisRepository.findByDateBetween(dateDebut, dateFin);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testFindMaxNumeroDevis() {
        // Given
        Devis devis2 = new Devis();
        devis2.setNumeroDevis("DEV-00005");
        devis2.setClient(client);
        devis2.setDate(LocalDate.now());
        entityManager.persistAndFlush(devis2);

        // When
        Integer maxNum = devisRepository.findMaxNumeroDevis();

        // Then
        assertNotNull(maxNum);
        assertEquals(5, maxNum);
    }
}

