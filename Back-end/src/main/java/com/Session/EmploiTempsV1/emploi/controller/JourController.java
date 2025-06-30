package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.JourDto;
import com.Session.EmploiTempsV1.emploi.service.JourService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jours")
public class JourController {
    private final JourService jourService;
    public JourController(JourService jourService) { this.jourService = jourService; }

    @GetMapping("all")
    public ResponseEntity<List<JourDto>> getAll() { return ResponseEntity.ok(jourService.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<JourDto> getById(@PathVariable Integer id) { return ResponseEntity.ok(jourService.findById(id)); }
    @PostMapping
    public ResponseEntity<JourDto> create(@RequestBody JourDto dto) { return new ResponseEntity<>(jourService.create(dto), HttpStatus.CREATED); }
    @PutMapping("/{id}")
    public ResponseEntity<JourDto> update(@PathVariable Integer id, @RequestBody JourDto dto) { return ResponseEntity.ok(jourService.update(id, dto)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { jourService.delete(id); return ResponseEntity.noContent().build(); }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) { return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); }
}