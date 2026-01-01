package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Client;
import com.entreprise.gestion.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testGetAllClients() throws Exception {
        // Given
        List<Client> clients = Arrays.asList(client);
        when(clientService.getAllClients()).thenReturn(clients);

        // When & Then
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Test Client"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void testGetClientById() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(Optional.of(client));

        // When & Then
        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Test Client"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    void testGetClientByIdNotFound() throws Exception {
        // Given
        when(clientService.getClientById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientById(999L);
    }

    @Test
    void testCreateClient() throws Exception {
        // Given
        Client newClient = new Client();
        newClient.setNom("New Client");
        newClient.setEmail("new@example.com");
        when(clientService.createClient(any(Client.class))).thenReturn(newClient);

        // When & Then
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("New Client"));

        verify(clientService, times(1)).createClient(any(Client.class));
    }

    @Test
    void testCreateClientWithError() throws Exception {
        // Given
        Client newClient = new Client();
        newClient.setNom("New Client");
        when(clientService.createClient(any(Client.class))).thenThrow(new RuntimeException("Email existe déjà"));

        // When & Then
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isBadRequest());

        verify(clientService, times(1)).createClient(any(Client.class));
    }

    @Test
    void testUpdateClient() throws Exception {
        // Given
        Client updatedClient = new Client();
        updatedClient.setNom("Updated Client");
        when(clientService.updateClient(eq(1L), any(Client.class))).thenReturn(updatedClient);

        // When & Then
        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Updated Client"));

        verify(clientService, times(1)).updateClient(eq(1L), any(Client.class));
    }

    @Test
    void testUpdateClientNotFound() throws Exception {
        // Given
        Client updatedClient = new Client();
        updatedClient.setNom("Updated");
        when(clientService.updateClient(eq(999L), any(Client.class)))
                .thenThrow(new RuntimeException("Client non trouvé"));

        // When & Then
        mockMvc.perform(put("/api/clients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).updateClient(eq(999L), any(Client.class));
    }

    @Test
    void testDeleteClient() throws Exception {
        // Given
        doNothing().when(clientService).deleteClient(1L);

        // When & Then
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    void testDeleteClientNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Client non trouvé")).when(clientService).deleteClient(999L);

        // When & Then
        mockMvc.perform(delete("/api/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).deleteClient(999L);
    }

    @Test
    void testSearchClients() throws Exception {
        // Given
        List<Client> clients = Arrays.asList(client);
        when(clientService.searchClients("Test")).thenReturn(clients);

        // When & Then
        mockMvc.perform(get("/api/clients/search")
                        .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Test Client"));

        verify(clientService, times(1)).searchClients("Test");
    }
}

