package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.service.FactureService;
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

@WebMvcTest(FactureController.class)
class FactureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FactureService factureService;

    @Autowired
    private ObjectMapper objectMapper;

    private Facture facture;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        
        facture = new Facture();
        facture.setId(1L);
        facture.setNumeroFacture("FAC-00001");
        facture.setDate(LocalDate.now());
        facture.setStatut(Facture.StatutFacture.EMISE);
        facture.setMontantHt(new BigDecimal("200.00"));
        facture.setMontantTva(new BigDecimal("40.00"));
        facture.setMontantTtc(new BigDecimal("240.00"));
    }

    @Test
    void testGetAllFactures() throws Exception {
        // Given
        List<Facture> factures = Arrays.asList(facture);
        when(factureService.getAllFactures()).thenReturn(factures);

        // When & Then
        mockMvc.perform(get("/api/factures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].numeroFacture").value("FAC-00001"));

        verify(factureService, times(1)).getAllFactures();
    }

    @Test
    void testGetFactureById() throws Exception {
        // Given
        when(factureService.getFactureById(1L)).thenReturn(Optional.of(facture));

        // When & Then
        mockMvc.perform(get("/api/factures/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroFacture").value("FAC-00001"));

        verify(factureService, times(1)).getFactureById(1L);
    }

    @Test
    void testCreateFacture() throws Exception {
        // Given
        Facture newFacture = new Facture();
        newFacture.setDate(LocalDate.now());
        when(factureService.createFacture(any(Facture.class))).thenReturn(newFacture);

        // When & Then
        mockMvc.perform(post("/api/factures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFacture)))
                .andExpect(status().isCreated());

        verify(factureService, times(1)).createFacture(any(Facture.class));
    }

    @Test
    void testMarquerCommePayee() throws Exception {
        // Given
        facture.setStatut(Facture.StatutFacture.PAYEE);
        when(factureService.marquerCommePayee(1L)).thenReturn(facture);

        // When & Then
        mockMvc.perform(put("/api/factures/1/payer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PAYEE"));

        verify(factureService, times(1)).marquerCommePayee(1L);
    }

    @Test
    void testGetFacturesByClient() throws Exception {
        // Given
        List<Facture> factures = Arrays.asList(facture);
        when(factureService.getFacturesByClient(1L)).thenReturn(factures);

        // When & Then
        mockMvc.perform(get("/api/factures/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(factureService, times(1)).getFacturesByClient(1L);
    }
}

