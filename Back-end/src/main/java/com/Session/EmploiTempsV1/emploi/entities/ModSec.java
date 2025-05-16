package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ModSecId.class)
public class ModSec {

    @Id
    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @Id
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;








    // Getters et Setters (si non générés par Lombok)
    public ModuleEntity getModule() {
        return module;
    }

    public void setModule(ModuleEntity module) {
        this.module = module;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }



}

