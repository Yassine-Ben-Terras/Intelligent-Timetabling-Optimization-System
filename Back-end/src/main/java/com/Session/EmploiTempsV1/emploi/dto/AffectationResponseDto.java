package com.Session.EmploiTempsV1.emploi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AffectationResponseDto(
        Long id,
        Integer professeurId,
        String professeurNom,
        Integer moduleId,
        String moduleLibelle,
        Integer typeSeanceId,
        String typeSeanceLibelle,
        Integer sectionId,
        String sectionLibelle,
        Integer groupeId,
        String groupeLibelle
) {}