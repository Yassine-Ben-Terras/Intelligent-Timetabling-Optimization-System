package com.Session.EmploiTempsV1.emploi.entities;


import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrpParamId implements Serializable {
    private Integer section;
    private Integer groupe;
    private Integer annee;
    private Integer semestre;
}
