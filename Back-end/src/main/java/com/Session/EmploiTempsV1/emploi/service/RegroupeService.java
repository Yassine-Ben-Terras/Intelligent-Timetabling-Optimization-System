package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.*;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class RegroupeService {

    @Autowired private RegroupeRepository regroupeRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private GroupeRepository groupeRepository;

    @Autowired private SectionService sectionService;
    @Autowired private GroupeService groupeService;

    @Transactional(readOnly = true)
    public List<RegroupeResponseDto> findAll() {
        return regroupeRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegroupeResponseDto create(RegroupeRequestDto requestDto) {
        Regroupe regroupe = new Regroupe(requestDto.nom(), requestDto.type());
        updateAndValidateAssociations(regroupe, requestDto);
        Regroupe saved = regroupeRepository.save(regroupe);
        return toResponseDto(saved);
    }

    @Transactional
    public RegroupeResponseDto update(Long id, RegroupeRequestDto requestDto) {
        Regroupe regroupe = regroupeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Regroupement non trouvé avec l'ID: " + id));

        regroupe.setNom(requestDto.nom());
        regroupe.setType(requestDto.type());

        updateAndValidateAssociations(regroupe, requestDto);

        Regroupe updated = regroupeRepository.save(regroupe);
        return toResponseDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!regroupeRepository.existsById(id)) {
            throw new EntityNotFoundException("Regroupement non trouvé avec l'ID: " + id);
        }
        regroupeRepository.deleteById(id);
    }

    private void updateAndValidateAssociations(Regroupe regroupe, RegroupeRequestDto dto) {
        if (dto.type() == TypeRegroupement.SECTION) {
            regroupe.setGroupes(Collections.emptySet());
            if (dto.sectionIds() == null || dto.sectionIds().isEmpty()) {
                throw new IllegalArgumentException("La liste des IDs de section est requise.");
            }
            Set<Section> sections = new HashSet<>(sectionRepository.findAllById(dto.sectionIds()));
            regroupe.setSections(sections);

        } else if (dto.type() == TypeRegroupement.GROUPE) {
            regroupe.setSections(Collections.emptySet());
            if (dto.groupeIds() == null || dto.groupeIds().isEmpty()) {
                throw new IllegalArgumentException("La liste des IDs de groupe est requise.");
            }
            Set<Groupe> groupes = new HashSet<>(groupeRepository.findAllById(dto.groupeIds()));
            regroupe.setGroupes(groupes);

        } else {
            throw new IllegalArgumentException("Type de regroupement non valide ou nul.");
        }
    }

    private RegroupeResponseDto toResponseDto(Regroupe regroupe) {
        Set<SectionDto> sectionDtos = regroupe.getSections().stream()
                .map(sectionService::toDto)
                .collect(Collectors.toSet());

        Set<GroupeResponseDto> groupeDtos = regroupe.getGroupes().stream()
                .map(groupeService::toResponseDto)
                .collect(Collectors.toSet());

        return new RegroupeResponseDto(
                regroupe.getId(),
                regroupe.getNom(),
                regroupe.getType(),
                sectionDtos,
                groupeDtos
        );
    }
}
