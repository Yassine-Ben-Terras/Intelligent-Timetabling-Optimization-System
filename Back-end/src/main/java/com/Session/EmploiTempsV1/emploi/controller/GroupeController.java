package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.dto.GroupeRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.GroupeResponseDto;
import com.Session.EmploiTempsV1.emploi.service.GroupeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groupes")
public class GroupeController {

    private final GroupeService groupeService;

    public GroupeController(GroupeService groupeService) {
        this.groupeService = groupeService;
    }

    @GetMapping("all")
    public ResponseEntity<List<GroupeResponseDto>> getAllGroupes() {
        List<GroupeResponseDto> groupes = groupeService.findAll();
        return ResponseEntity.ok(groupes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupeResponseDto> getGroupeById(@PathVariable Integer id) {
        GroupeResponseDto groupe = groupeService.findById(id);
        return ResponseEntity.ok(groupe);
    }

    @PostMapping
    public ResponseEntity<GroupeResponseDto> createGroupe(@Valid @RequestBody GroupeRequestDto dto) {
        GroupeResponseDto createdGroupe = groupeService.create(dto);
        return new ResponseEntity<>(createdGroupe, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupeResponseDto> updateGroupe(@PathVariable Integer id, @Valid @RequestBody GroupeRequestDto dto) {
        GroupeResponseDto updatedGroupe = groupeService.update(id, dto);
        return ResponseEntity.ok(updatedGroupe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroupe(@PathVariable Integer id) {
        groupeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}