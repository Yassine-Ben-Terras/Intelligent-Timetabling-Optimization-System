package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FILIERES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_FILIERE")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_DEPARTEMENT")
    private Departement departement;

    @Column(name = "CODE_FILIERE")
    private String codeFiliere;

    @Column(name = "LIBELLE_FILIERES")
    private String libelle;

}