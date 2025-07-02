package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.RegroupeRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.RegroupeResponseDto;
import com.Session.EmploiTempsV1.emploi.service.RegroupeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regroupe")
public class RegroupController {

    private final RegroupeService regroupeService;

    @Autowired
    public RegroupController(RegroupeService regroupeService) {
        this.regroupeService = regroupeService;
    }

    /**
     * Récupère tous les regroupements
     */
    @GetMapping
    public ResponseEntity<List<RegroupeResponseDto>> getAllRegroupements() {
        return ResponseEntity.ok(regroupeService.findAll());
    }

    /**
     * Crée un nouveau regroupement
     */
    @PostMapping
    public ResponseEntity<RegroupeResponseDto> createRegroupement(
            @Valid @RequestBody RegroupeRequestDto requestDto) {
        RegroupeResponseDto created = regroupeService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Met à jour un regroupement existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<RegroupeResponseDto> updateRegroupement(
            @PathVariable Long id,
            @Valid @RequestBody RegroupeRequestDto requestDto) {
        return ResponseEntity.ok(regroupeService.update(id, requestDto));
    }

    /**
     * Supprime un regroupement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegroupement(@PathVariable Long id) {
        regroupeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
