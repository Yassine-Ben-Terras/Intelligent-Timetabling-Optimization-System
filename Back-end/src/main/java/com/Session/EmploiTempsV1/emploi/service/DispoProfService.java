package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.DispoProfDto;
import com.Session.EmploiTempsV1.emploi.entities.Jour;
import com.Session.EmploiTempsV1.emploi.entities.DispoProf;
import com.Session.EmploiTempsV1.emploi.entities.DispoProfId;
import com.Session.EmploiTempsV1.emploi.entities.Professeur;
import com.Session.EmploiTempsV1.emploi.repository.JourRepository;
import com.Session.EmploiTempsV1.emploi.repository.DispoProfRepository;
import com.Session.EmploiTempsV1.emploi.repository.ProfesseurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DispoProfService {

    private final DispoProfRepository dispoProfRepository;
    private final ProfesseurRepository professeurRepository;
    private final JourRepository jourRepository;

    public DispoProfService(DispoProfRepository dispoProfRepository, ProfesseurRepository professeurRepository, JourRepository jourRepository) {
        this.dispoProfRepository = dispoProfRepository;
        this.professeurRepository = professeurRepository;
        this.jourRepository = jourRepository;
    }

    private DispoProfDto toDto(DispoProf dispoProf) {
        return new DispoProfDto(
                dispoProf.getProf().getId(),
                dispoProf.getJour().getId(),
                dispoProf.getPeriode()
        );
    }

    public List<DispoProfDto> findAll() {
        return dispoProfRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public DispoProfDto findById(Integer profId, Integer jourId, String periode) {
        DispoProfId id = new DispoProfId(profId, jourId, periode);
        return dispoProfRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Disponibilité non trouvée pour profId=" + profId + " et jourId=" + jourId + " et periode=" + periode));
    }

    public DispoProfDto create(DispoProfDto dto) {
        Professeur prof = professeurRepository.findById(dto.profId())
                .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé avec l'id : " + dto.profId()));

        Jour jour = jourRepository.findById(dto.jourId())
                .orElseThrow(() -> new EntityNotFoundException("Jour non trouvé avec l'id : " + dto.jourId()));

        DispoProf dispoProf = new DispoProf();
        dispoProf.setProf(prof);
        dispoProf.setJour(jour);
        dispoProf.setPeriode(dto.periode());

        DispoProf savedDispo = dispoProfRepository.save(dispoProf);
        return toDto(savedDispo);
    }

    public void delete(Integer profId, Integer jourId, String periode) {
        DispoProfId id = new DispoProfId(profId, jourId, periode);
        if (!dispoProfRepository.existsById(id)) {
            throw new EntityNotFoundException("Disponibilité non trouvée pour profId=" + profId + " et jourId=" + jourId + " et periode=" + periode);
        }
        dispoProfRepository.deleteById(id);
    }
}