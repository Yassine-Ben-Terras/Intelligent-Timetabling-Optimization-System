package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

public record DispLocDto(
        @NotNull Integer anneeId,
        @NotNull Integer localId,
        @NotNull Integer sessionId,
        boolean dispo
) {}