package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.JourDto;
import com.Session.EmploiTempsV1.emploi.entities.Jour;
import com.Session.EmploiTempsV1.emploi.repository.JourRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JourService {
    private final JourRepository jourRepository;
    public JourService(JourRepository jourRepository) { this.jourRepository = jourRepository; }
    public JourDto toDto(Jour jour) { return new JourDto(jour.getId(), jour.getLibelle()); }

    public List<JourDto> findAll() { return jourRepository.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public JourDto findById(Integer id) { return jourRepository.findById(id).map(this::toDto).orElseThrow(() -> new EntityNotFoundException("Jour non trouvé: " + id)); }
    public JourDto create(JourDto dto) {
        Jour jour = new Jour();
        jour.setLibelle(dto.libelle());
        return toDto(jourRepository.save(jour));
    }
    public JourDto update(Integer id, JourDto dto) {
        Jour jour = jourRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Jour non trouvé: " + id));
        jour.setLibelle(dto.libelle());
        return toDto(jourRepository.save(jour));
    }
    public void delete(Integer id) {
        if (!jourRepository.existsById(id)) { throw new EntityNotFoundException("Jour non trouvé: " + id); }
        jourRepository.deleteById(id);
    }
}