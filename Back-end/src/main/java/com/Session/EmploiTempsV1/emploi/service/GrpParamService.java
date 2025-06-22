package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.GrpParamCreateDto;
import com.Session.EmploiTempsV1.emploi.dto.GrpParamDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GrpParamService {
    private final GrpParamRepository grpParamRepository;
    private final SectionRepository sectionRepository;
    private final GroupeRepository groupeRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final SemestreRepository semestreRepository;

    public GrpParamService(GrpParamRepository r, SectionRepository s, GroupeRepository g, AnneeUniversitaireRepository a, SemestreRepository sem) {
        this.grpParamRepository = r; this.sectionRepository = s; this.groupeRepository = g; this.anneeRepository = a; this.semestreRepository = sem;
    }

    // Convertisseur vers le DTO riche (pour l'affichage)
    private GrpParamDto toDto(GrpParam entity) {
        String key = String.format("%d-%d-%d-%d",
                entity.getSection().getId(),
                entity.getGroupe().getId(),
                entity.getAnnee().getId(),
                entity.getSemestre().getId()
        );

        return new GrpParamDto(
                key,
                entity.getSection().getId(),
                entity.getSection().getLibelle(),
                entity.getGroupe().getId(),
                entity.getGroupe().getLibelle(),
                entity.getAnnee().getId(),
                entity.getAnnee().getLibelle(),
                entity.getSemestre().getId(),
                entity.getSemestre().getLibelle()
        );
    }

    @Transactional(readOnly = true)
    public List<GrpParamDto> findAll() {
        return grpParamRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // CORRECTION PRINCIPALE ICI
    @Transactional
    // La méthode prend le DTO de création en entrée...
    public GrpParamDto create(GrpParamCreateDto createDto) { // <-- ...mais retourne le DTO riche !

        Section section = sectionRepository.findById(createDto.sectionId()).orElseThrow(() -> new EntityNotFoundException("Section non trouvée avec l'id: " + createDto.sectionId()));
        Groupe groupe = groupeRepository.findById(createDto.groupeId()).orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé avec l'id: " + createDto.groupeId()));
        AnneeUniversitaire annee = anneeRepository.findById(createDto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année non trouvée avec l'id: " + createDto.anneeId()));
        Semestre semestre = semestreRepository.findById(createDto.semestreId()).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé avec l'id: " + createDto.semestreId()));

        GrpParamId id = new GrpParamId(section.getId(), groupe.getId(), annee.getId(), semestre.getId());
        if(grpParamRepository.existsById(id)){
            throw new DataIntegrityViolationException("Ce paramétrage de groupe existe déjà.");
        }

        GrpParam grpParam = new GrpParam();
        grpParam.setSection(section);
        grpParam.setGroupe(groupe);
        grpParam.setAnnee(annee);
        grpParam.setSemestre(semestre);

        GrpParam savedEntity = grpParamRepository.save(grpParam);

        // On retourne l'entité sauvegardée convertie en DTO riche.
        return toDto(savedEntity);
    }

    @Transactional
    public void delete(Integer sectionId, Integer groupeId, Integer anneeId, Integer semestreId) {
        GrpParamId id = new GrpParamId(sectionId, groupeId, anneeId, semestreId);
        if (!grpParamRepository.existsById(id)) {
            throw new EntityNotFoundException("Lien Grp-Param non trouvé");
        }
        grpParamRepository.deleteById(id);
    }
}