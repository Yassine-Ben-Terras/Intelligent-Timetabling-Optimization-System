// FICHIER : com/Session/EmploiTempsV1/emploi/maxSat/TimetableServiceV6.java

package com.Session.EmploiTempsV1.emploi.orTools;

import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("timetableServiceV6")
public class TimetableServiceV6 {

    public static final int SLOT_DURATION_MINUTES = 30;

    // Repositories
    private final AnneeUniversitaireRepository anneeRepository;
    private final SessionRepository sessionRepository;
    private final AffectationEnseignantRepository affectationRepository;
    private final CreneauxRepository creneauxRepository;
    private final LocalRepository localRepository;
    private final LigneEmploiTempsRepository ligneEmploiTempsRepository;
    private final DispoProfRepository dispoProfRepository;
    private final JourRepository jourRepository;
    private final RegroupementRepository regroupementRepository;
    private final PlageTravailRepository plageTravailRepository;
    private final GrpParamRepository grpParamRepository;
    private final OrToolsMaxSatServiceV6 maxSatService;

    public TimetableServiceV6(AnneeUniversitaireRepository anneeRepo,
                              SessionRepository sessionRepo,
                              AffectationEnseignantRepository affectationRepo,
                              CreneauxRepository creneauxRepo,
                              LocalRepository localRepo,
                              LigneEmploiTempsRepository ligneRepo,
                              DispoProfRepository dispoRepo,
                              JourRepository jourRepo,
                              RegroupementRepository regroupementRepo,
                              PlageTravailRepository plageTravailRepo,
                              GrpParamRepository grpParamRepository,
                              @Qualifier("orToolsMaxSatServiceV6") OrToolsMaxSatServiceV6 maxSatService) {
        this.anneeRepository = anneeRepo;
        this.sessionRepository = sessionRepo;
        this.affectationRepository = affectationRepo;
        this.creneauxRepository = creneauxRepo;
        this.localRepository = localRepo;
        this.ligneEmploiTempsRepository = ligneRepo;
        this.dispoProfRepository = dispoRepo;
        this.jourRepository = jourRepo;
        this.regroupementRepository = regroupementRepo;
        this.plageTravailRepository = plageTravailRepo;
        this.grpParamRepository = grpParamRepository;
        this.maxSatService = maxSatService;
    }

    @Transactional
    public void generateAndStoreTimetable(Integer anneeId, Integer sessionId) {
        AnneeUniversitaire annee = anneeRepository.findById(anneeId)
                .orElseThrow(() -> new RuntimeException("Année Universitaire non trouvée pour l'ID : " + anneeId));
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée pour l'ID : " + sessionId));

        if (!session.getAnnee().getId().equals(annee.getId())) {
            throw new RuntimeException("Incohérence : La session ID " + sessionId + " n'appartient pas à l'année ID " + anneeId);
        }

        Map<String, DayOfWeek> jourToDayOfWeekMap = buildDayMapping();

        List<TimeSlot> allTimeSlots = generateTimeSlotsFromDB(annee, session, jourToDayOfWeekMap);
        if (allTimeSlots.isEmpty()) {
            throw new RuntimeException("Aucune plage de travail n'a été définie pour cette session. Veuillez en créer.");
        }

        // ======================= MODIFICATION MAJEURE ICI =======================
        // On collecte toutes les affectations et tous les regroupements pour les pré-traiter.
        List<AffectationEnseignant> allAffectations = affectationRepository.findByModule_Semestre_Session_Id(sessionId);
        List<Regroupement> allRegroupements = regroupementRepository.findAll();

        // Crée des événements uniques pour les affectations individuelles et des "méta-événements" pour les regroupements.
        List<EventToSchedule> eventsToSchedule = createEventsWithRegroupements(allAffectations, allRegroupements);
        // ========================================================================

        if (eventsToSchedule.isEmpty()) {
            System.out.println("Aucun événement à planifier. La génération est terminée.");
            return;
        }

        Map<Professeur, Set<TimeSlot>> professorAvailability = buildProfessorAvailability(allTimeSlots, jourToDayOfWeekMap);
        List<Local> availableLocals = getAvailableLocals();

        Map<Section, Set<Groupe>> sectionGroupRelations = grpParamRepository.findAll().stream()
                .filter(gp -> gp.getSection() != null && gp.getGroupe() != null)
                .collect(Collectors.groupingBy(
                        GrpParam::getSection,
                        Collectors.mapping(GrpParam::getGroupe, Collectors.toSet())
                ));

        System.out.println("Début de la résolution avec OR-Tools MaxSAT...");
        // NOTE : On ne passe plus `allRegroupements` au service MaxSAT car la logique est maintenant gérée en amont.
        List<ScheduledEvent> scheduledEvents = maxSatService.solveTimetableMaxSAT(
                eventsToSchedule,
                allTimeSlots,
                availableLocals,
                professorAvailability,
                sectionGroupRelations
        );

        if (scheduledEvents.isEmpty() && !eventsToSchedule.isEmpty()) {
            throw new RuntimeException("L'optimiseur OR-Tools n'a pu planifier aucun événement. Vérifiez les contraintes.");
        }

        storeScheduledEvents(scheduledEvents, annee, session, jourToDayOfWeekMap);
    }

