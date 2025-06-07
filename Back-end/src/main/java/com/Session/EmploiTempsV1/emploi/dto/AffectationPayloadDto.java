// package com.Session.EmploiTempsV1.emploi.dto;
package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

// Ce record représente les données envoyées par le frontend pour créer UNE seule affectation.
public record AffectationPayloadDto(
        @NotNull Integer professeurId,
        @NotNull Integer moduleId,
        @NotNull Integer typeSeanceId,
        Integer sectionId, // Optionnel
        Integer groupeId   // Optionnel
) {}