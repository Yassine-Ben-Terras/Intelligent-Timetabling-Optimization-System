package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.SemestreRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.SemestreResponseDto;
import com.Session.EmploiTempsV1.emploi.service.SemestreService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/semestres")
public class SemestreController {
    private final SemestreService service;
    public SemestreController(SemestreService service) { this.service = service; }
    @GetMapping("all")
    public ResponseEntity<List<SemestreResponseDto>> getAll() { return ResponseEntity.ok(service.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<SemestreResponseDto> getById(@PathVariable Integer id) { return ResponseEntity.ok(service.findById(id)); }
    @PostMapping
    public ResponseEntity<SemestreResponseDto> create(@RequestBody SemestreRequestDto dto) { return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED); }
    @PutMapping("/{id}")
    public ResponseEntity<SemestreResponseDto> update(@PathVariable Integer id, @RequestBody SemestreRequestDto dto) { return ResponseEntity.ok(service.update(id, dto)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) { return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); }
}