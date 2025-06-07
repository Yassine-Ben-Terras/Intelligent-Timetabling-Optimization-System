package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO pour la création/mise à jour d'un professeur
public record ProfesseurRequestDto(
        @NotBlank String nom,
        @NotBlank String prenom,
        String statut,
        @NotNull Integer departementId
) {}