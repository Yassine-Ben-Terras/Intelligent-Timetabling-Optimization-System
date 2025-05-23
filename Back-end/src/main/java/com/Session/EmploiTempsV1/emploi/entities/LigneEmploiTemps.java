package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LIGNES_EMPLOI_TEMPS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneEmploiTemps {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NUM_LIGNE")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_LOCALE")
    private Local local;

    @ManyToOne
    @JoinColumn(name = "ID_MODULE")
    private ModuleEntity moduleEntity;

    @ManyToOne
    @JoinColumn(name = "ID_GROUPE")
    private Groupe groupe;

    @ManyToOne
    @JoinColumn(name = "ID_CRENEAU")
    private Creneaux creneaux;

    @ManyToOne
    @JoinColumn(name = "ID_PROF")
    private Professeur prof;

    @ManyToOne
    @JoinColumn(name = "ID_SECTION")
    private Section section;

    @ManyToOne
    @JoinColumn(name = "type_seance_id") // La colonne qui stockera l'ID du type de séance
    private TypeSeance typeSeance;

}