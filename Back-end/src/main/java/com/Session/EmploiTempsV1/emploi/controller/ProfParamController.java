// FICHIER MODIFIÉ : ProfParamController.java
package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.ProfParamDto;
import com.Session.EmploiTempsV1.emploi.service.ProfParamService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prof-params")
public class ProfParamController {

    private final ProfParamService service;

    public ProfParamController(ProfParamService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProfParamDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // Pas de changement ici, le nouveau DTO est géré par @RequestBody
    @PostMapping
    public ResponseEntity<ProfParamDto> create(@RequestBody ProfParamDto dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    // MODIFIÉ : L'URL de suppression est plus spécifique et inclut le typeSeanceId
    @DeleteMapping("/professeurs/{professeurId}/modules/{moduleId}/types-seance/{typeSeanceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer professeurId,
            @PathVariable Integer moduleId,
            @PathVariable Integer typeSeanceId) { // MODIFIÉ : Ajout du nouveau paramètre

        // MODIFIÉ : Appel de la nouvelle méthode de service
        service.delete(professeurId, moduleId, typeSeanceId);
        return ResponseEntity.noContent().build();
    }

    // Pas de changement ici, la gestion des exceptions reste la même
    @ExceptionHandler({EntityNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleExceptions(RuntimeException ex) {
        HttpStatus status = (ex instanceof EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ex.getMessage(), status);
    }
}