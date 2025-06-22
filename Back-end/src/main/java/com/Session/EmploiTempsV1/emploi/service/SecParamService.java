package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.SecParamDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecParamService {
    private final SecParamRepository secParamRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final FiliereRepository filiereRepository;
    private final SectionRepository sectionRepository;
    private final SemestreRepository semestreRepository;

    public SecParamService(SecParamRepository r, AnneeUniversitaireRepository a, FiliereRepository f, SectionRepository s, SemestreRepository sem) {
        this.secParamRepository = r; this.anneeRepository = a; this.filiereRepository = f; this.sectionRepository = s; this.semestreRepository = sem;
    }
    private SecParamDto toDto(SecParam entity) {
        return new SecParamDto(entity.getAnnee().getId(), entity.getFiliere().getId(), entity.getSection().getId(), entity.getSemestre().getId());
    }
    public List<SecParamDto> findAll() { return secParamRepository.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public SecParamDto create(SecParamDto dto) {
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année non trouvée: " + dto.anneeId()));
        Filiere filiere = filiereRepository.findById(dto.filiereId()).orElseThrow(() -> new EntityNotFoundException("Filière non trouvée: " + dto.filiereId()));
        Section section = sectionRepository.findById(dto.sectionId()).orElseThrow(() -> new EntityNotFoundException("Section non trouvée: " + dto.sectionId()));
        Semestre semestre = semestreRepository.findById(dto.semestreId()).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé: " + dto.semestreId()));

        SecParamId id = new SecParamId(annee.getId(), filiere.getId(), section.getId(), semestre.getId());
        if(secParamRepository.existsById(id)){
            throw new IllegalArgumentException("Ce paramétrage de section existe déjà.");
        }

        SecParam secParam = new SecParam();
        secParam.setAnnee(annee);
        secParam.setFiliere(filiere);
        secParam.setSection(section);
        secParam.setSemestre(semestre);
        return toDto(secParamRepository.save(secParam));
    }
    public void delete(Integer anneeId, Integer filiereId, Integer sectionId, Integer semestreId) {
        SecParamId id = new SecParamId(anneeId, filiereId, sectionId, semestreId);
        if (!secParamRepository.existsById(id)) { throw new EntityNotFoundException("Lien Sec-Param non trouvé."); }
        secParamRepository.deleteById(id);
    }
}