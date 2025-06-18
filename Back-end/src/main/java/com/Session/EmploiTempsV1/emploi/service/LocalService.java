package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.LocalRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.LocalResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.Local;
import com.Session.EmploiTempsV1.emploi.entities.TypeLocal;
import com.Session.EmploiTempsV1.emploi.repository.LocalRepository;
import com.Session.EmploiTempsV1.emploi.repository.TypeLocalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class LocalService {

    private final LocalRepository localRepository;
    private final TypeLocalRepository typeLocalRepository;
    private final TypeLocalService typeLocalService; // Injecté pour la conversion

    public LocalService(LocalRepository localRepository,
                        TypeLocalRepository typeLocalRepository,
                        TypeLocalService typeLocalService) {
        this.localRepository = localRepository;
        this.typeLocalRepository = typeLocalRepository;
        this.typeLocalService = typeLocalService;
    }

    // Conversion Local -> LocalResponseDto
    public LocalResponseDto toResponseDto(Local local) {
        return new LocalResponseDto(
                local.getId(),
                local.getLibelle(),
                local.getCapacite(),
                typeLocalService.toDto(local.getType()) // Retourne null si type absent
        );
    }

    // Récupérer tous les locaux
    public List<LocalResponseDto> findAll() {
        return localRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // Récupérer un local par ID
    public LocalResponseDto findById(Integer id) {
        return localRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Local non trouvé avec l'id: " + id));
    }

    // Créer un nouveau local
    public LocalResponseDto create(LocalRequestDto dto) {
        TypeLocal type = null;
        if (dto.typeId() != null) {
            type = typeLocalRepository.findById(dto.typeId())
                    .orElseThrow(() -> new EntityNotFoundException("Type de local non trouvé avec l'id: " + dto.typeId()));
        }

        Local local = new Local();
        local.setLibelle(dto.libelle());
        local.setCapacite(dto.capacite());
        local.setType(type); // Peut être null

        Local savedLocal = localRepository.save(local);
        return toResponseDto(savedLocal);
    }

    // Mettre à jour un local existant
    public LocalResponseDto update(Integer id, LocalRequestDto dto) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local non trouvé avec l'id: " + id));

        TypeLocal type = null;
        if (dto.typeId() != null) {
            type = typeLocalRepository.findById(dto.typeId())
                    .orElseThrow(() -> new EntityNotFoundException("Type de local non trouvé avec l'id: " + dto.typeId()));
        }

        local.setLibelle(dto.libelle());
        local.setCapacite(dto.capacite());
        local.setType(type); // Peut être null

        Local updatedLocal = localRepository.save(local);
        return toResponseDto(updatedLocal);
    }

    // Supprimer un local
    public void delete(Integer id) {
        if (!localRepository.existsById(id)) {
            throw new EntityNotFoundException("Local non trouvé avec l'id: " + id);
        }
        localRepository.deleteById(id);
    }
}