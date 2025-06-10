package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;
public record SemestreRequestDto(String libelle, @NotNull Integer sessionId) {}