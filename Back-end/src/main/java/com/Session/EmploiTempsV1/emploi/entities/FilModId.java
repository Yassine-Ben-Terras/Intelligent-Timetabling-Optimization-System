package com.Session.EmploiTempsV1.emploi.entities;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilModId implements Serializable {

    private Integer filiere;
    private Integer module;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilModId)) return false;
        FilModId that = (FilModId) o;
        return Objects.equals(filiere, that.filiere) &&
                Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filiere, module);
    }

    public Integer getFiliere() { return filiere; }
    public void setFiliere(Integer filiere) { this.filiere = filiere; }
    public Integer getModule() { return module; }
    public void setModule(Integer module) { this.module = module; }


}
