package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List; // Importer List

@Entity
@Table(name = "GROUPES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Groupe {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_GROUPE")
    private Integer id;

    // La relation est maintenant une liste de modules
    @ManyToMany(fetch = FetchType.LAZY) // Utiliser FetchType.LAZY est une bonne pratique
    @JoinTable(
            name = "groupe_module", // Nom de la table de liaison
            joinColumns = @JoinColumn(name = "id_groupe"), // Clé étrangère vers Groupe
            inverseJoinColumns = @JoinColumn(name = "id_module") // Clé étrangère vers ModuleEntity
    )
    private List<ModuleEntity> modules; // Changé de "moduleEntity" à "modules" et de type ModuleEntity à List<ModuleEntity>

    @Column(name = "LIBELLE_GROUPE")
    private String libelle;

    @Column(name = "NBR_GRP_ETUDIANTS")
    private Integer nbrEtudiants;
}