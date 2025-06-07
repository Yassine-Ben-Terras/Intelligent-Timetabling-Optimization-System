package com.Session.EmploiTempsV1.emploi.dto;

// DTO pour l'affichage d'un professeur
// Nécessite DepartementDto
public record ProfesseurResponseDto(
        Integer id,
        String nom,
        String prenom,
        String statut,
        DepartementDto departement
) {}