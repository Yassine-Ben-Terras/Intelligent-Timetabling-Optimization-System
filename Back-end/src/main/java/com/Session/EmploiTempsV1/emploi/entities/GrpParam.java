package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "GRP_PARAM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GrpParamId.class)
public class GrpParam {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_section")
    private Section section;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_GROUPE")
    private Groupe groupe;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_ANNEE")
    private AnneeUniversitaire annee;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_SEMESTRE")
    private Semestre semestre;
}

