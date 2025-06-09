package com.Session.EmploiTempsV1.emploi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegroupementDetailDto {
    private Long id;
    private Integer sectionId;
    private String sectionLibelle; // Pour l'affichage
    private Integer groupeId;
    private String groupeLibelle; // Pour l'affichage
    private Integer moduleId;
    private String moduleLibelle; // Pour l'affichage
}