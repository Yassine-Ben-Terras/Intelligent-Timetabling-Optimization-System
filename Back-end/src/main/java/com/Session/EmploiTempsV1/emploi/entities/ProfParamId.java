// FICHIER MODIFIÉ : ProfParamId.java
package com.Session.EmploiTempsV1.emploi.entities;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfParamId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer module;
    private Integer professeur;
    private Integer typeSeance; // <-- MODIFIÉ : C'est maintenant l'ID de l'entité TypeSeance

    // equals() et hashCode() mis à jour
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfParamId that = (ProfParamId) o;
        return Objects.equals(module, that.module) &&
                Objects.equals(professeur, that.professeur) &&
                Objects.equals(typeSeance, that.typeSeance); // <-- MODIFIÉ
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, professeur, typeSeance); // <-- MODIFIÉ
    }
}