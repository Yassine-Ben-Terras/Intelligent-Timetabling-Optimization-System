package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DISPO_PROF") // Assurez-vous que le nom de la table est correct
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DispoProfId.class) // Cette ligne est correcte
public class DispoProf {

    @Id // Marque ce champ comme faisant partie de la clé primaire
    @ManyToOne
    @JoinColumn(name = "ID_PROF")
    private Professeur prof;

    @Id // Marque ce champ comme faisant partie de la clé primaire
    @ManyToOne
    @JoinColumn(name = "ID_JOUR")
    private Jour jour;

    @Id
    @Column(name = "PERIODE", length = 10)
    private String periode;
}


