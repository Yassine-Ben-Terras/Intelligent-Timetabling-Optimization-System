package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;
public record SecParamDto(@NotNull Integer anneeId, @NotNull Integer filiereId, @NotNull Integer sectionId, @NotNull Integer semestreId) {}