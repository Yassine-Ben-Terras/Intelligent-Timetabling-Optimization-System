package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.DepartementDto;
import com.Session.EmploiTempsV1.emploi.entities.Departement;
import com.Session.EmploiTempsV1.emploi.repository.DepartementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartementService {

    private final DepartementRepository departementRepository;

    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    // Méthode pour convertir Entité vers DTO
    DepartementDto toDto(Departement departement) {
        return new DepartementDto(departement.getId(), departement.getLibelle());
    }

    // Méthode pour convertir DTO vers Entité (pour la création)
    private Departement toEntity(DepartementDto dto) {
        Departement departement = new Departement();
        departement.setLibelle(dto.libelle());
        return departement;
    }

    public List<DepartementDto> findAll() {
        return departementRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartementDto findById(Integer id) {
        return departementRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Departement non trouvé avec l'id : " + id));
    }

    public DepartementDto create(DepartementDto departementDto) {
        Departement departement = toEntity(departementDto);
        Departement savedDepartement = departementRepository.save(departement);
        return toDto(savedDepartement);
    }

    public DepartementDto update(Integer id, DepartementDto departementDto) {
        Departement existingDepartement = departementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Departement non trouvé avec l'id : " + id));

        existingDepartement.setLibelle(departementDto.libelle());
        Departement updatedDepartement = departementRepository.save(existingDepartement);
        return toDto(updatedDepartement);
    }

    public void delete(Integer id) {
        if (!departementRepository.existsById(id)) {
            throw new EntityNotFoundException("Departement non trouvé avec l'id : " + id);
        }
        departementRepository.deleteById(id);
    }
}