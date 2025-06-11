package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

public record CreneauxRequestDto(
        @NotNull Integer anneeId,
        @NotNull Integer jourId,
        @NotNull Integer sessionId,
        String heureDebut,
        String heureFin,
        Integer nbrHeures
) {}