package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.ModuleRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.ModuleResponseDto;
import com.Session.EmploiTempsV1.emploi.service.ModuleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) { this.moduleService = moduleService; }

    @GetMapping("all")
    public ResponseEntity<List<ModuleResponseDto>> getAll() {
        return ResponseEntity.ok(moduleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(moduleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ModuleResponseDto> create(@RequestBody ModuleRequestDto dto) {
        return new ResponseEntity<>(moduleService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> update(@PathVariable Integer id, @RequestBody ModuleRequestDto dto) {
        return ResponseEntity.ok(moduleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}