    // =================================================================================
    // =========== NOUVELLE MÉTHODE POUR PRÉ-TRAITER LES REGROUPEMENTS ===========
    // =================================================================================
    private List<EventToSchedule> createEventsWithRegroupements(List<AffectationEnseignant> allAffectations, List<Regroupement> allRegroupements) {
        List<EventToSchedule> finalEvents = new ArrayList<>();
        Set<AffectationEnseignant> groupedAffectations = new HashSet<>();

        // Crée une map pour retrouver facilement une affectation à partir de ses détails.
        Map<String, AffectationEnseignant> affectationLookup = new HashMap<>();
        for (AffectationEnseignant aff : allAffectations) {
            String sectionId = (aff.getSection() != null) ? aff.getSection().getId().toString() : "null";
            String groupeId = (aff.getGroupe() != null) ? aff.getGroupe().getId().toString() : "null";
            String key = aff.getModule().getId() + "_" + sectionId + "_" + groupeId;
            affectationLookup.put(key, aff);
        }

        // 1. Traiter les regroupements en priorité pour créer des "méta-événements"
        for (Regroupement reg : allRegroupements) {
            List<AffectationEnseignant> affectationsForThisGroup = new ArrayList<>();
            for (RegroupementDetail detail : reg.getDetails()) {
                String sectionId = (detail.getSection() != null) ? detail.getSection().getId().toString() : "null";
                String groupeId = (detail.getGroupe() != null) ? detail.getGroupe().getId().toString() : "null";
                String key = detail.getModule().getId() + "_" + sectionId + "_" + groupeId;

                AffectationEnseignant foundAff = affectationLookup.get(key);
                if (foundAff != null) {
                    affectationsForThisGroup.add(foundAff);
                } else {
                    System.err.println("AVERTISSEMENT : Le détail de regroupement pour le module " + detail.getModule().getId() + " et section/groupe " + sectionId + "/" + groupeId + " ne correspond à aucune affectation existante.");
                }
            }

            if (affectationsForThisGroup.size() > 1) {
                // Créer un seul événement composite qui englobe toutes les affectations du groupe.
                String eventId = "Regroupement_" + reg.getId();
                EventToSchedule compositeEvent = new EventToSchedule(eventId, affectationsForThisGroup, reg);
                finalEvents.add(compositeEvent);
                groupedAffectations.addAll(affectationsForThisGroup);
                System.out.println("Création d'un méta-événement '" + eventId + "' avec " + affectationsForThisGroup.size() + " affectations.");
            }
        }

        // 2. Traiter toutes les autres affectations qui n'ont pas été regroupées.
        for (AffectationEnseignant aff : allAffectations) {
            if (!groupedAffectations.contains(aff)) {
                String eventId = "Affectation_" + aff.getId();
                // Créer un événement standard avec une seule affectation. Le paramètre regroupement est null.
                EventToSchedule individualEvent = new EventToSchedule(eventId, Collections.singletonList(aff), null);
                finalEvents.add(individualEvent);
            }
        }

        System.out.println("Total d'événements à planifier (méta et individuels) : " + finalEvents.size());
        return finalEvents;
    }

