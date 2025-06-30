package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.SessionRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.SessionResponseDto;
import com.Session.EmploiTempsV1.emploi.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService sessionService;
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    @GetMapping("all")
    public ResponseEntity<List<SessionResponseDto>> getAll() { return ResponseEntity.ok(sessionService.findAll()); }
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDto> getById(@PathVariable Integer id) { return ResponseEntity.ok(sessionService.findById(id)); }
    @PostMapping
    public ResponseEntity<SessionResponseDto> create(@Valid @RequestBody SessionRequestDto dto) {
        return new ResponseEntity<>(sessionService.create(dto), HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<SessionResponseDto> update(@PathVariable Integer id, @Valid @RequestBody SessionRequestDto dto) {
        return ResponseEntity.ok(sessionService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}