package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.DispLocDto;
import com.Session.EmploiTempsV1.emploi.service.DispLocService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dispo-locaux")
public class DispLocController {
    private final DispLocService service;
    public DispLocController(DispLocService service) { this.service = service; }

    @GetMapping("all")
    public ResponseEntity<List<DispLocDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<DispLocDto> createOrUpdate(@RequestBody DispLocDto dto) {
        return new ResponseEntity<>(service.createOrUpdate(dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/annees/{anneeId}/locaux/{localId}/sessions/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable Integer anneeId, @PathVariable Integer localId, @PathVariable Integer sessionId) {
        service.delete(anneeId, localId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}