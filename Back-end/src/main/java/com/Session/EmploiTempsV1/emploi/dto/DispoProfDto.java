package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

// Ce DTO sert à la fois pour la création, la suppression et la lecture.
public record DispoProfDto(
        @NotNull Integer profId,
        @NotNull Integer jourId,
        @NotNull String periode
) {}