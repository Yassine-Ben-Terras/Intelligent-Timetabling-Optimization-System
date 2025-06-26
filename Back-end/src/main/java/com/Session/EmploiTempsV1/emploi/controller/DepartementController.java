package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.DepartementDto;
import com.Session.EmploiTempsV1.emploi.service.DepartementService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departements")
public class DepartementController {

    private final DepartementService departementService;

    public DepartementController(DepartementService departementService) {
        this.departementService = departementService;
    }

    @GetMapping("all")
    public ResponseEntity<List<DepartementDto>> getAllDepartements() {
        return ResponseEntity.ok(departementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartementDto> getDepartementById(@PathVariable Integer id) {
        return ResponseEntity.ok(departementService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DepartementDto> createDepartement(@RequestBody DepartementDto departementDto) {
        DepartementDto createdDepartement = departementService.create(departementDto);
        return new ResponseEntity<>(createdDepartement, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartementDto> updateDepartement(@PathVariable Integer id, @RequestBody DepartementDto departementDto) {
        return ResponseEntity.ok(departementService.update(id, departementDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(@PathVariable Integer id) {
        departementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Gestionnaire d'exception localisé pour ce contrôleur
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}