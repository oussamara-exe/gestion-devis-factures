package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Devis;
import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.service.DevisService;
import com.entreprise.gestion.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DevisController {
    
    private final DevisService devisService;
    private final PdfService pdfService;
    
    @GetMapping
    public ResponseEntity<List<Devis>> getAllDevis() {
        return ResponseEntity.ok(devisService.getAllDevis());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Devis> getDevisById(@PathVariable Long id) {
        return devisService.getDevisById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createDevis(@RequestBody Devis devis) {
        try {
            Devis createdDevis = devisService.createDevis(devis);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevis);
        } catch (RuntimeException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Devis> updateDevis(@PathVariable Long id, @RequestBody Devis devis) {
        try {
            Devis updatedDevis = devisService.updateDevis(id, devis);
            return ResponseEntity.ok(updatedDevis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevis(@PathVariable Long id) {
        try {
            devisService.deleteDevis(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerDevis(@PathVariable Long id) {
        try {
            Devis devis = devisService.validerDevis(id);
            return ResponseEntity.ok(devis);
        } catch (RuntimeException e) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/convertir-facture")
    public ResponseEntity<Facture> convertirDevisEnFacture(@PathVariable Long id) {
        try {
            Facture facture = devisService.convertirDevisEnFacture(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            Devis devis = devisService.getDevisById(id)
                    .orElseThrow(() -> new RuntimeException("Devis non trouvé"));
            
            byte[] pdfBytes = pdfService.generateDevisPdf(devis);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "devis-" + devis.getNumeroDevis() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Devis>> getDevisByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(devisService.getDevisByClient(clientId));
    }
}

