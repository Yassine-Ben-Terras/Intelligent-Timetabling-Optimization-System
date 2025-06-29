package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.AnneeUniversitaireDto;
import com.Session.EmploiTempsV1.emploi.service.AnneeUniversitaireService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/annees")
public class AnneeUniversitaireController {

    private final AnneeUniversitaireService anneeService;

    public AnneeUniversitaireController(AnneeUniversitaireService anneeService) {
        this.anneeService = anneeService;
    }

    @GetMapping("all")
    public ResponseEntity<List<AnneeUniversitaireDto>> getAll() {
        return ResponseEntity.ok(anneeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnneeUniversitaireDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(anneeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AnneeUniversitaireDto> create(@RequestBody AnneeUniversitaireDto dto) {
        return new ResponseEntity<>(anneeService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnneeUniversitaireDto> update(@PathVariable Integer id, @RequestBody AnneeUniversitaireDto dto) {
        return ResponseEntity.ok(anneeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        anneeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}