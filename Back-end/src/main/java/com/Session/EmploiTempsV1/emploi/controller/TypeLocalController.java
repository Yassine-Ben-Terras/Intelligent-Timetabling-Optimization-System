package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.TypeLocalDto;
import com.Session.EmploiTempsV1.emploi.service.TypeLocalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/type-locaux")
public class TypeLocalController {

    private final TypeLocalService service;

    public TypeLocalController(TypeLocalService service) {
        this.service = service;
    }

    /**
     * Récupère la liste de tous les types de locaux.
     * GET /api/type-locaux
     */
    @GetMapping
    public ResponseEntity<List<TypeLocalDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Récupère un type de local par son ID.
     * GET /api/type-locaux/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeLocalDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Crée un nouveau type de local.
     * POST /api/type-locaux
     */
    @PostMapping
    public ResponseEntity<TypeLocalDto> create(@Valid @RequestBody TypeLocalDto dto) {
        // Pour une entité aussi simple, le DTO n'a pas besoin de validation complexe
        // mais @Valid est une bonne pratique.
        TypeLocalDto createdTypeLocal = service.create(dto);
        return new ResponseEntity<>(createdTypeLocal, HttpStatus.CREATED);
    }

    /**
     * Met à jour un type de local existant.
     * PUT /api/type-locaux/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TypeLocalDto> update(@PathVariable Integer id, @Valid @RequestBody TypeLocalDto dto) {
        TypeLocalDto updatedTypeLocal = service.update(id, dto);
        return ResponseEntity.ok(updatedTypeLocal);
    }

    /**
     * Supprime un type de local par son ID.
     * DELETE /api/type-locaux/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gère les exceptions de type EntityNotFoundException pour ce contrôleur.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}