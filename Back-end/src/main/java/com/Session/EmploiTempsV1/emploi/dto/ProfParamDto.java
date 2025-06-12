// FICHIER MODIFIÉ : ProfParamDto.java
package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotNull;

// MODIFIÉ : Ajout de typeSeanceId. C'est le contrat de données avec le frontend.
public record ProfParamDto(
        @NotNull Integer professeurId,
        @NotNull Integer moduleId,
        @NotNull Integer typeSeanceId
) {}