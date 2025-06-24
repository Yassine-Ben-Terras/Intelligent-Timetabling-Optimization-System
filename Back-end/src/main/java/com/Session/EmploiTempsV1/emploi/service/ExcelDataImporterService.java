package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExcelDataImporterService {

    @Autowired private AnneeUniversitaireRepository anneeUniversitaireRepository;
    @Autowired private com.Session.EmploiTempsV1.emploi.repository.SessionRepository sessionRepository;
    @Autowired private DepartementRepository departementRepository;
    @Autowired private FiliereRepository filiereRepository;
    @Autowired private SemestreRepository semestreRepository;
    @Autowired private ModuleEntityRepository moduleRepository;
    @Autowired private ProfesseurRepository professeurRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private GroupeRepository groupeRepository;
    @Autowired private SecParamRepository secParamRepository;
    @Autowired private GrpParamRepository grpParamRepository;
    @Autowired private AffectationEnseignantRepository affectationEnseignantRepository;
    @Autowired private TypeSeanceRepository typeSeanceRepository;
    @Autowired private TypeLocalRepository typeLocalRepository;
    @Autowired private FilModRepository filModRepository;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public void importDataFromExcel(InputStream excelInputStream, String fileName) throws Exception {
        System.out.println("Début du traitement du fichier : " + fileName);
        try (Workbook workbook = new XSSFWorkbook(excelInputStream)) {
            for (Sheet sheet : workbook) {
                System.out.println("----------------------------------------------------");
                System.out.println("Traitement de la feuille : " + sheet.getSheetName());
                processSheet(sheet);
                System.out.println("Fin du traitement de la feuille : " + sheet.getSheetName());
                System.out.println("----------------------------------------------------");
            }
        }
        System.out.println("Traitement du fichier " + fileName + " terminé.");
    }

    private void processSheet(Sheet sheet) {
        String anneeLibelle = getCellValueAsString(sheet, 1, 2);
        String sessionLibelle = getCellValueAsString(sheet, 1, 1);
        String filiereLibelle = getCellValueAsString(sheet, 3, 1);
        String departementLibelle = getCellValueAsString(sheet, 3, 2);

        if (filiereLibelle.isEmpty()) {
            System.err.println("AVERTISSEMENT: Filière non spécifiée dans la feuille '" + sheet.getSheetName() + "'. Feuille ignorée.");
            return;
        }
        if (anneeLibelle.isEmpty() || sessionLibelle.isEmpty() || departementLibelle.isEmpty()) {
            throw new RuntimeException("Données d'en-tête manquantes (Année, Session, Département) dans la feuille " + sheet.getSheetName());
        }

        AnneeUniversitaire annee = findOrCreateAnnee(anneeLibelle);
        Session session = findOrCreateSession(sessionLibelle, annee);
        Departement departement = findOrCreateDepartement(departementLibelle);
        Filiere filiere = findOrCreateFiliere(filiereLibelle, departement);

        Semestre currentSemestre = null;
        ModuleEntity currentModule = null;
        Map<String, Section> sectionCache = new HashMap<>();

        for (int rowIndex = 7; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null || isRowEmpty(row)) continue;

            String excelRowForLog = "Feuille '" + sheet.getSheetName() + "' L" + (rowIndex + 1);

            String semestreLibelle = getCellValueAsString(row, 1);
            if (!semestreLibelle.isEmpty()) {
                currentSemestre = findOrCreateSemestre(semestreLibelle, session);
                sectionCache.clear();
            }
            if (currentSemestre == null) continue;

            String moduleLibelle = getCellValueAsString(row, 2);
            if (!moduleLibelle.isEmpty()) {
                currentModule = findOrCreateModule(moduleLibelle, currentSemestre);
                findOrCreateFilMod(filiere, currentModule);
            }
            if (currentModule == null) continue;

            String typeSeanceLibelle = getCellValueAsString(row, 3);
            String sectionLibelle = getCellValueAsString(row, 4);
            String groupeLibelle = getCellValueAsString(row, 5);
            String effectifStr = getCellValueAsString(row, 6);
            String profsFullNames = getCellValueAsString(row, 7);
            String typeSalleRequisLibelle = getCellValueAsString(row, 8);
            String volumeHoraireStr = getCellValueAsString(row, 9);

            if (typeSeanceLibelle.isEmpty()) continue;
            TypeSeance typeSeance = findOrCreateTypeSeance(typeSeanceLibelle);
            updateModuleDetails(currentModule, typeSeance, volumeHoraireStr, typeSalleRequisLibelle);

            if (sectionLibelle.isEmpty()) continue;
            Integer effectif = effectifStr.isEmpty() ? null : Integer.parseInt(effectifStr);

            Section section = findOrCreateSection(sectionLibelle, effectif, filiere, currentSemestre, sectionCache);
            findOrCreateSecParam(annee, filiere, section, currentSemestre);

            Groupe groupe = null;
            if (!groupeLibelle.isEmpty()) {
                groupe = findOrCreateGroupe(groupeLibelle, sectionLibelle, effectif, filiere, currentSemestre, currentModule);
                findOrCreateGrpParam(annee, section, groupe, currentSemestre);
            }

            if (profsFullNames.isEmpty()) continue;
            String[] namesArray = profsFullNames.split(",");
            for (String singleProfName : namesArray) {
                if (singleProfName.trim().isEmpty()) continue;
                Professeur professeur = findOrCreateProfesseur(singleProfName.trim(), departement);
                createAffectationEnseignant(professeur, currentModule, typeSeance, section, groupe);
            }
        }
    }

    private AnneeUniversitaire findOrCreateAnnee(String libelle) {
        return anneeUniversitaireRepository.findByLibelle(libelle)
                .orElseGet(() -> {
                    System.out.println("INFO: Création de l'Année Universitaire: " + libelle);
                    AnneeUniversitaire annee = new AnneeUniversitaire();
                    annee.setLibelle(libelle);
                    return anneeUniversitaireRepository.save(annee);
                });
    }

    private Session findOrCreateSession(String libelle, AnneeUniversitaire annee) {
        return sessionRepository.findByLibelleAndAnnee(libelle, annee)
                .orElseGet(() -> {
                    System.out.println("INFO: Création de la Session: " + libelle + " pour l'année " + annee.getLibelle());
                    Session session = new Session();
                    session.setLibelle(libelle);
                    session.setAnnee(annee);
                    return sessionRepository.save(session);
                });
    }

    private Departement findOrCreateDepartement(String libelle) {
        return departementRepository.findByLibelle(libelle)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Département: " + libelle);
                    Departement dept = new Departement();
                    dept.setLibelle(libelle);
                    return departementRepository.save(dept);
                });
    }

    private Filiere findOrCreateFiliere(String libelle, Departement departement) {
        return filiereRepository.findByLibelleAndDepartement(libelle, departement)
                .orElseGet(() -> {
                    System.out.println("INFO: Création de la Filière: " + libelle);
                    Filiere filiere = new Filiere();
                    filiere.setLibelle(libelle);
                    filiere.setDepartement(departement);
                    return filiereRepository.save(filiere);
                });
    }

    private Semestre findOrCreateSemestre(String libelle, Session session) {
        return semestreRepository.findByLibelleAndSession(libelle, session)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Semestre: " + libelle);
                    Semestre semestre = new Semestre();
                    semestre.setLibelle(libelle);
                    semestre.setSession(session);
                    return semestreRepository.save(semestre);
                });
    }

    private ModuleEntity findOrCreateModule(String libelle, Semestre semestre) {
        return moduleRepository.findByLibelleAndSemestre(libelle, semestre)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Module: " + libelle);
                    ModuleEntity module = new ModuleEntity();
                    module.setLibelle(libelle);
                    module.setSemestre(semestre);
                    return moduleRepository.save(module);
                });
    }

    private void findOrCreateFilMod(Filiere filiere, ModuleEntity module) {
        FilModId filModId = new FilModId();
        filModId.setFiliere(filiere.getId());
        filModId.setModule(module.getId());

        if (!filModRepository.existsById(filModId)) {
            System.out.println("INFO: Liaison de la filière '" + filiere.getLibelle() + "' avec le module '" + module.getLibelle() + "'.");
            FilMod filMod = new FilMod(filiere, module);
            filModRepository.save(filMod);
        }
    }

    private TypeSeance findOrCreateTypeSeance(String libelle) {
        return typeSeanceRepository.findByLibelle(libelle.toUpperCase())
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Type de Séance: " + libelle.toUpperCase());
                    TypeSeance ts = new TypeSeance();
                    ts.setLibelle(libelle.toUpperCase());
                    return typeSeanceRepository.save(ts);
                });
    }

    private TypeLocal findOrCreateTypeLocal(String libelle) {
        if (libelle.isEmpty()) return null;
        return typeLocalRepository.findByLibelle(libelle)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Type de Local: " + libelle);
                    TypeLocal tl = new TypeLocal();
                    tl.setLibelle(libelle);
                    return typeLocalRepository.save(tl);
                });
    }

    private void updateModuleDetails(ModuleEntity module, TypeSeance typeSeance, String volumeHoraireStr, String typeSalleRequisLibelle) {
        if (volumeHoraireStr.isEmpty()) return;

        Integer volumeHoraire = Integer.parseInt(volumeHoraireStr);
        TypeLocal typeLocalRequis = findOrCreateTypeLocal(typeSalleRequisLibelle);
        boolean updated = false;

        switch (typeSeance.getLibelle().toUpperCase()) {
            case "COURS":
                if (!volumeHoraire.equals(module.getHeuresCours())) {
                    module.setHeuresCours(volumeHoraire);
                    updated = true;
                }
                if (typeLocalRequis != null && !typeLocalRequis.equals(module.getTypeLocalRequisCours())) {
                    module.setTypeLocalRequisCours(typeLocalRequis);
                    updated = true;
                }
                break;
            case "TD":
                if (!volumeHoraire.equals(module.getHeuresTD())) {
                    module.setHeuresTD(volumeHoraire);
                    updated = true;
                }
                if (typeLocalRequis != null && !typeLocalRequis.equals(module.getTypeLocalRequisTD())) {
                    module.setTypeLocalRequisTD(typeLocalRequis);
                    updated = true;
                }
                break;
            case "TP":
                if (!volumeHoraire.equals(module.getHeuresTP())) {
                    module.setHeuresTP(volumeHoraire);
                    updated = true;
                }
                if (typeLocalRequis != null && !typeLocalRequis.equals(module.getTypeLocalRequisTP())) {
                    module.setTypeLocalRequisTP(typeLocalRequis);
                    updated = true;
                }
                break;
        }

        if (updated) {
            System.out.println("INFO: Mise à jour du Module '" + module.getLibelle() + "' pour le type " + typeSeance.getLibelle());
            moduleRepository.save(module);
        }
    }

    private Section findOrCreateSection(String libelle, Integer effectif, Filiere filiere, Semestre semestre, Map<String, Section> cache) {
        // Nouvelle convention: SectionA_SMI_S6
        String uniqueLibelle = libelle + "_" + filiere.getLibelle() + "_" + semestre.getLibelle();

        if (cache.containsKey(uniqueLibelle)) {
            Section cachedSection = cache.get(uniqueLibelle);
            if (effectif != null && !effectif.equals(cachedSection.getNbrEtudiants())) {
                cachedSection.setNbrEtudiants(effectif);
                sectionRepository.save(cachedSection);
            }
            return cachedSection;
        }

        Section section = sectionRepository.findByLibelle(uniqueLibelle)
                .map(existingSection -> {
                    if (effectif != null && !effectif.equals(existingSection.getNbrEtudiants())) {
                        System.out.println("INFO: Mise à jour de l'effectif de la Section: " + uniqueLibelle);
                        existingSection.setNbrEtudiants(effectif);
                        return sectionRepository.save(existingSection);
                    }
                    return existingSection;
                })
                .orElseGet(() -> {
                    System.out.println("INFO: Création de la Section: " + uniqueLibelle);
                    Section newSection = new Section();
                    newSection.setLibelle(uniqueLibelle);
                    newSection.setNbrEtudiants(effectif);
                    return sectionRepository.save(newSection);
                });

        cache.put(uniqueLibelle, section);
        return section;
    }

    private Groupe findOrCreateGroupe(String groupeLibelle, String sectionLibelle, Integer effectif, Filiere filiere, Semestre semestre, ModuleEntity module) {
        // Nouvelle convention: Groupe1_SectionA_SMI_S6
        String uniqueLibelle = groupeLibelle + "_" + sectionLibelle + "_" + filiere.getLibelle() + "_" + semestre.getLibelle();

        Groupe groupe = groupeRepository.findByLibelle(uniqueLibelle)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Groupe: " + uniqueLibelle);
                    Groupe newGroupe = new Groupe();
                    newGroupe.setLibelle(uniqueLibelle);
                    newGroupe.setModules(new ArrayList<>());
                    return newGroupe;
                });

        if (effectif != null && !effectif.equals(groupe.getNbrEtudiants())) {
            System.out.println("INFO: Mise à jour de l'effectif du groupe '" + uniqueLibelle + "' à " + effectif);
            groupe.setNbrEtudiants(effectif);
        }

        if (groupe.getModules() == null) {
            groupe.setModules(new ArrayList<>());
        }

        if (!groupe.getModules().contains(module)) {
            System.out.println("INFO: Association du module '" + module.getLibelle() + "' au groupe '" + uniqueLibelle + "'.");
            groupe.getModules().add(module);
        }

        return groupeRepository.save(groupe);
    }

    private Professeur findOrCreateProfesseur(String fullName, Departement departement) {
        String[] nameParts = fullName.trim().split("\\s+", 2);
        String nom = (nameParts.length > 0) ? nameParts[0].trim().toUpperCase() : "";
        String prenom = (nameParts.length > 1) ? nameParts[1].trim() : "";

        if (nom.isEmpty()) {
            throw new RuntimeException("Le nom du professeur est invalide: " + fullName);
        }

        return professeurRepository.findByNomAndPrenom(nom, prenom)
                .orElseGet(() -> {
                    System.out.println("INFO: Création du Professeur: " + nom + " " + prenom);
                    Professeur p = new Professeur();
                    p.setNom(nom);
                    p.setPrenom(prenom);
                    p.setDepartement(departement);
                    return professeurRepository.save(p);
                });
    }

    private void findOrCreateSecParam(AnneeUniversitaire annee, Filiere filiere, Section section, Semestre semestre) {
        SecParamId id = new SecParamId(annee.getId(), filiere.getId(), section.getId(), semestre.getId());
        if (!secParamRepository.existsById(id)) {
            System.out.println("INFO: Création de SecParam pour la section " + section.getLibelle());
            SecParam secParam = new SecParam(annee, filiere, section, semestre);
            secParamRepository.save(secParam);
        }
    }

    private void findOrCreateGrpParam(AnneeUniversitaire annee, Section section, Groupe groupe, Semestre semestre) {
        GrpParamId id = new GrpParamId(section.getId(), groupe.getId(), annee.getId(), semestre.getId());
        if (!grpParamRepository.existsById(id)) {
            System.out.println("INFO: Création de GrpParam pour le groupe " + groupe.getLibelle());
            GrpParam grpParam = new GrpParam(section, groupe, annee, semestre);
            grpParamRepository.save(grpParam);
        }
    }

    private void createAffectationEnseignant(Professeur prof, ModuleEntity module, TypeSeance typeSeance, Section section, Groupe groupe) {
        Optional<AffectationEnseignant> existing;
        if (groupe != null) {
            existing = affectationEnseignantRepository.findByProfesseurAndModuleAndTypeSeanceAndGroupe(prof, module, typeSeance, groupe);
        } else {
            existing = affectationEnseignantRepository.findByProfesseurAndModuleAndTypeSeanceAndSection(prof, module, typeSeance, section);
        }

        if (existing.isEmpty()) {
            System.out.println("INFO: Création de l'affectation pour " + prof.getNom() + " au module " + module.getLibelle() + " (" + typeSeance.getLibelle() + ")");
            AffectationEnseignant affectation = AffectationEnseignant.builder()
                    .professeur(prof)
                    .module(module)
                    .typeSeance(typeSeance)
                    .section(groupe == null ? section : null)
                    .groupe(groupe)
                    .build();
            affectationEnseignantRepository.save(affectation);
        }
    }

    private String getCellValueAsString(Sheet sheet, int rowIndex, int colIndex) {
        Row row = sheet.getRow(rowIndex);
        return getCellValueAsString(row, colIndex);
    }

    private String getCellValueAsString(Row row, int colIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}