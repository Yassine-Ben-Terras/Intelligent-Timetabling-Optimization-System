package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;
public record SessionRequestDto(String libelle, @NotNull Integer anneeId) {}