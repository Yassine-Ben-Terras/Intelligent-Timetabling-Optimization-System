package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LOCAUX")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Local {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_LOCALE")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_TYPE")
    private TypeLocal type;

    @Column(name = "LIBELLE_LOCALE")
    private String libelle;

    @Column(name = "CAPACITE")
    private Integer capacite;
}
