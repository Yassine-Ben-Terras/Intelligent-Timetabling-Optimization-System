package com.Session.EmploiTempsV1.emploi.controller;


import com.Session.EmploiTempsV1.emploi.dto.ArchivePDFDTO;
import com.Session.EmploiTempsV1.emploi.entities.ArchivePDF;
import com.Session.EmploiTempsV1.emploi.service.ArchivePDFService;
import jakarta.persistence.Lob;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdfs")
public class ArchivePDFController {

    private final ArchivePDFService archivePDFService;

    public ArchivePDFController(ArchivePDFService archivePDFService) {
        this.archivePDFService = archivePDFService;
    }

    @GetMapping("/generate-all-filiere-semestre")
    public ResponseEntity<byte[]> generateAllFiliereSemestresPdf() {
        byte[] pdfData = archivePDFService.generateAllFiliereSemestresPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "all-filiere-semestres.pdf");
        headers.setContentLength(pdfData.length);
        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

    // Endpoint for uploading a PDF
    @PostMapping("/upload")
    public ResponseEntity<ArchivePDFDTO> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot upload empty file.");
        }
        if (!MediaType.APPLICATION_PDF.toString().equals(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed.");
        }
        try {
            ArchivePDFDTO storedPdf = archivePDFService.storePdf(file);
            return new ResponseEntity<>(storedPdf, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the PDF file.", e);
        }
    }

    // Endpoint for downloading a PDF by ID
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Optional<ArchivePDF> pdfOptional = archivePDFService.getPdfById(id);
        if (pdfOptional.isPresent()) {
            ArchivePDF pdf = pdfOptional.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(pdf.getContentType()));
            headers.setContentDispositionFormData("attachment", pdf.getFileName()); // "inline" to display in browser
            headers.setContentLength(pdf.getPdfData().length);
            return new ResponseEntity<>(pdf.getPdfData(), headers, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF not found with ID: " + id);
        }
    }

    // Endpoint for downloading a PDF by file name
    @GetMapping("/download/by-name/{fileName}")
    public ResponseEntity<byte[]> downloadPdfByName(@PathVariable String fileName) {
        Optional<ArchivePDF> pdfOptional = archivePDFService.getPdfByFileName(fileName);
        if (pdfOptional.isPresent()) {
            ArchivePDF pdf = pdfOptional.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(pdf.getContentType()));
            headers.setContentDispositionFormData("attachment", pdf.getFileName());
            headers.setContentLength(pdf.getPdfData().length);
            return new ResponseEntity<>(pdf.getPdfData(), headers, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF not found with name: " + fileName);
        }
    }

    // Endpoint to get a list of all archived PDFs (metadata only)
    @GetMapping
    public ResponseEntity<List<ArchivePDFDTO>> getAllPdfsMetadata() {
        List<ArchivePDFDTO> pdfs = archivePDFService.getAllPdfsMetadata();
        return new ResponseEntity<>(pdfs, HttpStatus.OK);
    }

    // Endpoint to delete a PDF by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePdf(@PathVariable Long id) {
        archivePDFService.deletePdf(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}