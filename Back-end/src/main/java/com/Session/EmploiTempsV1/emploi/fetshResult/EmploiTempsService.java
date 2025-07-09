package com.Session.EmploiTempsV1.emploi.fetshResult;

import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.LigneEmploiTempsRepository;
import com.Session.EmploiTempsV1.emploi.repository.SecParamRepository;
import com.Session.EmploiTempsV1.emploi.repository.GrpParamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmploiTempsService {

    private final LigneEmploiTempsRepository ligneEmploiTempsRepository;
    private final SecParamRepository secParamRepository;
    private final GrpParamRepository grpParamRepository; // NOUVEAU : Injection du repository

    @Autowired
    public EmploiTempsService(LigneEmploiTempsRepository ligneEmploiTempsRepository,
                              SecParamRepository secParamRepository,
                              GrpParamRepository grpParamRepository) { // NOUVEAU : Ajout au constructeur
        this.ligneEmploiTempsRepository = ligneEmploiTempsRepository;
        this.secParamRepository = secParamRepository;
        this.grpParamRepository = grpParamRepository;
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDTO> getAllSimplifiedEmploi() {
        List<LigneEmploiTemps> lignes = ligneEmploiTempsRepository.findAll();
        return lignes.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private TimetableEntryDTO mapToDTO(LigneEmploiTemps ligne) {
        TimetableEntryDTO dto = new TimetableEntryDTO();
        dto.setId(ligne.getId());


        if (ligne.getTypeSeance() != null) dto.setTypeSeance(ligne.getTypeSeance().getLibelle());
        if (ligne.getLocal() != null) dto.setLocalLibelle(ligne.getLocal().getLibelle());

        ModuleEntity module = ligne.getModuleEntity();
        if (module != null) {
            dto.setModuleLibelle(module.getLibelle());
            if (module.getSemestre() != null) dto.setSemestreLibelle(module.getSemestre().getLibelle());
        }

        Creneaux creneau = ligne.getCreneaux();
        if (creneau != null) {
            dto.setHeureDebut(creneau.getHeureDebut());
            dto.setHeureFin(creneau.getHeureFin());
            if (creneau.getJour() != null) dto.setJourLibelle(creneau.getJour().getLibelle());
        }

        Professeur prof = ligne.getProf();
        if (prof != null) {
            dto.setProfNom(prof.getNom());
            dto.setProfPrenom(prof.getPrenom());
        }

        Groupe groupe = ligne.getGroupe();
        if(groupe != null) dto.setGroupeLibelle(groupe.getLibelle());

        Section section = null;

        // 1. Essayer d'obtenir la section directement (cas des cours pour une section entière)
        if (ligne.getSection() != null) {
            section = ligne.getSection();
        }
        // 2. Sinon, si c'est une séance de groupe (TD/TP), trouver la section via GrpParam
        else if (groupe != null && creneau != null && module != null && creneau.getAnnee() != null && module.getSemestre() != null) {
            Optional<GrpParam> grpParamOpt = grpParamRepository.findByGroupeAndAnneeAndSemestre(
                    groupe, creneau.getAnnee(), module.getSemestre()
            );
            // Si on trouve le paramétrage, on en extrait la section
            if (grpParamOpt.isPresent()) {
                section = grpParamOpt.get().getSection();
            }
        }

        // Si une section a été trouvée (d'une manière ou d'une autre), on la met dans le DTO
        if(section != null){
            dto.setSectionLibelle(section.getLibelle());
        }

        // 3. Maintenant qu'on a la section, on peut trouver la filière comme avant
        AnneeUniversitaire anneeFromCreneau = (creneau != null) ? creneau.getAnnee() : null;
        Semestre semestreFromModule = (module != null) ? module.getSemestre() : null;

        if (section != null && anneeFromCreneau != null && semestreFromModule != null) {
            List<SecParam> secParams = secParamRepository.findBySectionAndAnneeAndSemestre(
                    section, anneeFromCreneau, semestreFromModule
            );
            if (!secParams.isEmpty() && secParams.getFirst().getFiliere() != null) {
                dto.setFiliereLibelle(secParams.getFirst().getFiliere().getLibelle());
            } else {
                dto.setFiliereLibelle("Filière contextuelle introuvable");
            }
        } else {
            dto.setFiliereLibelle("Données manquantes pour contexte");
        }

        return dto;
    }
}