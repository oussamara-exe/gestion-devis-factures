package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setNom("Test Client");
        client.setEmail("test@example.com");
        client.setTelephone("0123456789");
        client.setAdresse("123 Rue Test");
        client.setVille("Paris");
        client.setCodePostal("75001");
        client = entityManager.persistAndFlush(client);
    }

    @Test
    void testFindById() {
        // When
        Optional<Client> found = clientRepository.findById(client.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Client", found.get().getNom());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        // When
        Optional<Client> found = clientRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Client", found.get().getNom());
    }

    @Test
    void testFindByEmailNotFound() {
        // When
        Optional<Client> found = clientRepository.findByEmail("notfound@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testSearchClients() {
        // Given
        Client client2 = new Client();
        client2.setNom("Autre Client");
        client2.setEmail("autre@example.com");
        entityManager.persistAndFlush(client2);

        // When
        List<Client> results = clientRepository.searchClients("Test");

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Client", results.get(0).getNom());
    }

    @Test
    void testSaveClient() {
        // Given
        Client newClient = new Client();
        newClient.setNom("New Client");
        newClient.setEmail("new@example.com");

        // When
        Client saved = clientRepository.save(newClient);
        entityManager.flush();

        // Then
        assertNotNull(saved.getId());
        Optional<Client> found = clientRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("New Client", found.get().getNom());
    }

    @Test
    void testDeleteClient() {
        // When
        clientRepository.delete(client);
        entityManager.flush();

        // Then
        Optional<Client> found = clientRepository.findById(client.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        // Given
        Client client2 = new Client();
        client2.setNom("Client 2");
        client2.setEmail("client2@example.com");
        entityManager.persistAndFlush(client2);

        // When
        List<Client> all = clientRepository.findAll();

        // Then
        assertEquals(2, all.size());
    }
}

