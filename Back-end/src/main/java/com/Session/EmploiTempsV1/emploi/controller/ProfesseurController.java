package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.ProfesseurRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.ProfesseurResponseDto;
import com.Session.EmploiTempsV1.emploi.service.ProfesseurService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/professeurs")
public class ProfesseurController {
    private final ProfesseurService service;

    public ProfesseurController(ProfesseurService service) {
        this.service = service;
    }

    @GetMapping("all")
    public ResponseEntity<List<ProfesseurResponseDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesseurResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProfesseurResponseDto> create(@RequestBody ProfesseurRequestDto dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesseurResponseDto> update(@PathVariable Integer id, @RequestBody ProfesseurRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}