package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.*;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModuleService {
    private final ModuleEntityRepository moduleRepository;
    private final SemestreRepository semestreRepository;
    // NOUVEAU : Injection du repository pour TypeLocal
    private final TypeLocalRepository typeLocalRepository;

    // MODIFIÉ : Mise à jour du constructeur
    public ModuleService(ModuleEntityRepository moduleRepository, SemestreRepository semestreRepository, TypeLocalRepository typeLocalRepository) {
        this.moduleRepository = moduleRepository;
        this.semestreRepository = semestreRepository;
        this.typeLocalRepository = typeLocalRepository;
    }

    // MODIFIÉ : Le DTO de réponse inclut désormais les infos sur TypeLocal
    ModuleResponseDto toResponseDto(ModuleEntity module) {
        Semestre semestre = module.getSemestre();
        SemestreDto semestreDto = new SemestreDto(semestre.getId(), semestre.getLibelle(), semestre.getSession().getId());

        // NOUVEAU : Conversion des entités TypeLocal en DTOs (gère les cas null)
        TypeLocalDto typeLocalCoursDto = Optional.ofNullable(module.getTypeLocalRequisCours())
                .map(tl -> new TypeLocalDto(tl.getId(), tl.getLibelle()))
                .orElse(null);

        TypeLocalDto typeLocalTDDto = Optional.ofNullable(module.getTypeLocalRequisTD())
                .map(tl -> new TypeLocalDto(tl.getId(), tl.getLibelle()))
                .orElse(null);

        TypeLocalDto typeLocalTPDto = Optional.ofNullable(module.getTypeLocalRequisTP())
                .map(tl -> new TypeLocalDto(tl.getId(), tl.getLibelle()))
                .orElse(null);

        return new ModuleResponseDto(
                module.getId(),
                module.getLibelle(),
                module.getHeuresCours(),
                module.getHeuresTD(),
                module.getHeuresTP(),
                module.getSemDemTP(),
                module.getSemDemTD(),
                semestreDto,
                // NOUVEAU : Ajout des DTOs TypeLocal à la réponse
                typeLocalCoursDto,
                typeLocalTDDto,
                typeLocalTPDto
        );
    }

    // NOUVEAU : Méthode utilitaire pour trouver un TypeLocal ou retourner null
    private TypeLocal findTypeLocalById(Integer id) {
        if (id == null) {
            return null;
        }
        return typeLocalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TypeLocal non trouvé pour l'ID: " + id));
    }

    public List<ModuleResponseDto> findAll() {
        return moduleRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public ModuleResponseDto findById(Integer id) {
        return moduleRepository.findById(id).map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé: " + id));
    }

    // MODIFIÉ : La méthode create gère les nouveaux champs
    public ModuleResponseDto create(ModuleRequestDto dto) {
        Semestre semestre = semestreRepository.findById(dto.semestreId()).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé"));
        ModuleEntity module = new ModuleEntity();
        module.setSemestre(semestre);
        module.setLibelle(dto.libelle());
        module.setHeuresCours(dto.heuresCours());
        module.setHeuresTD(dto.heuresTD());
        module.setHeuresTP(dto.heuresTP());
        module.setSemDemTD(dto.semDemTD());
        module.setSemDemTP(dto.semDemTP());

        // NOUVEAU : Récupération et assignation des TypeLocal
        module.setTypeLocalRequisCours(findTypeLocalById(dto.typeLocalRequisCoursId()));
        module.setTypeLocalRequisTD(findTypeLocalById(dto.typeLocalRequisTDId()));
        module.setTypeLocalRequisTP(findTypeLocalById(dto.typeLocalRequisTPId()));

        return toResponseDto(moduleRepository.save(module));
    }

    // MODIFIÉ : La méthode update gère les nouveaux champs
    public ModuleResponseDto update(Integer id, ModuleRequestDto dto) {
        ModuleEntity module = moduleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Module non trouvé"));
        Semestre semestre = semestreRepository.findById(dto.semestreId()).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé"));
        module.setSemestre(semestre);
        module.setLibelle(dto.libelle());
        module.setHeuresCours(dto.heuresCours());
        module.setHeuresTD(dto.heuresTD());
        module.setHeuresTP(dto.heuresTP());
        module.setSemDemTD(dto.semDemTD());
        module.setSemDemTP(dto.semDemTP());

        // NOUVEAU : Récupération et assignation des TypeLocal
        module.setTypeLocalRequisCours(findTypeLocalById(dto.typeLocalRequisCoursId()));
        module.setTypeLocalRequisTD(findTypeLocalById(dto.typeLocalRequisTDId()));
        module.setTypeLocalRequisTP(findTypeLocalById(dto.typeLocalRequisTPId()));

        return toResponseDto(moduleRepository.save(module));
    }

    public void delete(Integer id) {
        if (!moduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Module non trouvé: " + id);
        }
        moduleRepository.deleteById(id);
    }
}