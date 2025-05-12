package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DEPARTEMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Departement {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_DEPARTEMENT")
    private Integer id;

    @Column(name = "LIBELLE_DEPARTEMENT")
    private String libelle;

}
