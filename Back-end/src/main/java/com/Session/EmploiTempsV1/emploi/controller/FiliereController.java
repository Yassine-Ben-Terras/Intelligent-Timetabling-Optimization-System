package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.FiliereRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.FiliereResponseDto;
import com.Session.EmploiTempsV1.emploi.service.FiliereService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/filieres")
public class FiliereController {

    private final FiliereService filiereService;

    public FiliereController(FiliereService filiereService) {
        this.filiereService = filiereService;
    }

    @GetMapping("all")
    public ResponseEntity<List<FiliereResponseDto>> getAllFilieres() {
        return ResponseEntity.ok(filiereService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiliereResponseDto> getFiliereById(@PathVariable Integer id) {
        return ResponseEntity.ok(filiereService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FiliereResponseDto> createFiliere(@RequestBody FiliereRequestDto filiereDto) {
        FiliereResponseDto createdFiliere = filiereService.create(filiereDto);
        return new ResponseEntity<>(createdFiliere, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiliereResponseDto> updateFiliere(@PathVariable Integer id, @RequestBody FiliereRequestDto filiereDto) {
        return ResponseEntity.ok(filiereService.update(id, filiereDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFiliere(@PathVariable Integer id) {
        filiereService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}