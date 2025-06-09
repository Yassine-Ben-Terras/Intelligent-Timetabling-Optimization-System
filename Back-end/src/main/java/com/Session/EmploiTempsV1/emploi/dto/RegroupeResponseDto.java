package com.Session.EmploiTempsV1.emploi.dto;

import com.Session.EmploiTempsV1.emploi.entities.TypeRegroupement;
import java.util.Set;

public record RegroupeResponseDto(
        Long id,
        String nom,
        TypeRegroupement type,
        Set<SectionDto> sections,
        Set<GroupeResponseDto> groupes
) {}
