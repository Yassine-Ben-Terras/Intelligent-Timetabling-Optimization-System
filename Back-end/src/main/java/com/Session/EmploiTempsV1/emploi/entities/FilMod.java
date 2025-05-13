package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@IdClass(FilModId.class)
@AllArgsConstructor @NoArgsConstructor
public class FilMod {

    @Id
    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @Id
    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module; // This is the correct placement

}
