package com.Session.EmploiTempsV1.emploi.service;

// ... imports
import com.Session.EmploiTempsV1.emploi.dto.TypeLocalDto;
import com.Session.EmploiTempsV1.emploi.entities.TypeLocal;
import com.Session.EmploiTempsV1.emploi.repository.TypeLocalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TypeLocalService {
    private final TypeLocalRepository repository;
    public TypeLocalService(TypeLocalRepository repository) { this.repository = repository; }

    public TypeLocalDto toDto(TypeLocal entity) {
        if (entity == null) {
            return null; // retourne null si le TypeLocal n'existe pas
        }
        return new TypeLocalDto(entity.getId(), entity.getLibelle());
    }

    public List<TypeLocalDto> findAll() { return repository.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public TypeLocalDto findById(Integer id) { return repository.findById(id).map(this::toDto).orElseThrow(() -> new EntityNotFoundException("TypeLocal non trouvé: " + id)); }
    public TypeLocalDto create(TypeLocalDto dto) {
        TypeLocal entity = new TypeLocal();
        entity.setLibelle(dto.libelle());
        return toDto(repository.save(entity));
    }
    public TypeLocalDto update(Integer id, TypeLocalDto dto) {
        TypeLocal entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("TypeLocal non trouvé: " + id));
        entity.setLibelle(dto.libelle());
        return toDto(repository.save(entity));
    }
    public void delete(Integer id) {
        if (!repository.existsById(id)) { throw new EntityNotFoundException("TypeLocal non trouvé: " + id); }
        repository.deleteById(id);
    }
}