package com.Session.EmploiTempsV1.emploi.dto;

public record CreneauxResponseDto(
        Integer id,
        String heureDebut,
        String heureFin,
        Integer nbrHeures,
        AnneeUniversitaireDto annee,
        JourDto jour,
        SessionResponseDto session // <-- CORRECTION
) {}