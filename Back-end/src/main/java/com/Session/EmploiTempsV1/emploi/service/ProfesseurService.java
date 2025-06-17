package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.ProfesseurRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.ProfesseurResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.Departement;
import com.Session.EmploiTempsV1.emploi.entities.Professeur;
import com.Session.EmploiTempsV1.emploi.repository.DepartementRepository;
import com.Session.EmploiTempsV1.emploi.repository.ProfesseurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesseurService {
    private final ProfesseurRepository professeurRepository;
    private final DepartementRepository departementRepository;
    private final DepartementService departementService; // Pour la conversion

    public ProfesseurService(ProfesseurRepository pr, DepartementRepository dr, DepartementService ds) {
        this.professeurRepository = pr;
        this.departementRepository = dr;
        this.departementService = ds;
    }

    public ProfesseurResponseDto toResponseDto(Professeur prof) {
        return new ProfesseurResponseDto(
                prof.getId(),
                prof.getNom(),
                prof.getPrenom(),
                prof.getStatut(),
                departementService.toDto(prof.getDepartement())
        );
    }

    public List<ProfesseurResponseDto> findAll() {
        return professeurRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public ProfesseurResponseDto findById(Integer id) {
        return professeurRepository.findById(id).map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé: " + id));
    }

    public ProfesseurResponseDto create(ProfesseurRequestDto dto) {
        Departement dept = departementRepository.findById(dto.departementId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé: " + dto.departementId()));

        Professeur prof = new Professeur();
        prof.setNom(dto.nom());
        prof.setPrenom(dto.prenom());
        prof.setStatut(dto.statut());
        prof.setDepartement(dept);

        return toResponseDto(professeurRepository.save(prof));
    }

    public ProfesseurResponseDto update(Integer id, ProfesseurRequestDto dto) {
        Professeur prof = professeurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé: " + id));
        Departement dept = departementRepository.findById(dto.departementId())
                .orElseThrow(() -> new EntityNotFoundException("Département non trouvé: " + dto.departementId()));

        prof.setNom(dto.nom());
        prof.setPrenom(dto.prenom());
        prof.setStatut(dto.statut());
        prof.setDepartement(dept);

        return toResponseDto(professeurRepository.save(prof));
    }

    public void delete(Integer id) {
        if (!professeurRepository.existsById(id)) {
            throw new EntityNotFoundException("Professeur non trouvé: " + id);
        }
        professeurRepository.deleteById(id);
    }
}