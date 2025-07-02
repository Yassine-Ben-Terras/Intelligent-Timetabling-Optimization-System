package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.RegroupementDto;
import com.Session.EmploiTempsV1.emploi.service.RegroupementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regroupements")
@RequiredArgsConstructor
public class RegroupementController {

    private final RegroupementService regroupementService;

    @PostMapping
    public ResponseEntity<RegroupementDto> createRegroupement(@RequestBody RegroupementDto regroupementDto) {
        // S'assurer que l'ID est null pour une création
        regroupementDto.setId(null);
        RegroupementDto savedRegroupement = regroupementService.saveRegroupement(regroupementDto);
        return new ResponseEntity<>(savedRegroupement, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegroupementDto> updateRegroupement(@PathVariable Integer id, @RequestBody RegroupementDto regroupementDto) {
        // Assigner l'ID de l'URL au DTO pour garantir la mise à jour de la bonne entité
        regroupementDto.setId(id);
        try {
            RegroupementDto updatedRegroupement = regroupementService.saveRegroupement(regroupementDto);
            return ResponseEntity.ok(updatedRegroupement);
        } catch (RuntimeException e) {
            // Gérer le cas où le regroupement n'est pas trouvé
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RegroupementDto>> getAllRegroupements() {
        List<RegroupementDto> regroupements = regroupementService.getAllRegroupements();
        return ResponseEntity.ok(regroupements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegroupementDto> getRegroupementById(@PathVariable Integer id) {
        return regroupementService.getRegroupementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegroupement(@PathVariable Integer id) {
        try {
            regroupementService.deleteRegroupement(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Gérer le cas où le regroupement à supprimer n'est pas trouvé
            return ResponseEntity.notFound().build();
        }
    }
}