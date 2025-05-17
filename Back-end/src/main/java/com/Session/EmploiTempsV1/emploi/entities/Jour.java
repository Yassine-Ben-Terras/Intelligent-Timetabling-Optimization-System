package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "JOURS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jour {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_JOURS")
    private Integer id;

    @Column(name = "LIBELLE_JOURS")
    private String libelle;
}