    @Cacheable("available-locals")
    public List<Local> getAvailableLocals() {
        return localRepository.findAll();
    }

    private List<TimeSlot> generateTimeSlotsFromDB(AnneeUniversitaire annee, Session session, Map<String, DayOfWeek> jourMapping) {
        List<PlageTravail> workingPeriods = plageTravailRepository.findByAnneeAndSession(annee, session);
        Set<TimeSlot> slots = new TreeSet<>(Comparator.comparing(TimeSlot::getDay).thenComparing(TimeSlot::getStartTime));
        for (PlageTravail period : workingPeriods) {
            DayOfWeek day = jourMapping.get(period.getJour().getLibelle().toUpperCase());
            if (day == null) continue;
            LocalTime currentTime = LocalTime.parse(period.getHeureDebut());
            LocalTime endTime = LocalTime.parse(period.getHeureFin());
            while (currentTime.isBefore(endTime)) {
                slots.add(new TimeSlot(day, currentTime, SLOT_DURATION_MINUTES));
                currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
            }
        }
        return new ArrayList<>(slots);
    }

    // La méthode storeScheduledEvents est déjà correcte et n'a pas besoin de modification.
    // Elle parcourt déjà la liste `getAffectations()` de chaque `EventToSchedule`,
    // ce qui fonctionnera parfaitement pour les méta-événements (liste > 1) et les événements normaux (liste = 1).
    private void storeScheduledEvents(List<ScheduledEvent> scheduledEvents, AnneeUniversitaire annee, Session session, Map<String, DayOfWeek> jourMapping) {
        Map<DayOfWeek, Jour> dayOfWeekToJourMap = jourRepository.findAll().stream()
                .collect(Collectors.toMap(jour -> jourMapping.get(jour.getLibelle().toUpperCase()), Function.identity(), (j1, j2) -> j1));

        List<Creneaux> oldGeneratedCreneaux = creneauxRepository.findByAnneeAndSession(annee, session);
        if (!oldGeneratedCreneaux.isEmpty()) {
            ligneEmploiTempsRepository.deleteByCreneauxIn(oldGeneratedCreneaux);
            creneauxRepository.deleteAll(oldGeneratedCreneaux);
        }

        Map<String, List<ScheduledEvent>> eventsGroupedBySlotAndLocal = scheduledEvents.stream()
                .collect(Collectors.groupingBy(se ->
                        se.getStartTimeSlot().toString() + "_" + se.getLocal().getId()
                ));

        List<LigneEmploiTemps> newLignes = new ArrayList<>();

        for (List<ScheduledEvent> groupedEvents : eventsGroupedBySlotAndLocal.values()) {
            ScheduledEvent firstEvent = groupedEvents.get(0);

            LocalTime startTime = firstEvent.getStartTimeSlot().getStartTime();
            int maxDuration = groupedEvents.stream()
                    .mapToInt(se -> se.getEventDetails().getDurationInMinutes())
                    .max().orElse(0);
            LocalTime endTime = startTime.plusMinutes(maxDuration);
            DayOfWeek dayEnum = firstEvent.getStartTimeSlot().getDay();
            Jour jourToSave = dayOfWeekToJourMap.get(dayEnum);

            Creneaux newCreneau = new Creneaux(null, annee, jourToSave, session, startTime.toString(), endTime.toString(), (int) Duration.between(startTime, endTime).toMinutes() / 60);
            Creneaux savedCreneau = creneauxRepository.save(newCreneau);

            for (ScheduledEvent se : groupedEvents) {
                for (AffectationEnseignant affectation : se.getEventDetails().getAffectations()) {
                    LigneEmploiTemps ligne = new LigneEmploiTemps();

                    ligne.setLocal(se.getLocal());
                    ligne.setCreneaux(savedCreneau);
                    ligne.setModuleEntity(affectation.getModule());
                    ligne.setGroupe(affectation.getGroupe());
                    ligne.setSection(affectation.getSection());
                    ligne.setProf(affectation.getProfesseur());
                    ligne.setTypeSeance(affectation.getTypeSeance());

                    newLignes.add(ligne);
                }
            }
        }

        ligneEmploiTempsRepository.saveAll(newLignes);
        System.out.println("Sauvegarde de " + newLignes.size() + " nouvelles lignes d'emploi du temps pour " + eventsGroupedBySlotAndLocal.size() + " créneaux.");
    }


