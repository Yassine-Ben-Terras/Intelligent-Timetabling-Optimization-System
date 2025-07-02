package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.PlageTravailDto;
import com.Session.EmploiTempsV1.emploi.service.PlageTravailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plages-travail")
public class PlageTravailController {

    private final PlageTravailService plageTravailService;

    public PlageTravailController(PlageTravailService plageTravailService) {
        this.plageTravailService = plageTravailService;
    }

    @PostMapping
    public ResponseEntity<PlageTravailDto> createPlageTravail(@RequestBody PlageTravailDto dto) {
        PlageTravailDto createdPlage = plageTravailService.createPlageTravail(dto);
        return new ResponseEntity<>(createdPlage, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PlageTravailDto>> getAllPlagesTravail() {
        List<PlageTravailDto> plages = plageTravailService.getAllPlagesTravail();
        return ResponseEntity.ok(plages);
    }

    // NOUVEAU ENDPOINT - Récupérer par ID
    @GetMapping("/{id}")
    public ResponseEntity<PlageTravailDto> getPlageTravailById(@PathVariable Integer id) {
        PlageTravailDto plage = plageTravailService.getPlageTravailById(id);
        return ResponseEntity.ok(plage);
    }

    // NOUVEAU ENDPOINT - Mettre à jour
    @PutMapping("/{id}")
    public ResponseEntity<PlageTravailDto> updatePlageTravail(@PathVariable Integer id, @RequestBody PlageTravailDto dto) {
        PlageTravailDto updatedPlage = plageTravailService.updatePlageTravail(id, dto);
        return ResponseEntity.ok(updatedPlage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlageTravail(@PathVariable Integer id) {
        plageTravailService.deletePlageTravail(id);
        return ResponseEntity.noContent().build();
    }
}