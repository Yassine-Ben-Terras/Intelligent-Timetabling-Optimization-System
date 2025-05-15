package com.Session.EmploiTempsV1.emploi.entities;


import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispLocId implements Serializable {
    private Integer annee;
    private Integer local;
    private Integer session;
}

