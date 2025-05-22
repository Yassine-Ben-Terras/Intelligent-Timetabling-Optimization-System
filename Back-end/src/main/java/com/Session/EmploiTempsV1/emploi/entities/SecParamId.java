package com.Session.EmploiTempsV1.emploi.entities;


import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecParamId implements Serializable {
    private Integer annee;
    private Integer filiere;
    private Integer section;
    private Integer semestre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecParamId that = (SecParamId) o;
        return Objects.equals(annee, that.annee) &&
                Objects.equals(filiere, that.filiere) &&
                Objects.equals(section, that.section) &&
                Objects.equals(semestre, that.semestre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annee, filiere, section, semestre);
    }

}