    private Map<Professeur, Set<TimeSlot>> buildProfessorAvailability(List<TimeSlot> allTimeSlots, Map<String, DayOfWeek> jourMapping) {
        Map<Professeur, Set<TimeSlot>> availabilityMap = new HashMap<>();
        List<DispoProf> allDispos = dispoProfRepository.findAll();
        Set<Professeur> allProfs = affectationRepository.findAll().stream().map(AffectationEnseignant::getProfesseur).collect(Collectors.toSet());
        Map<Professeur, List<DispoProf>> disposByProf = allDispos.stream().collect(Collectors.groupingBy(DispoProf::getProf));

        for (Professeur prof : allProfs) {
            List<DispoProf> profDispos = disposByProf.get(prof);
            if (profDispos == null || profDispos.isEmpty()) {
                availabilityMap.put(prof, new HashSet<>(allTimeSlots));
            } else {
                Set<TimeSlot> availableSlots = new HashSet<>();
                for (DispoProf dispo : profDispos) {
                    DayOfWeek day = jourMapping.get(dispo.getJour().getLibelle().toUpperCase());
                    if (day == null) continue;

                    String periode = dispo.getPeriode().toUpperCase();
                    LocalTime boundary = LocalTime.of(13, 0);

                    allTimeSlots.stream()
                            .filter(ts -> ts.getDay().equals(day))
                            .filter(ts -> {
                                if (periode.equals("MATIN")) {
                                    return ts.getStartTime().isBefore(boundary);
                                } else if (periode.equals("SOIR") || periode.equals("APRES_MIDI")) {
                                    return !ts.getStartTime().isBefore(boundary);
                                }
                                return true;
                            })
                            .forEach(availableSlots::add);
                }
                availabilityMap.put(prof, availableSlots);
            }
        }
        return availabilityMap;
    }

    @Cacheable("day-mapping")
    public Map<String, DayOfWeek> buildDayMapping() {
        return jourRepository.findAll().stream()
                .collect(Collectors.toMap(
                        jour -> jour.getLibelle().toUpperCase(),
                        this::mapFrenchDayToEnum
                ));
    }

    private DayOfWeek mapFrenchDayToEnum(Jour jour) {
        return switch (jour.getLibelle().toUpperCase()) {
            case "LUNDI" -> DayOfWeek.MONDAY;
            case "MARDI" -> DayOfWeek.TUESDAY;
            case "MERCREDI" -> DayOfWeek.WEDNESDAY;
            case "JEUDI" -> DayOfWeek.THURSDAY;
            case "VENDREDI" -> DayOfWeek.FRIDAY;
            case "SAMEDI" -> DayOfWeek.SATURDAY;
            case "DIMANCHE" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Jour français non reconnu : " + jour.getLibelle());
        };
    }
}