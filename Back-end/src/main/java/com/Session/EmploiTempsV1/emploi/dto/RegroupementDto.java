package com.Session.EmploiTempsV1.emploi.dto;

import com.Session.EmploiTempsV1.emploi.entities.TypeRegroupement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegroupementDto {
    private Integer id;
    private String libelle;
    private TypeRegroupement typeRegroupement;
    private List<RegroupementDetailDto> details;
}