package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SEMESTRE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Semestre {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_SEMESTRE")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_SESSION")
    private Session session;

    @Column(name = "LIBELLE_SEMESTRE")
    private String libelle;

}
