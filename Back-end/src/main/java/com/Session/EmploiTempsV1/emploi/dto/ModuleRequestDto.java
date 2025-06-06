package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

// MODIFIÉ
public record ModuleRequestDto(
        @NotNull Integer semestreId,
        String libelle,
        Integer heuresCours,
        Integer heuresTD,
        Integer heuresTP,
        String semDemTP,
        String semDemTD,
        // NOUVEAU : IDs pour les types de locaux requis (peuvent être null)
        Integer typeLocalRequisCoursId,
        Integer typeLocalRequisTDId,
        Integer typeLocalRequisTPId
) {}