package com.Session.EmploiTempsV1.emploi.fetshResult;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableEntryDTO {
    private Integer id;
    private String jourLibelle;
    private String heureDebut;
    private String heureFin;
    private String profNom;
    private String profPrenom;
    private String moduleLibelle;
    private String localLibelle;
    private String filiereLibelle;
    private String semestreLibelle;
    private String typeSeance;
    private String sectionLibelle;
    private String groupeLibelle;
}