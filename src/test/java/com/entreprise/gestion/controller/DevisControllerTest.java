package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.*;
import com.entreprise.gestion.service.DevisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevisController.class)
class DevisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DevisService devisService;

    @Autowired
    private ObjectMapper objectMapper;

    private Devis devis;
    private Client client;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        
        client = new Client();
        client.setId(1L);
        client.setNom("Client Test");

        devis = new Devis();
        devis.setId(1L);
        devis.setNumeroDevis("DEV-00001");
        devis.setClient(client);
        devis.setDate(LocalDate.now());
        devis.setStatut(Devis.StatutDevis.BROUILLON);
        devis.setTotalHt(new BigDecimal("200.00"));
        devis.setTotalTva(new BigDecimal("40.00"));
        devis.setTotalTtc(new BigDecimal("240.00"));
    }

    @Test
    void testGetAllDevis() throws Exception {
        // Given
        List<Devis> devisList = Arrays.asList(devis);
        when(devisService.getAllDevis()).thenReturn(devisList);

        // When & Then
        mockMvc.perform(get("/api/devis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].numeroDevis").value("DEV-00001"));

        verify(devisService, times(1)).getAllDevis();
    }

    @Test
    void testGetDevisById() throws Exception {
        // Given
        when(devisService.getDevisById(1L)).thenReturn(Optional.of(devis));

        // When & Then
        mockMvc.perform(get("/api/devis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroDevis").value("DEV-00001"));

        verify(devisService, times(1)).getDevisById(1L);
    }

    @Test
    void testCreateDevis() throws Exception {
        // Given
        Devis newDevis = new Devis();
        newDevis.setClient(client);
        newDevis.setDate(LocalDate.now());
        when(devisService.createDevis(any(Devis.class))).thenReturn(newDevis);

        // When & Then
        mockMvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDevis)))
                .andExpect(status().isCreated());

        verify(devisService, times(1)).createDevis(any(Devis.class));
    }

    @Test
    void testValiderDevis() throws Exception {
        // Given
        devis.setStatut(Devis.StatutDevis.VALIDE);
        when(devisService.validerDevis(1L)).thenReturn(devis);

        // When & Then
        mockMvc.perform(put("/api/devis/1/valider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));

        verify(devisService, times(1)).validerDevis(1L);
    }

    @Test
    void testConvertirDevisEnFacture() throws Exception {
        // Given
        Facture facture = new Facture();
        facture.setId(1L);
        facture.setNumeroFacture("FAC-00001");
        when(devisService.convertirDevisEnFacture(1L)).thenReturn(facture);

        // When & Then
        mockMvc.perform(post("/api/devis/1/convertir-facture"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFacture").value("FAC-00001"));

        verify(devisService, times(1)).convertirDevisEnFacture(1L);
    }

    @Test
    void testDeleteDevis() throws Exception {
        // Given
        doNothing().when(devisService).deleteDevis(1L);

        // When & Then
        mockMvc.perform(delete("/api/devis/1"))
                .andExpect(status().isNoContent());

        verify(devisService, times(1)).deleteDevis(1L);
    }
}

