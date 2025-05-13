package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MODULES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_MODULE")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_SEMESTRE")
    private Semestre semestre;

    @Column(name = "LIBELLE_MODULE")
    private String libelle;

    @Column(name = "NBR_HEURES_COURS")
    private Integer heuresCours;

    @Column(name = "NBR_HEURES_TD")
    private Integer heuresTD;

    @Column(name = "NBR_HEURES_TP")
    private Integer heuresTP;

    @Column(name = "SEM_DEM_TP")
    private String semDemTP;

    @Column(name = "SEM_DEM_TD")
    private String semDemTD;

    @ManyToOne // Un cours se fait dans un certain type de local (ex: Amphi)
    @JoinColumn(name = "type_local_cours_id", nullable = true) // nullable=true si un cours n'a pas de prérequis
    private TypeLocal typeLocalRequisCours;

    @ManyToOne // Un TD se fait dans un certain type de local (ex: Salle TD)
    @JoinColumn(name = "type_local_td_id", nullable = true)
    private TypeLocal typeLocalRequisTD;

    @ManyToOne // Un TP se fait dans un certain type de local (ex: Labo Info)
    @JoinColumn(name = "type_local_tp_id", nullable = true)
    private TypeLocal typeLocalRequisTP;


}

