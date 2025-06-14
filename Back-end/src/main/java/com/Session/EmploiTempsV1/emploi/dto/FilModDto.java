package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;
public record FilModDto(@NotNull Integer filiereId, @NotNull Integer moduleId) {}