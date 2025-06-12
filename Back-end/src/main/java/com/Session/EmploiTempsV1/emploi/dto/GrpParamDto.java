package com.Session.EmploiTempsV1.emploi.dto;

// Ce DTO est utilisé pour AFFICHER les données (GET). Il est riche en informations.
public record GrpParamDto(
        String key, // "sectionId-groupeId-anneeId-semestreId"
        Integer sectionId,
        String sectionLibelle,
        Integer groupeId,
        String groupeLibelle,
        Integer anneeId,
        String anneeLibelle,
        Integer semestreId,
        String semestreLibelle
) {}