// FICHIER : com/Session/EmploiTempsV1/emploi/grpSerV6/TimetableControllerV6.java
package com.Session.EmploiTempsV1.emploi.orTools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/timetable/v6")
public class TimetableControllerV6 {

    private final TimetableServiceV6 timetableService;
    private static final Logger LOGGER = Logger.getLogger(TimetableControllerV6.class.getName());

    @Autowired
    public TimetableControllerV6(@Qualifier("timetableServiceV6") TimetableServiceV6 timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generateTimetable(
            @RequestParam("annee") Integer anneeId,
            @RequestParam("session") Integer sessionId) {

        if (anneeId == null || sessionId == null) {
            return ResponseEntity.badRequest().body("Les paramètres 'annee' (ID) et 'session' (ID) sont obligatoires.");
        }

        LOGGER.log(Level.INFO, "Requête reçue pour générer l'emploi du temps pour : Année ID {0}, Session ID {1}",
                new Object[]{anneeId, sessionId});

        try {
            timetableService.generateAndStoreTimetable(anneeId, sessionId);
            String successMessage = "L'emploi du temps  a été généré avec succès .";
            LOGGER.info(successMessage);
            return ResponseEntity.ok(successMessage);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'exécution : " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la génération: " + e.getMessage());
        }
    }
}