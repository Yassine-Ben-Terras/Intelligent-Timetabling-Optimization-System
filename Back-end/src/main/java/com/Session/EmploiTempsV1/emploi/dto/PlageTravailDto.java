// FICHIER : com/Session/EmploiTempsV1/emploi/dto/PlageTravailDto.java
package com.Session.EmploiTempsV1.emploi.dto;

import lombok.Data;

@Data
public class PlageTravailDto {
    private Integer id;
    private Integer anneeId;
    private Integer jourId;
    private Integer sessionId;
    private String heureDebut;
    private String heureFin;
}