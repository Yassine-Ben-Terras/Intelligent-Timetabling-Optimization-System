// FICHIER MODIFIÉ : ProfParam.java
package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@IdClass(ProfParamId.class)
public class ProfParam {

    @Id
    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @Id
    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private Professeur professeur;

    @Id // Fait toujours partie de la clé
    @ManyToOne // <-- MODIFIÉ : C'est maintenant une relation
    @JoinColumn(name = "type_seance_id") // Le nom de la colonne de clé étrangère
    private TypeSeance typeSeance;

    // Constructeur mis à jour
    public ProfParam(ModuleEntity module, Professeur professeur, TypeSeance typeSeance) {
        this.module = module;
        this.professeur = professeur;
        this.typeSeance = typeSeance;
    }
}