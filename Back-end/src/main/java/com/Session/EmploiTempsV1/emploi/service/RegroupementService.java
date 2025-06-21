package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.RegroupementDto;
import com.Session.EmploiTempsV1.emploi.dto.RegroupementDetailDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegroupementService {

    private final RegroupementRepository regroupementRepository;
    private final RegroupementDetailRepository regroupementDetailRepository;
    private final SectionRepository sectionRepository;
    private final GroupeRepository groupeRepository;
    private final ModuleEntityRepository moduleRepository;

    /**
     * Crée ou met à jour un regroupement.
     * Si l'ID du DTO est null, un nouveau regroupement est créé.
     * Sinon, le regroupement existant est mis à jour.
     */
    @Transactional
    public RegroupementDto saveRegroupement(RegroupementDto regroupementDto) {
        Regroupement regroupement;

        if (regroupementDto.getId() != null) {
            // Mise à jour d'un regroupement existant
            regroupement = regroupementRepository.findById(regroupementDto.getId())
                    .orElseThrow(() -> new RuntimeException("Regroupement non trouvé avec l'ID : " + regroupementDto.getId()));

            // Supprimer les anciens détails pour les remplacer par les nouveaux
            regroupementDetailRepository.deleteAll(regroupement.getDetails());
            regroupement.getDetails().clear();

        } else {
            // Création d'un nouveau regroupement
            regroupement = new Regroupement();
        }

        regroupement.setLibelle(regroupementDto.getLibelle());
        regroupement.setTypeRegroupement(regroupementDto.getTypeRegroupement());

        // Mapper les DTOs de détails en entités
        List<RegroupementDetail> details = regroupementDto.getDetails().stream().map(detailDto -> {
            RegroupementDetail detail = new RegroupementDetail();
            detail.setRegroupement(regroupement);

            if (detailDto.getSectionId() != null) {
                Section section = sectionRepository.findById(detailDto.getSectionId())
                        .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID : " + detailDto.getSectionId()));
                detail.setSection(section);
            }
            if (detailDto.getGroupeId() != null) {
                Groupe groupe = groupeRepository.findById(detailDto.getGroupeId())
                        .orElseThrow(() -> new RuntimeException("Groupe non trouvé avec l'ID : " + detailDto.getGroupeId()));
                detail.setGroupe(groupe);
            }
            ModuleEntity module = moduleRepository.findById(detailDto.getModuleId())
                    .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'ID : " + detailDto.getModuleId()));
            detail.setModule(module);

            return detail;
        }).collect(Collectors.toList());

        regroupement.setDetails(details);
        Regroupement savedRegroupement = regroupementRepository.save(regroupement);

        // Pas besoin d'appeler saveAll sur les détails si la cascade est bien configurée
        // regroupementDetailRepository.saveAll(details);

        return mapToDto(savedRegroupement);
    }

    public List<RegroupementDto> getAllRegroupements() {
        return regroupementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<RegroupementDto> getRegroupementById(Integer id) {
        return regroupementRepository.findById(id).map(this::mapToDto);
    }

    @Transactional
    public void deleteRegroupement(Integer id) {
        if (!regroupementRepository.existsById(id)) {
            throw new RuntimeException("Regroupement non trouvé avec l'ID : " + id);
        }
        regroupementRepository.deleteById(id);
    }

    private RegroupementDto mapToDto(Regroupement regroupement) {
        return RegroupementDto.builder()
                .id(regroupement.getId())
                .libelle(regroupement.getLibelle())
                .typeRegroupement(regroupement.getTypeRegroupement())
                .details(regroupement.getDetails().stream()
                        .map(this::mapDetailToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private RegroupementDetailDto mapDetailToDto(RegroupementDetail detail) {
        return RegroupementDetailDto.builder()
                .id(detail.getId())
                .sectionId(Optional.ofNullable(detail.getSection()).map(Section::getId).orElse(null))
                .sectionLibelle(Optional.ofNullable(detail.getSection()).map(Section::getLibelle).orElse(null))
                .groupeId(Optional.ofNullable(detail.getGroupe()).map(Groupe::getId).orElse(null))
                .groupeLibelle(Optional.ofNullable(detail.getGroupe()).map(Groupe::getLibelle).orElse(null))
                .moduleId(detail.getModule().getId())
                .moduleLibelle(detail.getModule().getLibelle())
                .build();
    }
}