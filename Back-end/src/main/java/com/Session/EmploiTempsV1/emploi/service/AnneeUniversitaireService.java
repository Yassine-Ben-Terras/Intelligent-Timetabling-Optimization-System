package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.AnneeUniversitaireDto;
import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import com.Session.EmploiTempsV1.emploi.repository.AnneeUniversitaireRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnneeUniversitaireService {

    private final AnneeUniversitaireRepository anneeRepository;

    public AnneeUniversitaireService(AnneeUniversitaireRepository anneeRepository) {
        this.anneeRepository = anneeRepository;
    }

    AnneeUniversitaireDto toDto(AnneeUniversitaire annee) {
        return new AnneeUniversitaireDto(annee.getId(), annee.getLibelle());
    }

    public List<AnneeUniversitaireDto> findAll() {
        return anneeRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public AnneeUniversitaireDto findById(Integer id) {
        return anneeRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Année non trouvée: " + id));
    }

    public AnneeUniversitaireDto create(AnneeUniversitaireDto dto) {
        AnneeUniversitaire annee = new AnneeUniversitaire();
        annee.setLibelle(dto.libelle());
        return toDto(anneeRepository.save(annee));
    }

    public AnneeUniversitaireDto update(Integer id, AnneeUniversitaireDto dto) {
        AnneeUniversitaire annee = anneeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Année non trouvée: " + id));
        annee.setLibelle(dto.libelle());
        return toDto(anneeRepository.save(annee));
    }

    public void delete(Integer id) {
        if (!anneeRepository.existsById(id)) {
            throw new EntityNotFoundException("Année non trouvée: " + id);
        }
        anneeRepository.deleteById(id);
    }
}