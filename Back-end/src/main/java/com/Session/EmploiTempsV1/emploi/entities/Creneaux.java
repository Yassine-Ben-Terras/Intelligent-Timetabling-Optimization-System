package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creneaux {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_CRENEAU")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_ANNEE")
    private AnneeUniversitaire annee;

    @ManyToOne
    @JoinColumn(name = "ID_JOURS")
    private Jour jour;

    @ManyToOne
    @JoinColumn(name = "ID_SESSION")
    private Session session;

    @Column(name = "HEURE_DEBUT")
    private String heureDebut;

    @Column(name = "HEURE_FIN")
    private String heureFin;

    @Column(name = "NBR_HEURES")
    private Integer nbrHeures;
}

