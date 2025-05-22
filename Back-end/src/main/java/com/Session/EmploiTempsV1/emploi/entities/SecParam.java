package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SEC_PARAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SecParamId.class)
public class SecParam {

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_ANNEE")
    private AnneeUniversitaire annee;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_FILIERE")
    private Filiere filiere;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_SECTION")
    private Section section;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_SEMESTRE")
    private Semestre semestre;
}
