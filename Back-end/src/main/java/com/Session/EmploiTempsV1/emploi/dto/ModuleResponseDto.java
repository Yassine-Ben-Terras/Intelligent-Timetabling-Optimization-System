package com.Session.EmploiTempsV1.emploi.dto;

// MODIFIÉ
public record ModuleResponseDto(
        Integer id,
        String libelle,
        Integer heuresCours,
        Integer heuresTD,
        Integer heuresTP,
        String semDemTP,
        String semDemTD,
        SemestreDto semestre,
        // NOUVEAU : Informations complètes sur les types de locaux requis
        TypeLocalDto typeLocalRequisCours,
        TypeLocalDto typeLocalRequisTD,
        TypeLocalDto typeLocalRequisTP
) {}