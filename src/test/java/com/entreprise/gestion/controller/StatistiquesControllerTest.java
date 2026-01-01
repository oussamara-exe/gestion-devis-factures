package com.entreprise.gestion.controller;

import com.entreprise.gestion.service.FactureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatistiquesController.class)
class StatistiquesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FactureService factureService;

    @Test
    void testGetChiffreAffaires() throws Exception {
        // Given
        BigDecimal ca = new BigDecimal("10000.00");
        when(factureService.getChiffreAffaires()).thenReturn(ca);

        // When & Then
        mockMvc.perform(get("/api/statistiques/ca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiffreAffaires").value(10000.00))
                .andExpect(jsonPath("$.devise").value("EUR"));

        verify(factureService, times(1)).getChiffreAffaires();
    }

    @Test
    void testGetChiffreAffairesByPeriod() throws Exception {
        // Given
        LocalDate dateDebut = LocalDate.now().minusDays(30);
        LocalDate dateFin = LocalDate.now();
        BigDecimal ca = new BigDecimal("5000.00");
        when(factureService.getChiffreAffairesByDate(any(LocalDate.class), any(LocalDate.class))).thenReturn(ca);

        // When & Then
        mockMvc.perform(get("/api/statistiques/ca/period")
                        .param("dateDebut", dateDebut.toString())
                        .param("dateFin", dateFin.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiffreAffaires").value(5000.00))
                .andExpect(jsonPath("$.devise").value("EUR"));

        verify(factureService, times(1)).getChiffreAffairesByDate(any(LocalDate.class), any(LocalDate.class));
    }
}

