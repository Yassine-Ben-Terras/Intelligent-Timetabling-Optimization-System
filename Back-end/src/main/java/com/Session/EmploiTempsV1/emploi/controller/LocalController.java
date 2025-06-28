package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.LocalRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.LocalResponseDto;
import com.Session.EmploiTempsV1.emploi.service.LocalService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locaux") // Vous aviez mis /salles, je mets /locaux pour correspondre à l'entité
public class LocalController {

    private final LocalService localService;

    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    @GetMapping("all")
    public ResponseEntity<List<LocalResponseDto>> getAllLocaux() {
        List<LocalResponseDto> locaux = localService.findAll();
        return ResponseEntity.ok(locaux);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalResponseDto> getLocalById(@PathVariable Integer id) {
        LocalResponseDto local = localService.findById(id);
        return ResponseEntity.ok(local);
    }

    @PostMapping
    public ResponseEntity<LocalResponseDto> createLocal(@Valid @RequestBody LocalRequestDto dto) {
        LocalResponseDto createdLocal = localService.create(dto);
        return new ResponseEntity<>(createdLocal, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalResponseDto> updateLocal(@PathVariable Integer id, @Valid @RequestBody LocalRequestDto dto) {
        LocalResponseDto updatedLocal = localService.update(id, dto);
        return ResponseEntity.ok(updatedLocal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocal(@PathVariable Integer id) {
        localService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}