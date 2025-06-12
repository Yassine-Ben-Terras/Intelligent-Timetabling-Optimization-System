package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;

// Ce DTO est utilisé UNIQUEMENT pour la création via le corps de la requête POST.
public record GrpParamCreateDto(
        @NotNull Integer sectionId,
        @NotNull Integer groupeId,
        @NotNull Integer anneeId,
        @NotNull Integer semestreId
) {}