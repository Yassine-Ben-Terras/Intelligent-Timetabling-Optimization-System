package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.GroupeRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.GroupeResponseDto;
import com.Session.EmploiTempsV1.emploi.dto.ModuleResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.Groupe;
import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import com.Session.EmploiTempsV1.emploi.repository.GroupeRepository;
import com.Session.EmploiTempsV1.emploi.repository.ModuleEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupeService {

    private final GroupeRepository groupeRepository;
    private final ModuleEntityRepository moduleRepository;
    private final ModuleService moduleService;

    public GroupeService(GroupeRepository groupeRepository, ModuleEntityRepository moduleRepository, ModuleService moduleService) {
        this.groupeRepository = groupeRepository;
        this.moduleRepository = moduleRepository;
        this.moduleService = moduleService;
    }

    /**
     * Convertit une entité Groupe en son DTO de réponse.
     */
    public GroupeResponseDto toResponseDto(Groupe groupe) {
        // Conversion de la liste de ModuleEntity en liste de ModuleResponseDto
        List<ModuleResponseDto> moduleDtos = groupe.getModules().stream()
                .map(moduleService::toResponseDto)
                .collect(Collectors.toList());

        return new GroupeResponseDto(
                groupe.getId(),
                groupe.getLibelle(),
                groupe.getNbrEtudiants(),
                moduleDtos // Utilisation de la liste de DTOs de modules
        );
    }

    /**
     * Récupère tous les groupes.
     */
    public List<GroupeResponseDto> findAll() {
        return groupeRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un groupe par son ID.
     */
    public GroupeResponseDto findById(Integer id) {
        return groupeRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé avec l'id: " + id));
    }

    /**
     * Crée un nouveau groupe.
     */
    public GroupeResponseDto create(GroupeRequestDto dto) {
        // Récupérer la liste des entités Module à partir de leurs IDs
        List<ModuleEntity> modules = moduleRepository.findAllById(dto.moduleIds());
        if (modules.size() != dto.moduleIds().size()) {
            throw new EntityNotFoundException("Un ou plusieurs modules n'ont pas été trouvés.");
        }

        Groupe groupe = new Groupe();
        groupe.setLibelle(dto.libelle());
        groupe.setNbrEtudiants(dto.nbrEtudiants());
        groupe.setModules(modules); // Assigner la liste de modules

        Groupe savedGroupe = groupeRepository.save(groupe);
        return toResponseDto(savedGroupe);
    }

    /**
     * Met à jour un groupe existant.
     */
    public GroupeResponseDto update(Integer id, GroupeRequestDto dto) {
        Groupe groupe = groupeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé avec l'id: " + id));

        // Récupérer la nouvelle liste de modules
        List<ModuleEntity> modules = moduleRepository.findAllById(dto.moduleIds());
        if (modules.size() != dto.moduleIds().size()) {
            throw new EntityNotFoundException("Un ou plusieurs modules n'ont pas été trouvés.");
        }

        groupe.setLibelle(dto.libelle());
        groupe.setNbrEtudiants(dto.nbrEtudiants());
        groupe.setModules(modules); // Mettre à jour la liste des modules

        Groupe updatedGroupe = groupeRepository.save(groupe);
        return toResponseDto(updatedGroupe);
    }

    /**
     * Supprime un groupe par son ID.
     */
    public void delete(Integer id) {
        if (!groupeRepository.existsById(id)) {
            throw new EntityNotFoundException("Groupe non trouvé avec l'id: " + id);
        }
        groupeRepository.deleteById(id);
    }
}