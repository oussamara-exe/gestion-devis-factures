package com.entreprise.gestion.service;

import com.entreprise.gestion.model.Devis;
import com.entreprise.gestion.model.DevisDetail;
import com.entreprise.gestion.model.Facture;
import com.entreprise.gestion.model.FactureDetail;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {
    
    public byte[] generateFacturePdf(Facture facture) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // En-tête
            Paragraph title = new Paragraph("FACTURE")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // Informations de la facture
            Paragraph numero = new Paragraph("N° " + facture.getNumeroFacture())
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10);
            document.add(numero);
            
            // Informations client
            if (facture.getClient() != null) {
                Paragraph clientTitle = new Paragraph("Client:")
                        .setBold()
                        .setMarginTop(20)
                        .setMarginBottom(5);
                document.add(clientTitle);
                
                Paragraph clientInfo = new Paragraph(
                        facture.getClient().getNom() + "\n" +
                        (facture.getClient().getAdresse() != null ? facture.getClient().getAdresse() + "\n" : "") +
                        (facture.getClient().getCodePostal() != null && facture.getClient().getVille() != null ?
                         facture.getClient().getCodePostal() + " " + facture.getClient().getVille() + "\n" : "") +
                        (facture.getClient().getEmail() != null ? facture.getClient().getEmail() + "\n" : "") +
                        (facture.getClient().getTelephone() != null ? facture.getClient().getTelephone() : "")
                )
                .setMarginBottom(20);
                document.add(clientInfo);
            }
            
            // Date et échéance
            Paragraph dateInfo = new Paragraph(
                    "Date: " + facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    (facture.getDateEcheance() != null ? 
                     "\nÉchéance: " + facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
            )
            .setMarginBottom(20);
            document.add(dateInfo);
            
            // Tableau des produits
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1.5f, 1.5f, 1.5f, 1.5f}))
                    .useAllAvailableWidth()
                    .setMarginTop(20)
                    .setMarginBottom(20);
            
            // En-têtes du tableau
            table.addHeaderCell(new Cell().add(new Paragraph("Produit").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Qté").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix unitaire").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("TVA %").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Montant HT").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Montant TTC").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            // Lignes de produits
            if (facture.getDetails() != null) {
                for (FactureDetail detail : facture.getDetails()) {
                    table.addCell(new Cell().add(new Paragraph(detail.getProduit() != null ? detail.getProduit().getNom() : "")));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantite()))));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getPrixUnitaire()) + " MAD")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getTauxTva()) + " %")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getMontantHt()) + " MAD")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getMontantTtc()) + " MAD")));
                }
            }
            
            document.add(table);
            
            // Totaux
            Paragraph totalHt = new Paragraph("Total HT: " + formatDecimal(facture.getMontantHt()) + " MAD")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10);
            document.add(totalHt);
            
            Paragraph totalTva = new Paragraph("Total TVA: " + formatDecimal(facture.getMontantTva()) + " MAD")
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(totalTva);
            
            Paragraph totalTtc = new Paragraph("Total TTC: " + formatDecimal(facture.getMontantTtc()) + " MAD")
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(5)
                    .setMarginBottom(20);
            document.add(totalTtc);
            
            // Statut et mode de paiement
            if (facture.getStatut() != null) {
                Paragraph statut = new Paragraph("Statut: " + facture.getStatut())
                        .setMarginTop(20);
                document.add(statut);
            }
            
            if (facture.getModePaiement() != null) {
                Paragraph paiement = new Paragraph("Mode de paiement: " + facture.getModePaiement())
                        .setMarginTop(5);
                document.add(paiement);
            }
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
        
        return baos.toByteArray();
    }
    
    public byte[] generateDevisPdf(Devis devis) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // En-tête
            Paragraph title = new Paragraph("DEVIS")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // Informations du devis
            Paragraph numero = new Paragraph("N° " + devis.getNumeroDevis())
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10);
            document.add(numero);
            
            // Informations client
            if (devis.getClient() != null) {
                Paragraph clientTitle = new Paragraph("Client:")
                        .setBold()
                        .setMarginTop(20)
                        .setMarginBottom(5);
                document.add(clientTitle);
                
                Paragraph clientInfo = new Paragraph(
                        devis.getClient().getNom() + "\n" +
                        (devis.getClient().getAdresse() != null ? devis.getClient().getAdresse() + "\n" : "") +
                        (devis.getClient().getCodePostal() != null && devis.getClient().getVille() != null ?
                         devis.getClient().getCodePostal() + " " + devis.getClient().getVille() + "\n" : "") +
                        (devis.getClient().getEmail() != null ? devis.getClient().getEmail() + "\n" : "") +
                        (devis.getClient().getTelephone() != null ? devis.getClient().getTelephone() : "")
                )
                .setMarginBottom(20);
                document.add(clientInfo);
            }
            
            // Date et statut
            Paragraph dateInfo = new Paragraph(
                    "Date: " + devis.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    "\nStatut: " + devis.getStatut()
            )
            .setMarginBottom(20);
            document.add(dateInfo);
            
            // Tableau des produits
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1.5f, 1.5f, 1.5f, 1.5f}))
                    .useAllAvailableWidth()
                    .setMarginTop(20)
                    .setMarginBottom(20);
            
            // En-têtes du tableau
            table.addHeaderCell(new Cell().add(new Paragraph("Produit").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Qté").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix unitaire").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("TVA %").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Montant HT").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Montant TTC").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            
            // Lignes de produits
            if (devis.getDetails() != null) {
                for (DevisDetail detail : devis.getDetails()) {
                    table.addCell(new Cell().add(new Paragraph(detail.getProduit() != null ? detail.getProduit().getNom() : "")));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantite()))));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getPrixUnitaire()) + " MAD")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getTauxTva()) + " %")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getMontantHt()) + " MAD")));
                    table.addCell(new Cell().add(new Paragraph(formatDecimal(detail.getMontantTtc()) + " MAD")));
                }
            }
            
            document.add(table);
            
            // Totaux
            Paragraph totalHt = new Paragraph("Total HT: " + formatDecimal(devis.getTotalHt()) + " MAD")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10);
            document.add(totalHt);
            
            Paragraph totalTva = new Paragraph("Total TVA: " + formatDecimal(devis.getTotalTva()) + " MAD")
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(totalTva);
            
            Paragraph totalTtc = new Paragraph("Total TTC: " + formatDecimal(devis.getTotalTtc()) + " MAD")
                    .setBold()
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(5)
                    .setMarginBottom(20);
            document.add(totalTtc);
            
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
        
        return baos.toByteArray();
    }
    
    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%.2f", value);
    }
}

