package com.Session.EmploiTempsV1.emploi.entities;


import lombok.*;
import java.io.Serializable;
import java.util.Objects;
@Data
public class ModSecId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer module;  // Doit correspondre au nom du champ 'module' dans ModSec
    // et son type est l'ID de ModuleEntity
    private Integer section; // Doit correspondre au nom du champ 'section' dans ModSec
    // et son type est l'ID de Section

    // Constructeur sans argument (requis par JPA)
    public ModSecId() {
    }

    // Votre constructeur demandé
    public ModSecId(ModuleEntity moduleEntity, Section sectionEntity) {
        if (moduleEntity != null) {
            this.module = moduleEntity.getId(); // On stocke l'ID du module
        }
        if (sectionEntity != null) {
            this.section = sectionEntity.getId(); // On stocke l'ID de la section
        }
    }

    // Constructeur alternatif
    public ModSecId(Integer moduleId, Integer sectionId) {
        this.module = moduleId;
        this.section = sectionId;
    }

    // Getters et Setters
    public Integer getModule() {
        return module;
    }

    public void setModule(Integer module) {
        this.module = module;
    }

    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

    // equals() et hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModSecId modSecId = (ModSecId) o;
        return Objects.equals(module, modSecId.module) &&
                Objects.equals(section, modSecId.section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, section);
    }
}