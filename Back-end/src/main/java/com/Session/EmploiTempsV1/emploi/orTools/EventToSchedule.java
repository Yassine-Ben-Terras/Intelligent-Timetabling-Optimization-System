package com.Session.EmploiTempsV1.emploi.orTools;

import com.Session.EmploiTempsV1.emploi.entities.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(of = "id")
public class EventToSchedule {

    private final String id;
    private final List<AffectationEnseignant> affectations;
    private final int durationInMinutes;
    private final TypeLocal requiredRoomType;
    private final int requiredCapacity;
    private final Regroupement regroupement; // Remplacé Regroupe par Regroupement. Peut être null.

    // Champs dérivés pour un accès facile
    private final Set<Professeur> professeurs;
    private final Set<Groupe> groupes;
    private final Set<Section> sections;
    private final ModuleEntity module;
    private final TypeSeance typeSeance;

    public EventToSchedule(String id, List<AffectationEnseignant> affectations, Regroupement regroupement) {
        if (affectations == null || affectations.isEmpty()) {
            throw new IllegalArgumentException("Un événement doit avoir au moins une affectation.");
        }
        this.id = id;
        this.affectations = affectations;
        this.regroupement = regroupement;

        // Toutes les affectations d'un événement partagent le même module, type de séance et durée.
        AffectationEnseignant firstAff = affectations.getFirst();
        this.module = firstAff.getModule();
        this.typeSeance = firstAff.getTypeSeance();

        // Calcul de la durée
        // NOTE: module.getHeuresCours/TD/TP() stocke la valeur du volume horaire de l'Excel.
        // Cette valeur DOIT représenter la durée d'une séance hebdomadaire EN MINUTES (ex: 90).
        // Si elle représente le volume total du semestre en heures (ex: 42), les résultats seront incorrects.
        int rawDuration;
        switch (typeSeance.getLibelle().toUpperCase()) {
            case "COURS":
                rawDuration = module.getHeuresCours() != null ? module.getHeuresCours() : 0;
                this.requiredRoomType = module.getTypeLocalRequisCours();
                break;
            case "TD":
                rawDuration = module.getHeuresTD() != null ? module.getHeuresTD() : 0;
                this.requiredRoomType = module.getTypeLocalRequisTD();
                break;
            case "TP":
                rawDuration = module.getHeuresTP() != null ? module.getHeuresTP() : 0;
                this.requiredRoomType = module.getTypeLocalRequisTP();
                break;
            default:
                throw new IllegalStateException("Type de séance non géré : " + typeSeance.getLibelle());
        }
        // FIX Bug 2.3: Protection contre les valeurs nulles ou invalides
        if (rawDuration <= 0) {
            System.err.println("AVERTISSEMENT: Module '" + module.getLibelle()
                    + "' a une durée <= 0 pour " + typeSeance.getLibelle()
                    + ". Valeur par défaut de 90 min utilisée.");
            rawDuration = 90;
        }
        this.durationInMinutes = rawDuration;

        // Agrégation des informations
        this.professeurs = affectations.stream().map(AffectationEnseignant::getProfesseur).collect(Collectors.toSet());
        this.groupes = affectations.stream().map(AffectationEnseignant::getGroupe).filter(Objects::nonNull).collect(Collectors.toSet());
        this.sections = affectations.stream().map(AffectationEnseignant::getSection).filter(Objects::nonNull).collect(Collectors.toSet());

        // La capacité requise est la somme des capacités de chaque entité (groupe/section)
        int capacityFromGroups = groupes.stream().mapToInt(g -> g.getNbrEtudiants() != null ? g.getNbrEtudiants() : 0).sum();
        int capacityFromSections = sections.stream().mapToInt(s -> s.getNbrEtudiants() != null ? s.getNbrEtudiants() : 0).sum();
        this.requiredCapacity = Math.max(capacityFromGroups, capacityFromSections);
    }
}