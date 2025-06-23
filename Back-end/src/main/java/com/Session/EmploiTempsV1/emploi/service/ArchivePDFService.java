package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.ArchivePDFDTO;
import com.Session.EmploiTempsV1.emploi.entities.ArchivePDF;
import com.Session.EmploiTempsV1.emploi.repository.ArchivePDFRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
// ou Apache PDFBox si tu préfères

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArchivePDFService {

    private final ArchivePDFRepository archivePDFRepository;

    public ArchivePDFService(ArchivePDFRepository archivePDFRepository) {
        this.archivePDFRepository = archivePDFRepository;
    }

    // Save a PDF
    public ArchivePDFDTO storePdf(MultipartFile file) throws IOException {
        ArchivePDF archivePDF = new ArchivePDF();
        archivePDF.setFileName(file.getOriginalFilename());
        archivePDF.setContentType(file.getContentType());
        archivePDF.setPdfData(file.getBytes());
        ArchivePDF savedPdf = archivePDFRepository.save(archivePDF);
        return mapToDTO(savedPdf);
    }

    // Get a PDF by ID
    public Optional<ArchivePDF> getPdfById(Long id) {
        return archivePDFRepository.findById(id);
    }

    // Get a PDF by file name
    public Optional<ArchivePDF> getPdfByFileName(String fileName) {
        return archivePDFRepository.findByFileName(fileName);
    }

    // Get all PDFs (metadata only for list view)
    public List<ArchivePDFDTO> getAllPdfsMetadata() {
        return archivePDFRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Delete a PDF
    public void deletePdf(Long id) {
        archivePDFRepository.deleteById(id);
    }

    private ArchivePDFDTO mapToDTO(ArchivePDF archivePDF) {
        ArchivePDFDTO dto = new ArchivePDFDTO();
        dto.setId(archivePDF.getId());
        dto.setFileName(archivePDF.getFileName());
        dto.setContentType(archivePDF.getContentType());
        return dto;
    }
    public byte[] generateAllFiliereSemestresPdf() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            document.add(new Paragraph("Emplois du temps - Toutes Filières & Semestres",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(Chunk.NEWLINE);

            // TODO : Remplacer par une vraie récupération des données depuis ta base
            // Exemple basique :
            List<String> fakeEntries = List.of(
                    "Informatique - Semestre 1",
                    "Informatique - Semestre 2",
                    "Mathématiques - Semestre 1"
            );

            for (String line : fakeEntries) {
                document.add(new Paragraph(line, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}