package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SECTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_SECTION")
    private Integer id;

    @Column(name = "LIBELLE_SECTION")
    private String libelle;

    @Column(name = "NBR_SEC_ETUDIANTS")
    private Integer nbrEtudiants;

}
