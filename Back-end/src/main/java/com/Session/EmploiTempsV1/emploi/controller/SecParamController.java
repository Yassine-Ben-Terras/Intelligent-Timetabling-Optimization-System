package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.SecParamDto;
import com.Session.EmploiTempsV1.emploi.service.SecParamService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sec-params")
public class SecParamController {
    private final SecParamService service;
    public SecParamController(SecParamService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<List<SecParamDto>> getAll() { return ResponseEntity.ok(service.findAll()); }
    @PostMapping
    public ResponseEntity<SecParamDto> create(@RequestBody SecParamDto dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }
    @DeleteMapping("/annees/{anneeId}/filieres/{filiereId}/sections/{sectionId}/semestres/{semestreId}")
    public ResponseEntity<Void> delete(@PathVariable Integer anneeId, @PathVariable Integer filiereId, @PathVariable Integer sectionId, @PathVariable Integer semestreId) {
        service.delete(anneeId, filiereId, sectionId, semestreId);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler({EntityNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleExceptions(RuntimeException ex) {
        HttpStatus status = (ex instanceof EntityNotFoundException) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ex.getMessage(), status);
    }
}