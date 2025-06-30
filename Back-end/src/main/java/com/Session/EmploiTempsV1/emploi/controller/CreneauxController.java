package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.CreneauxRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.CreneauxResponseDto;
import com.Session.EmploiTempsV1.emploi.service.CreneauxService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/creneaux") // Corrigé de 'crenaux' à 'creneaux'
public class CreneauxController {

    private final CreneauxService creneauxService;

    public CreneauxController(CreneauxService creneauxService) {
        this.creneauxService = creneauxService;
    }

    @GetMapping("all")
    public ResponseEntity<List<CreneauxResponseDto>> getAll() {
        return ResponseEntity.ok(creneauxService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreneauxResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(creneauxService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CreneauxResponseDto> create(@RequestBody CreneauxRequestDto dto) {
        return new ResponseEntity<>(creneauxService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreneauxResponseDto> update(@PathVariable Integer id, @RequestBody CreneauxRequestDto dto) {
        return ResponseEntity.ok(creneauxService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        creneauxService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}