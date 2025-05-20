// NOUVEAU FICHIER : TypeSeance.java
package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TYPE_SEANCE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeSeance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_TYPE_SEANCE")
    private Integer id;

    @Column(name = "LIBELLE_TYPE_SEANCE", unique = true, nullable = false)
    private String libelle; // "COURS", "TD", "TP"...
}