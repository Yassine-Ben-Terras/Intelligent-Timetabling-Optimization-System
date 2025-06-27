package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.DispoProfDto;
import com.Session.EmploiTempsV1.emploi.service.DispoProfService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dispo-profs")
public class DispoProfController {

    private final DispoProfService dispoProfService;

    public DispoProfController(DispoProfService dispoProfService) {
        this.dispoProfService = dispoProfService;
    }

    @GetMapping("all")
    public ResponseEntity<List<DispoProfDto>> getAllDispoProfs() {
        return ResponseEntity.ok(dispoProfService.findAll());
    }

    // URL RESTful pour identifier une ressource avec une clé composite
    @GetMapping("/professeurs/{profId}/jours/{jourId}/periodes/{periode}")
    public ResponseEntity<DispoProfDto> getDispoProfById(
            @PathVariable Integer profId,
            @PathVariable Integer jourId,
            @PathVariable String periode) {
        return ResponseEntity.ok(dispoProfService.findById(profId, jourId, periode));
    }

    @PostMapping
    public ResponseEntity<DispoProfDto> createDispoProf(@RequestBody DispoProfDto dto) {
        DispoProfDto createdDto = dispoProfService.create(dto);
        return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/professeurs/{profId}/jours/{jourId}/periodes/{periode}")
    public ResponseEntity<Void> deleteDispoProf(
            @PathVariable Integer profId,
            @PathVariable Integer jourId,
            @PathVariable String periode) {
        dispoProfService.delete(profId, jourId, periode);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}