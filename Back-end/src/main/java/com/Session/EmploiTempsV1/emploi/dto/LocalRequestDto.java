package com.Session.EmploiTempsV1.emploi.dto;
import jakarta.validation.constraints.NotNull;
public record LocalRequestDto(String libelle, Integer capacite,  Integer typeId) {}