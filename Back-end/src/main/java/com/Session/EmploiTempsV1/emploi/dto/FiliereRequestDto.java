package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO pour la création et la mise à jour
public record FiliereRequestDto(
        @NotBlank String codeFiliere,
        @NotBlank String libelle,
        @NotNull Integer departementId
) {}