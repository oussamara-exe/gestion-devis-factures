package com.entreprise.gestion.service;

import com.entreprise.gestion.model.Client;
import com.entreprise.gestion.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Test Client");
        client.setEmail("test@example.com");
        client.setTelephone("0123456789");
        client.setAdresse("123 Rue Test");
        client.setVille("Paris");
        client.setCodePostal("75001");
    }

    @Test
    void testGetAllClients() {
        // Given
        List<Client> clients = Arrays.asList(client);
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<Client> result = clientService.getAllClients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Client", result.get(0).getNom());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void testGetClientById() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        // When
        Optional<Client> result = clientService.getClientById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Client", result.get().getNom());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetClientByIdNotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Client> result = clientService.getClientById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(clientRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateClient() {
        // Given
        Client newClient = new Client();
        newClient.setNom("New Client");
        newClient.setEmail("new@example.com");
        when(clientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);

        // When
        Client result = clientService.createClient(newClient);

        // Then
        assertNotNull(result);
        assertEquals("New Client", result.getNom());
        verify(clientRepository, times(1)).findByEmail("new@example.com");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testCreateClientWithExistingEmail() {
        // Given
        Client newClient = new Client();
        newClient.setEmail("test@example.com");
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(client));

        // When & Then
        assertThrows(RuntimeException.class, () -> clientService.createClient(newClient));
        verify(clientRepository, times(1)).findByEmail("test@example.com");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testUpdateClient() {
        // Given
        Client updatedClient = new Client();
        updatedClient.setNom("Updated Client");
        updatedClient.setEmail("updated@example.com");
        updatedClient.setTelephone("0987654321");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

        // When
        Client result = clientService.updateClient(1L, updatedClient);

        // Then
        assertNotNull(result);
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void testUpdateClientNotFound() {
        // Given
        Client updatedClient = new Client();
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> clientService.updateClient(999L, updatedClient));
        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testDeleteClient() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).delete(any(Client.class));

        // When
        clientService.deleteClient(1L);

        // Then
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).delete(any(Client.class));
    }

    @Test
    void testDeleteClientNotFound() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> clientService.deleteClient(999L));
        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void testSearchClients() {
        // Given
        List<Client> clients = Arrays.asList(client);
        when(clientRepository.searchClients("Test")).thenReturn(clients);

        // When
        List<Client> result = clientService.searchClients("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientRepository, times(1)).searchClients("Test");
    }
}

