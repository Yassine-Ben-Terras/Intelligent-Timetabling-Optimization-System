package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.AffectationPayloadDto;
import com.Session.EmploiTempsV1.emploi.dto.AffectationResponseDto;
import com.Session.EmploiTempsV1.emploi.dto.AffectationSyncRequestDto;
import com.Session.EmploiTempsV1.emploi.service.AffectationEnseignantService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affectations")
public class AffectationEnseignantController {

    private final AffectationEnseignantService affectationService;

    public AffectationEnseignantController(AffectationEnseignantService affectationService) {
        this.affectationService = affectationService;
    }

    @GetMapping
    public ResponseEntity<List<AffectationResponseDto>> getAllAffectations() {
        return ResponseEntity.ok(affectationService.findAll());
    }

    /**
     * NOUVEL ENDPOINT pour créer une affectation.
     * Correspond à la méthode 'createAffectation' du frontend.
     * URL: POST /api/affectations
     */
    @PostMapping
    public ResponseEntity<AffectationResponseDto> createAffectation(@Valid @RequestBody AffectationPayloadDto payload) {
        AffectationResponseDto affectationCreee = affectationService.create(payload);
        return new ResponseEntity<>(affectationCreee, HttpStatus.CREATED);
    }

    /**
     * NOUVEL ENDPOINT pour supprimer une affectation.
     * Correspond à la méthode 'deleteAffectation' du frontend.
     * URL: DELETE /api/affectations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffectation(@PathVariable Long id) {
        affectationService.deleteById(id);
        return ResponseEntity.noContent().build(); // Réponse HTTP 204 No Content
    }

    @PostMapping("/synchroniser")
    public ResponseEntity<Void> synchroniserAffectations(@Valid @RequestBody AffectationSyncRequestDto dto) {
        affectationService.synchroniserAffectations(dto);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({EntityNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBusinessExceptions(RuntimeException ex) {
        HttpStatus status = (ex instanceof EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ex.getMessage(), status);
    }
}