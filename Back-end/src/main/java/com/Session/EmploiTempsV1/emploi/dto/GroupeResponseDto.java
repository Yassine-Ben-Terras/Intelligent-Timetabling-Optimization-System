package com.Session.EmploiTempsV1.emploi.dto;

import java.util.List;

// La réponse contient maintenant une liste de modules
public record GroupeResponseDto(Integer id, String libelle, Integer nbrEtudiants, List<ModuleResponseDto> modules) {}