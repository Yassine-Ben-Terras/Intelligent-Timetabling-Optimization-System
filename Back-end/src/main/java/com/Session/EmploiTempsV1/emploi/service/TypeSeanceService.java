package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.TypeSeanceDto;
import com.Session.EmploiTempsV1.emploi.entities.TypeSeance;
import com.Session.EmploiTempsV1.emploi.repository.TypeSeanceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TypeSeanceService {

    private final TypeSeanceRepository repository;

    public TypeSeanceService(TypeSeanceRepository repository) {
        this.repository = repository;
    }

    /**
     * Méthode utilitaire pour convertir une entité TypeSeance en son DTO.
     */
    public TypeSeanceDto toDto(TypeSeance entity) {
        return new TypeSeanceDto(entity.getId(), entity.getLibelle());
    }

    /**
     * Récupère tous les types de séance.
     * @return Une liste de TypeSeanceDto.
     */
    public List<TypeSeanceDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Trouve un type de séance par son ID.
     * @param id L'identifiant du type de séance.
     * @return Le TypeSeanceDto correspondant.
     * @throws EntityNotFoundException si aucun type de séance n'est trouvé.
     */
    public TypeSeanceDto findById(Integer id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("TypeSeance non trouvé: " + id));
    }

    /**
     * Crée un nouveau type de séance.
     * @param dto Le DTO contenant les informations pour la création.
     * @return Le TypeSeanceDto de l'entité créée.
     */
    public TypeSeanceDto create(TypeSeanceDto dto) {
        TypeSeance entity = new TypeSeance();
        entity.setLibelle(dto.getLibelle());
        return toDto(repository.save(entity));
    }

    /**
     * Met à jour un type de séance existant.
     * @param id L'identifiant du type de séance à mettre à jour.
     * @param dto Le DTO contenant les nouvelles informations.
     * @return Le TypeSeanceDto de l'entité mise à jour.
     * @throws EntityNotFoundException si aucun type de séance n'est trouvé.
     */
    public TypeSeanceDto update(Integer id, TypeSeanceDto dto) {
        TypeSeance entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TypeSeance non trouvé: " + id));
        entity.setLibelle(dto.getLibelle());
        return toDto(repository.save(entity));
    }

    /**
     * Supprime un type de séance par son ID.
     * @param id L'identifiant du type de séance à supprimer.
     * @throws EntityNotFoundException si aucun type de séance n'est trouvé.
     */
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("TypeSeance non trouvé: " + id);
        }
        repository.deleteById(id);
    }
}