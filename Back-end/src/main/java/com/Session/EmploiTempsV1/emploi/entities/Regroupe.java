package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "REGROUPEMENTTS")
@Data
@NoArgsConstructor
public class Regroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_regroupement", nullable = false)
    private TypeRegroupement type; // SECTION ou GROUPE

    // Liste des sections (uniquement si type = SECTION)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "regroupement_sections",
            joinColumns = @JoinColumn(name = "regroupement_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Set<Section> sections = new HashSet<>();

    // Liste des groupes (uniquement si type = GROUPE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "regroupement_groupes",
            joinColumns = @JoinColumn(name = "regroupement_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id")
    )
    private Set<Groupe> groupes = new HashSet<>();

    // Constructeur pratique
    public Regroupe(String nom, TypeRegroupement type) {
        this.nom = nom;
        this.type = type;
    }

    /**
     * Validation métier : empêche de mélanger sections et groupes.
     */
    @PrePersist
    @PreUpdate
    private void validateRegroupement() {
        if (type == TypeRegroupement.SECTION && !groupes.isEmpty()) {
            throw new IllegalStateException("Un regroupement de type SECTION ne peut pas contenir de groupes.");
        }
        if (type == TypeRegroupement.GROUPE && !sections.isEmpty()) {
            throw new IllegalStateException("Un regroupement de type GROUPE ne peut pas contenir de sections.");
        }
    }
}
