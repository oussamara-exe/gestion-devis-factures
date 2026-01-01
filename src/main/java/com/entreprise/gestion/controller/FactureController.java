package com.entreprise.gestion.controller;

import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.service.FactureService;
import com.entreprise.gestion.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FactureController {
    
    private final FactureService factureService;
    private final PdfService pdfService;
    
    @GetMapping
    public ResponseEntity<List<Facture>> getAllFactures() {
        return ResponseEntity.ok(factureService.getAllFactures());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Facture> getFactureById(@PathVariable Long id) {
        return factureService.getFactureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Facture> createFacture(@RequestBody Facture facture) {
        try {
            Facture createdFacture = factureService.createFacture(facture);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFacture);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Facture> updateFacture(@PathVariable Long id, @RequestBody Facture facture) {
        try {
            Facture updatedFacture = factureService.updateFacture(id, facture);
            return ResponseEntity.ok(updatedFacture);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureService.deleteFacture(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/payer")
    public ResponseEntity<Facture> marquerCommePayee(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerCommePayee(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Facture>> getFacturesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(factureService.getFacturesByClient(clientId));
    }
    
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            Facture facture = factureService.getFactureById(id)
                    .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
            
            byte[] pdfBytes = pdfService.generateFacturePdf(facture);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "facture-" + facture.getNumeroFacture() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

