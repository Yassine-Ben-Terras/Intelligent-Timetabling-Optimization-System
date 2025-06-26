package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.FilModDto;
import com.Session.EmploiTempsV1.emploi.service.FilModService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fil-mods")
public class FilModController {
    private final FilModService service;
    public FilModController(FilModService service) { this.service = service; }

    @GetMapping("all")
    public ResponseEntity<List<FilModDto>> getAll() { return ResponseEntity.ok(service.findAll()); }

    @PostMapping
    public ResponseEntity<FilModDto> create(@RequestBody FilModDto dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/filieres/{filiereId}/modules/{moduleId}")
    public ResponseEntity<Void> delete(@PathVariable Integer filiereId, @PathVariable Integer moduleId) {
        service.delete(filiereId, moduleId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
