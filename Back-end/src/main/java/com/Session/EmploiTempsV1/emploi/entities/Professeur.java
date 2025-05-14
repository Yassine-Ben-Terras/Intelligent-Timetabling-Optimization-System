package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PROFESSEURS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Professeur {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PROF")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_DEPARTEMENT")
    private Departement departement;

    @Column(name = "NOM")
    private String nom;

    @Column(name = "PRENOM")
    private String prenom;

    @Column(name = "STATUT")
    private String statut;

}

