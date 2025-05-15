package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DISP_LOC")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DispLocId.class)
public class DispLoc {

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_ANNEE")
    private AnneeUniversitaire annee;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_LOCALE")
    private Local local;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_SESSION")
    private Session session;

    @Column(name = "DISPO")
    private boolean dispo;


}

