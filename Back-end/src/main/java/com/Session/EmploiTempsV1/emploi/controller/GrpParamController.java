package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.GrpParamCreateDto; // MODIFIÉ
import com.Session.EmploiTempsV1.emploi.dto.GrpParamDto;       // MODIFIÉ
import com.Session.EmploiTempsV1.emploi.service.GrpParamService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid; // Pour la validation
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/grp-params")
public class GrpParamController {
    private final GrpParamService service;
    public GrpParamController(GrpParamService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<GrpParamDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // MODIFIÉ : La méthode attend maintenant un GrpParamCreateDto
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody GrpParamCreateDto createDto) {
        try {
            GrpParamDto createdDto = service.create(createDto);
            return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 409 Conflict est plus approprié pour un doublon
        }
    }

    @DeleteMapping("/sections/{sectionId}/groupes/{groupeId}/annees/{anneeId}/semestres/{semestreId}")
    public ResponseEntity<Void> delete(@PathVariable Integer sectionId, @PathVariable Integer groupeId, @PathVariable Integer anneeId, @PathVariable Integer semestreId){
        service.delete(sectionId, groupeId, anneeId, semestreId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({EntityNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleExceptions(RuntimeException ex) {
        HttpStatus status = (ex instanceof EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ex.getMessage(), status);
    }
}