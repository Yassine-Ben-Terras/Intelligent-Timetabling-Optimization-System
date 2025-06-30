package com.Session.EmploiTempsV1.emploi.controller;

import com.Session.EmploiTempsV1.emploi.entities.TypeSeance;
import com.Session.EmploiTempsV1.emploi.repository.TypeSeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/types-seance")
public class TypeSeanceController {
    @Autowired
    private TypeSeanceRepository typeSeanceRepository;

    @GetMapping
    public List<TypeSeance> getAllTypesSeance() {
        return typeSeanceRepository.findAll();
    }
}