package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TYPE_LOCAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeLocal {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)

    @Column(name = "ID_TYPE")
    private Integer id;

    @Column(name = "LIBELLE_TYPE")
    private String libelle;
}
