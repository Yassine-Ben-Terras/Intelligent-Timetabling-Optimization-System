package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AffectationSyncRequestDto(
        @NotNull Integer moduleId,
        @NotNull Integer typeSeanceId,
        Integer sectionId, // Doit être non-null si groupeId est null
        Integer groupeId,  // Doit être non-null si sectionId est null
        @NotNull List<Integer> professeurIds // La liste complète des IDs de professeurs à affecter
) {}