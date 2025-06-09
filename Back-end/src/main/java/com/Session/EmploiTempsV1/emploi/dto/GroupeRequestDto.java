package com.Session.EmploiTempsV1.emploi.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

// On attend maintenant une liste d'IDs de modules
public record GroupeRequestDto(String libelle, Integer nbrEtudiants, @NotEmpty List<Integer> moduleIds) {}