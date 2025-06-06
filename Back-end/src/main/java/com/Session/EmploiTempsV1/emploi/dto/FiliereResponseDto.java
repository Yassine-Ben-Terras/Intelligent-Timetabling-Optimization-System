package com.Session.EmploiTempsV1.emploi.dto;

// DTO pour l'affichage des données
public record FiliereResponseDto(
        Integer id,
        String codeFiliere,
        String libelle,
        DepartementDto departement // On réutilise le DTO de Département
) {}