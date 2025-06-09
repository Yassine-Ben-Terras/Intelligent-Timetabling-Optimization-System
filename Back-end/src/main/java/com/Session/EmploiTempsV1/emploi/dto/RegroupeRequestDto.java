package com.Session.EmploiTempsV1.emploi.dto;

import com.Session.EmploiTempsV1.emploi.entities.TypeRegroupement;
import java.util.Set;

public record RegroupeRequestDto(
        String nom,
        TypeRegroupement type,
        Set<Integer> sectionIds,
        Set<Integer> groupeIds
) {}
