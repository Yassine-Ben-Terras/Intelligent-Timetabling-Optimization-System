// FICHIER MODIFIÉ : ProfParamService.java
package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.ProfParamDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfParamService {
    private final ProfParamRepository profParamRepository;
    private final ProfesseurRepository professeurRepository;
    private final ModuleEntityRepository moduleRepository;
    // MODIFIÉ : Ajout du repository pour TypeSeance
    private final TypeSeanceRepository typeSeanceRepository;

    // MODIFIÉ : Mise à jour du constructeur pour injecter le nouveau repository
    public ProfParamService(ProfParamRepository profParamRepository, ProfesseurRepository professeurRepository, ModuleEntityRepository moduleRepository, TypeSeanceRepository typeSeanceRepository) {
        this.profParamRepository = profParamRepository;
        this.professeurRepository = professeurRepository;
        this.moduleRepository = moduleRepository;
        this.typeSeanceRepository = typeSeanceRepository;
    }

    // MODIFIÉ : La conversion en DTO inclut maintenant l'ID du type de séance
    private ProfParamDto toDto(ProfParam entity) {
        return new ProfParamDto(
                entity.getProfesseur().getId(),
                entity.getModule().getId(),
                entity.getTypeSeance().getId()
        );
    }

    public List<ProfParamDto> findAll() {
        return profParamRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProfParamDto create(ProfParamDto dto) {
        // Récupération des 3 entités nécessaires
        ModuleEntity module = moduleRepository.findById(dto.moduleId())
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec l'id: " + dto.moduleId()));
        Professeur prof = professeurRepository.findById(dto.professeurId())
                .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé avec l'id: " + dto.professeurId()));
        // MODIFIÉ : Récupération de l'entité TypeSeance
        TypeSeance typeSeance = typeSeanceRepository.findById(dto.typeSeanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type de séance non trouvé avec l'id: " + dto.typeSeanceId()));

        // MODIFIÉ : La clé composite utilise maintenant 3 IDs
        ProfParamId id = new ProfParamId(module.getId(), prof.getId(), typeSeance.getId());

        // Vérifier si l'affectation exacte existe déjà
        if(profParamRepository.existsById(id)){
            throw new IllegalArgumentException("Cette affectation (professeur, module, type de séance) existe déjà.");
        }

        // MODIFIÉ : Le constructeur de l'entité prend les 3 objets
        ProfParam profParam = new ProfParam(module, prof, typeSeance);
        return toDto(profParamRepository.save(profParam));
    }

    // MODIFIÉ : La méthode delete prend maintenant 3 IDs
    public void delete(Integer professeurId, Integer moduleId, Integer typeSeanceId) {
        // MODIFIÉ : La clé composite est créée avec les 3 IDs
        ProfParamId id = new ProfParamId(moduleId, professeurId, typeSeanceId);
        if (!profParamRepository.existsById(id)) {
            throw new EntityNotFoundException("Lien Prof-Param non trouvé pour professeur " + professeurId + ", module " + moduleId + " et type séance " + typeSeanceId);
        }
        profParamRepository.deleteById(id);
    }
}