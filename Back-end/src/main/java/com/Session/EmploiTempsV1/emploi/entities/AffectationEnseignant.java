package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AFFECTATIONS_ENSEIGNANTS", uniqueConstraints = {
        // Ajout d'une contrainte d'unicité pour éviter les doublons logiques.
        // Par exemple, on ne peut pas affecter le même prof, au même module/groupe/type de séance deux fois.
        @UniqueConstraint(columnNames = {"professeur_id", "module_id", "type_seance_id", "groupe_id"}),
        @UniqueConstraint(columnNames = {"professeur_id", "module_id", "type_seance_id", "section_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Le Builder est très pratique pour la création dans le service
public class AffectationEnseignant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_seance_id", nullable = false)
    private TypeSeance typeSeance;

    // Une affectation concerne SOIT une section, SOIT un groupe.
    @ManyToOne(optional = true)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(optional = true)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;
}