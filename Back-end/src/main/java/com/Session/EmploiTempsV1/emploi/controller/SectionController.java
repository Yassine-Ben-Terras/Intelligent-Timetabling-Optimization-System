package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.SectionDto;
import com.Session.EmploiTempsV1.emploi.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {
    private final SectionService service;
    public SectionController(SectionService service) { this.service = service; }
    @GetMapping("all")
    public ResponseEntity<List<SectionDto>> getAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<SectionDto> getById(@PathVariable Integer id) { return ResponseEntity.ok(service.findById(id)); }
    @PostMapping
    public ResponseEntity<SectionDto> create(@RequestBody SectionDto dto) { return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED); }
    @PutMapping("/{id}")
    public ResponseEntity<SectionDto> update(@PathVariable Integer id, @RequestBody SectionDto dto) { return ResponseEntity.ok(service.update(id, dto)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) { return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); }
}