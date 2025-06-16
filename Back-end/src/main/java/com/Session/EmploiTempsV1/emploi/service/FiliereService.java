package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.FiliereRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.FiliereResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.Departement;
import com.Session.EmploiTempsV1.emploi.entities.Filiere;
import com.Session.EmploiTempsV1.emploi.repository.DepartementRepository;
import com.Session.EmploiTempsV1.emploi.repository.FiliereRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FiliereService {

    private final FiliereRepository filiereRepository;
    private final DepartementRepository departementRepository;
    private final DepartementService departementService; // Pour la conversion en DTO

    public FiliereService(FiliereRepository filiereRepository, DepartementRepository departementRepository, DepartementService departementService) {
        this.filiereRepository = filiereRepository;
        this.departementRepository = departementRepository;
        this.departementService = departementService;
    }

    // Conversion Entité -> DTO
    private FiliereResponseDto toResponseDto(Filiere filiere) {
        return new FiliereResponseDto(
                filiere.getId(),
                filiere.getCodeFiliere(),
                filiere.getLibelle(),
                departementService.toDto(filiere.getDepartement()) // Réutiliser le mapper
        );
    }

    public List<FiliereResponseDto> findAll() {
        return filiereRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public FiliereResponseDto findById(Integer id) {
        return filiereRepository.findById(id).map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Filiere non trouvée avec l'id : " + id));
    }

    public FiliereResponseDto create(FiliereRequestDto dto) {
        Departement departement = departementRepository.findById(dto.departementId())
                .orElseThrow(() -> new EntityNotFoundException("Departement non trouvé avec l'id : " + dto.departementId()));

        Filiere filiere = new Filiere();
        filiere.setCodeFiliere(dto.codeFiliere());
        filiere.setLibelle(dto.libelle());
        filiere.setDepartement(departement);

        Filiere savedFiliere = filiereRepository.save(filiere);
        return toResponseDto(savedFiliere);
    }

    public FiliereResponseDto update(Integer id, FiliereRequestDto dto) {
        Filiere existingFiliere = filiereRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Filiere non trouvée avec l'id : " + id));

        Departement departement = departementRepository.findById(dto.departementId())
                .orElseThrow(() -> new EntityNotFoundException("Departement non trouvé avec l'id : " + dto.departementId()));

        existingFiliere.setCodeFiliere(dto.codeFiliere());
        existingFiliere.setLibelle(dto.libelle());
        existingFiliere.setDepartement(departement);

        Filiere updatedFiliere = filiereRepository.save(existingFiliere);
        return toResponseDto(updatedFiliere);
    }

    public void delete(Integer id) {
        if (!filiereRepository.existsById(id)) {
            throw new EntityNotFoundException("Filiere non trouvée avec l'id : " + id);
        }
        filiereRepository.deleteById(id);
    }
}