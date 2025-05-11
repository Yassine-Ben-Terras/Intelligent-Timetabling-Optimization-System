package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnneeUniversitaire {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_ANNEE")
    private Integer id;

    @Column(name = "LIBELLE_ANNEE")
    private String libelle;
}
