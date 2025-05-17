package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SESSION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_SESSION")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_ANNEE")
    private AnneeUniversitaire annee;

    @Column(name = "LIBELLE_SESSION")
    private String libelle;

}
