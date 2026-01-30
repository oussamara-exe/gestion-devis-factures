package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.repository.ClientRepository;
import com.entreprise.gestion.repository.DevisRepository;
import com.entreprise.gestion.repository.FactureRepository;
import com.entreprise.gestion.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatistiquesController {
    
    private final FactureService factureService;
    private final FactureRepository factureRepository;
    private final DevisRepository devisRepository;
    private final ClientRepository clientRepository;
    
    @GetMapping("/ca")
    public ResponseEntity<Map<String, Object>> getChiffreAffaires() {
        BigDecimal ca = factureService.getChiffreAffaires();
        Map<String, Object> response = new HashMap<>();
        response.put("chiffreAffaires", ca);
        response.put("devise", "MAD");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ca/period")
    public ResponseEntity<Map<String, Object>> getChiffreAffairesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        BigDecimal ca = factureService.getChiffreAffairesByDate(dateDebut, dateFin);
        Map<String, Object> response = new HashMap<>();
        response.put("chiffreAffaires", ca);
        response.put("dateDebut", dateDebut);
        response.put("dateFin", dateFin);
        response.put("devise", "MAD");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Statistiques générales
        long totalClients = clientRepository.count();
        
        long totalFactures = factureRepository.count();
        long totalDevis = devisRepository.count();
        
        // Chiffre d'affaires total
        BigDecimal caTotal = factureService.getChiffreAffaires();
        
        // Factures payées
        long facturesPayees = factureRepository.findByStatut(Facture.StatutFacture.PAYEE).size();
        
        // Factures en retard
        List<Facture> facturesEnRetard = factureRepository.findAll().stream()
                .filter(f -> f.getDateEcheance() != null 
                        && f.getDateEcheance().isBefore(LocalDate.now())
                        && f.getStatut() != Facture.StatutFacture.PAYEE)
                .collect(Collectors.toList());
        
        // Chiffre d'affaires par mois (6 derniers mois)
        List<Map<String, Object>> caParMois = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            
            BigDecimal caMois = factureService.getChiffreAffairesByDate(start, end);
            Map<String, Object> moisData = new HashMap<>();
            moisData.put("mois", month.toString());
            moisData.put("chiffreAffaires", caMois);
            caParMois.add(moisData);
        }
        
        // Répartition par statut de facture
        Map<String, Long> facturesParStatut = factureRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        f -> f.getStatut().toString(),
                        Collectors.counting()
                ));
        
        // Répartition par statut de devis
        Map<String, Long> devisParStatut = devisRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        d -> d.getStatut().toString(),
                        Collectors.counting()
                ));
        
        // Top 5 clients par chiffre d'affaires
        List<Map<String, Object>> topClients = factureRepository.findAll().stream()
                .filter(f -> f.getStatut() == Facture.StatutFacture.PAYEE)
                .collect(Collectors.groupingBy(
                        f -> f.getClient().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                f -> f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Facture firstFacture = factureRepository.findAll().stream()
                            .filter(f -> f.getClient().getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    Map<String, Object> clientData = new HashMap<>();
                    if (firstFacture != null && firstFacture.getClient() != null) {
                        clientData.put("nom", firstFacture.getClient().getNom());
                        clientData.put("chiffreAffaires", entry.getValue());
                    }
                    return clientData;
                })
                .filter(m -> !m.isEmpty())
                .collect(Collectors.toList());
        
        stats.put("totalClients", totalClients);
        stats.put("totalFactures", totalFactures);
        stats.put("totalDevis", totalDevis);
        stats.put("chiffreAffairesTotal", caTotal);
        stats.put("facturesPayees", facturesPayees);
        stats.put("facturesEnRetard", facturesEnRetard.size());
        stats.put("montantEnRetard", facturesEnRetard.stream()
                .map(f -> f.getMontantTtc() != null ? f.getMontantTtc() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("caParMois", caParMois);
        stats.put("facturesParStatut", facturesParStatut);
        stats.put("devisParStatut", devisParStatut);
        stats.put("topClients", topClients);
        
        return ResponseEntity.ok(stats);
    }
}
