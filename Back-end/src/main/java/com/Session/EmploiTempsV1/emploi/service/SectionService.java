package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.SectionDto;
import com.Session.EmploiTempsV1.emploi.entities.Section;
import com.Session.EmploiTempsV1.emploi.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {
    private final SectionRepository repository;
    public SectionService(SectionRepository repository) { this.repository = repository; }
    public SectionDto toDto(Section entity) { return new SectionDto(entity.getId(), entity.getLibelle(), entity.getNbrEtudiants()); }
    public List<SectionDto> findAll() { return repository.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public SectionDto findById(Integer id) { return repository.findById(id).map(this::toDto).orElseThrow(() -> new EntityNotFoundException("Section non trouvée: " + id)); }
    public SectionDto create(SectionDto dto) {
        Section entity = new Section();
        entity.setLibelle(dto.libelle());
        entity.setNbrEtudiants(dto.nbrEtudiants());
        return toDto(repository.save(entity));
    }
    public SectionDto update(Integer id, SectionDto dto) {
        Section entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Section non trouvée: " + id));
        entity.setLibelle(dto.libelle());
        entity.setNbrEtudiants(dto.nbrEtudiants());
        return toDto(repository.save(entity));
    }
    public void delete(Integer id) {
        if (!repository.existsById(id)) { throw new EntityNotFoundException("Section non trouvée: " + id); }
        repository.deleteById(id);
    }
}