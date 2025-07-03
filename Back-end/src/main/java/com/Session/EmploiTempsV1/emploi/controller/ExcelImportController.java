package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.service.ExcelDataImporterService; // <-- MODIFIÉ: On appelle le nouveau service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/api/import")
public class ExcelImportController {

    private final ExcelDataImporterService excelDataImporterService; // <-- MODIFIÉ

    @Autowired
    public ExcelImportController(ExcelDataImporterService excelDataImporterService) { // <-- MODIFIÉ
        this.excelDataImporterService = excelDataImporterService;
    }

    @PostMapping("/excel")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Veuillez sélectionner un fichier à téléverser."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Format de fichier invalide. Seuls les fichiers .xlsx et .xls sont autorisés."));
        }

        try {
            // On appelle la nouvelle méthode du nouveau service
            excelDataImporterService.importDataFromExcel(file.getInputStream(), originalFilename);

            return ResponseEntity.ok(Map.of("message", "Fichier '" + originalFilename + "' traité et données importées avec succès."));
        } catch (Exception e) {
            e.printStackTrace(); // Très important pour le débogage côté serveur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Une erreur est survenue lors de l'importation: " + e.getMessage()));
        }
    }
}