package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.AffectationPayloadDto;
import com.Session.EmploiTempsV1.emploi.dto.AffectationResponseDto;
import com.Session.EmploiTempsV1.emploi.dto.AffectationSyncRequestDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AffectationEnseignantService {

    private final AffectationEnseignantRepository affectationRepository;
    private final ProfesseurRepository professeurRepository;
    private final ModuleEntityRepository moduleRepository;
    private final TypeSeanceRepository typeSeanceRepository;
    private final SectionRepository sectionRepository;
    private final GroupeRepository groupeRepository;

    public AffectationEnseignantService(AffectationEnseignantRepository affectationRepository, ProfesseurRepository professeurRepository, ModuleEntityRepository moduleRepository, TypeSeanceRepository typeSeanceRepository, SectionRepository sectionRepository, GroupeRepository groupeRepository) {
        this.affectationRepository = affectationRepository;
        this.professeurRepository = professeurRepository;
        this.moduleRepository = moduleRepository;
        this.typeSeanceRepository = typeSeanceRepository;
        this.sectionRepository = sectionRepository;
        this.groupeRepository = groupeRepository;
    }

    // Le convertisseur vers le DTO de réponse (rendu public pour le contrôleur)
    public AffectationResponseDto toDto(AffectationEnseignant entity) {
        return new AffectationResponseDto(
                entity.getId(),
                entity.getProfesseur().getId(),
                entity.getProfesseur().getNom() + " " + entity.getProfesseur().getPrenom(),
                entity.getModule().getId(),
                entity.getModule().getLibelle(),
                entity.getTypeSeance().getId(),
                entity.getTypeSeance().getLibelle(),
                entity.getSection() != null ? entity.getSection().getId() : null,
                entity.getSection() != null ? entity.getSection().getLibelle() : null,
                entity.getGroupe() != null ? entity.getGroupe().getId() : null,
                entity.getGroupe() != null ? entity.getGroupe().getLibelle() : null
        );
    }

    @Transactional(readOnly = true)
    public List<AffectationResponseDto> findAll() {
        return affectationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     *  Création d'une seule affectation à partir du payload reçu du frontend.
     */
    @Transactional
    public AffectationResponseDto create(AffectationPayloadDto dto) {
        if ((dto.sectionId() == null && dto.groupeId() == null) || (dto.sectionId() != null && dto.groupeId() != null)) {
            throw new IllegalArgumentException("Une affectation doit concerner soit une section, soit un groupe.");
        }

        Professeur professeur = professeurRepository.findById(dto.professeurId())
                .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé avec l'id: " + dto.professeurId()));
        ModuleEntity module = moduleRepository.findById(dto.moduleId())
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec l'id: " + dto.moduleId()));
        TypeSeance typeSeance = typeSeanceRepository.findById(dto.typeSeanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type de séance non trouvé avec l'id: " + dto.typeSeanceId()));

        Section section = null;
        if (dto.sectionId() != null) {
            section = sectionRepository.findById(dto.sectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section non trouvée avec l'id: " + dto.sectionId()));
        }

        Groupe groupe = null;
        if (dto.groupeId() != null) {
            groupe = groupeRepository.findById(dto.groupeId())
                    .orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé avec l'id: " + dto.groupeId()));
        }

        AffectationEnseignant nouvelleAffectation = AffectationEnseignant.builder()
                .professeur(professeur)
                .module(module)
                .typeSeance(typeSeance)
                .section(section)
                .groupe(groupe)
                .build();

        AffectationEnseignant affectationSauvegardee = affectationRepository.save(nouvelleAffectation);
        return toDto(affectationSauvegardee);
    }

    /**
     * Supprime une affectation par son identifiant.
     */
    @Transactional
    public void deleteById(Long id) {
        if (!affectationRepository.existsById(id)) {
            throw new EntityNotFoundException("Affectation non trouvée avec l'id: " + id);
        }
        affectationRepository.deleteById(id);
    }

    @Transactional
    public void synchroniserAffectations(AffectationSyncRequestDto dto) {
        // --- 1. Validation et récupération des entités de base ---
        ModuleEntity module = moduleRepository.findById(dto.moduleId())
                .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec l'id: " + dto.moduleId()));
        TypeSeance typeSeance = typeSeanceRepository.findById(dto.typeSeanceId())
                .orElseThrow(() -> new EntityNotFoundException("Type de séance non trouvé avec l'id: " + dto.typeSeanceId()));

        if ((dto.sectionId() == null && dto.groupeId() == null) || (dto.sectionId() != null && dto.groupeId() != null)) {
            throw new IllegalArgumentException("Une affectation doit concerner soit une section, soit un groupe, mais pas les deux.");
        }

        // --- 2. Trouver les affectations existantes pour cet enseignement ---
        List<AffectationEnseignant> affectationsExistantes;
        Section section = null;
        Groupe groupe = null;

        if (dto.sectionId() != null) {
            section = sectionRepository.findById(dto.sectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section non trouvée avec l'id: " + dto.sectionId()));
            affectationsExistantes = affectationRepository.findByModuleAndTypeSeanceAndSection(module, typeSeance, section);
        } else {
            groupe = groupeRepository.findById(dto.groupeId())
                    .orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé avec l'id: " + dto.groupeId()));

            boolean moduleIsInGroupe = groupe.getModules().stream().anyMatch(m -> m.getId().equals(module.getId()));
            if (!moduleIsInGroupe) {
                throw new IllegalArgumentException("Le groupe '" + groupe.getLibelle() + "' n'est pas associé au module '" + module.getLibelle() + "'.");
            }
            affectationsExistantes = affectationRepository.findByModuleAndTypeSeanceAndGroupe(module, typeSeance, groupe);
        }

        List<Integer> idsProfesseursExistants = affectationsExistantes.stream()
                .map(affectation -> affectation.getProfesseur().getId())
                .collect(Collectors.toList());

        List<Integer> idsProfesseursSouhaites = dto.professeurIds();

        // --- 3. Déterminer les affectations à supprimer ---
        List<AffectationEnseignant> affectationsASupprimer = affectationsExistantes.stream()
                .filter(affectation -> !idsProfesseursSouhaites.contains(affectation.getProfesseur().getId()))
                .collect(Collectors.toList());

        // --- 4. Déterminer les ID des professeurs à ajouter ---
        List<Integer> idsProfesseursAAjouter = idsProfesseursSouhaites.stream()
                .filter(id -> !idsProfesseursExistants.contains(id))
                .collect(Collectors.toList());

        if (!idsProfesseursAAjouter.isEmpty()) {
            List<Professeur> professeursAAjouter = professeurRepository.findAllById(idsProfesseursAAjouter);
            if(professeursAAjouter.size() != idsProfesseursAAjouter.size()){
                throw new EntityNotFoundException("Un ou plusieurs professeurs à ajouter n'ont pas été trouvés.");
            }

            final Section finalSection = section;
            final Groupe finalGroupe = groupe;

            List<AffectationEnseignant> affectationsACreer = professeursAAjouter.stream().map(prof ->
                    AffectationEnseignant.builder()
                            .professeur(prof)
                            .module(module)
                            .typeSeance(typeSeance)
                            .section(finalSection)
                            .groupe(finalGroupe)
                            .build()
            ).collect(Collectors.toList());

            affectationRepository.saveAll(affectationsACreer);
        }

        // --- 5. Appliquer la suppression ---
        if (!affectationsASupprimer.isEmpty()) {
            affectationRepository.deleteAll(affectationsASupprimer);
        }
    }
}