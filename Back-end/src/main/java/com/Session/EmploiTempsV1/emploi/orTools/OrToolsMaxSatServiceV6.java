package com.Session.EmploiTempsV1.emploi.orTools;

import com.Session.EmploiTempsV1.emploi.entities.*;
import com.google.ortools.sat.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("orToolsMaxSatServiceV6")
public class OrToolsMaxSatServiceV6 {

    // La signature de la méthode est modifiée : `allRegroupements` est supprimé.
    public List<ScheduledEvent> solveTimetableMaxSAT(List<EventToSchedule> events,
                                                     List<TimeSlot> timeSlots,
                                                     List<Local> locals,
                                                     Map<Professeur, Set<TimeSlot>> professorAvailability,
                                                     Map<Section, Set<Groupe>> sectionGroupRelations) {

        // Les validations spécifiques aux regroupements ne sont plus nécessaires ici.
        // La validité est implicitement gérée par la création correcte des EventToSchedule.

        List<ScheduledEvent> scheduledEventsResult = new ArrayList<>();
        CpModel model = new CpModel();

        Map<String, EventToSchedule> eventMap = events.stream()
                .collect(Collectors.toMap(EventToSchedule::getId, e -> e));
        Map<TimeSlot, Integer> timeSlotIndex = indexList(timeSlots);
        Map<Local, Integer> localIndex = indexList(locals);

        Map<String, BoolVar[][]> x = new HashMap<>();

        // --- Création des variables ---
        for (EventToSchedule e : events) {
            BoolVar[][] eventVars = new BoolVar[timeSlots.size()][locals.size()];
            int durationInSlots = (int) Math.ceil((double) e.getDurationInMinutes() / TimetableServiceV6.SLOT_DURATION_MINUTES);

            for (int t = 0; t < timeSlots.size(); t++) {
                for (int l = 0; l < locals.size(); l++) {
                    // isAssignmentValid fonctionne déjà pour les méta-événements car EventToSchedule
                    // a déjà agrégé les professeurs, la capacité, etc.
                    if (isAssignmentValid(e, timeSlots.get(t), locals.get(l), durationInSlots,
                            timeSlots, timeSlotIndex, professorAvailability)) {
                        String varName = String.format("x_%s_%d_%d", e.getId(), t, l);
                        eventVars[t][l] = model.newBoolVar(varName);
                    } else {
                        eventVars[t][l] = null;
                    }
                }
            }
            x.put(e.getId(), eventVars);
        }

        // --- Contrainte 1: Chaque événement (y compris les méta-événements) doit être planifié exactement une fois ---
        for (EventToSchedule e : events) {
            List<Literal> literals = new ArrayList<>();
            BoolVar[][] eventVars = x.get(e.getId());

            for (int t = 0; t < timeSlots.size(); t++) {
                for (int l = 0; l < locals.size(); l++) {
                    if (eventVars[t][l] != null) {
                        literals.add(eventVars[t][l]);
                    }
                }
            }

            if (!literals.isEmpty()) {
                model.addExactlyOne(literals);
            } else {
                System.err.println("AVERTISSEMENT : Aucune assignation valide (local/horaire) trouvée pour l'événement : " + e.getId()
                        + ". Capacité requise: " + e.getRequiredCapacity() + ", Profs: " + e.getProfesseurs().size());
            }
        }

        // --- Contrainte 2: Pas de chevauchement de ressources ---
        // Ces contraintes fonctionnent maintenant correctement car un regroupement est traité comme un seul événement.
        addResourceNoOverlapConstraint(model, x, eventMap, timeSlots, locals, "Prof", EventToSchedule::getProfesseurs);
        addResourceNoOverlapConstraint(model, x, eventMap, timeSlots, locals, "Groupe", EventToSchedule::getGroupes);
        addResourceNoOverlapConstraint(model, x, eventMap, timeSlots, locals, "Section", EventToSchedule::getSections);
        addLocalNoOverlapConstraint(model, x, events, timeSlots, locals);

        // --- Contrainte 3: Non-chevauchement Section/Groupe ---
        addSectionGroupNoOverlapConstraint(model, x, events, timeSlots, locals, sectionGroupRelations);

        // --- Contrainte 4: Regroupements ---
        // CETTE CONTRAINTE EST COMPLÈTEMENT SUPPRIMÉE. ELLE EST MAINTENANT GÉRÉE DANS LA PRÉPARATION DES DONNÉES.

        // --- Résolution ---
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(300);
        solver.getParameters().setNumSearchWorkers(Runtime.getRuntime().availableProcessors());

        CpSolverStatus status = solver.solve(model);

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Solution trouvée avec OR-Tools MaxSAT!");

            for (EventToSchedule e : events) {
                BoolVar[][] eventVars = x.get(e.getId());
                boolean eventScheduled = false;

                for (int t = 0; t < timeSlots.size(); t++) {
                    for (int l = 0; l < locals.size(); l++) {
                        if (eventVars[t][l] != null && solver.booleanValue(eventVars[t][l])) {
                            scheduledEventsResult.add(new ScheduledEvent(e, timeSlots.get(t), locals.get(l)));
                            eventScheduled = true;
                            break;
                        }
                    }
                    if (eventScheduled) break;
                }
            }
        } else {
            System.err.println("Aucune solution trouvée. Statut: " + status);
            System.err.println("Le modèle est peut-être infaisable. Causes possibles :");
            System.err.println("- Contraintes trop strictes (disponibilités profs, locaux insuffisants).");
            System.err.println("- Pas assez de plages de travail définies pour le volume de cours.");
            System.err.println("- Un méta-événement (regroupement) a des exigences impossibles (ex: capacité totale > plus grande salle).");
            System.err.println("- La contrainte Section/Groupe empêche toute solution.");

            throw new RuntimeException("Aucune solution trouvée . Statut: " + status);
        }

        return scheduledEventsResult;
    }

    private boolean isAssignmentValid(EventToSchedule e, TimeSlot startSlot, Local local,
                                      int durationInSlots, List<TimeSlot> allSlots,
                                      Map<TimeSlot, Integer> slotIndexMap,
                                      Map<Professeur, Set<TimeSlot>> profAvailability) {
        // Validation du local
        if (local.getCapacite() < e.getRequiredCapacity()) return false;
        if (e.getRequiredRoomType() != null && !e.getRequiredRoomType().equals(local.getType())) return false;

        // Validation de la disponibilité horaire et du professeur
        Integer startIdx = slotIndexMap.get(startSlot);
        if (startIdx == null || startIdx + durationInSlots > allSlots.size()) return false;

        for (int i = 0; i < durationInSlots; i++) {
            int currentIdx = startIdx + i;
            if (currentIdx >= allSlots.size()) return false;
            TimeSlot currentSlot = allSlots.get(currentIdx);
            if (currentSlot.getDay() != startSlot.getDay()) return false;

            // Vérifie la dispo pour TOUS les profs de l'événement (qu'il soit simple ou regroupé)
            for (Professeur prof : e.getProfesseurs()) {
                Set<TimeSlot> availableSlots = profAvailability.get(prof);
                if (availableSlots == null || !availableSlots.contains(currentSlot)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addLocalNoOverlapConstraint(CpModel model, Map<String, BoolVar[][]> x, List<EventToSchedule> events, List<TimeSlot> timeSlots, List<Local> locals) {
        for (int l = 0; l < locals.size(); l++) {
            for (int t = 0; t < timeSlots.size(); t++) {
                List<Literal> activeAssignments = new ArrayList<>();
                for (EventToSchedule e : events) {
                    int durationInSlots = (int) Math.ceil((double) e.getDurationInMinutes() / TimetableServiceV6.SLOT_DURATION_MINUTES);
                    BoolVar[][] eventVars = x.get(e.getId());

                    for (int i = 0; i < durationInSlots; i++) {
                        int startSlotIdx = t - i;
                        if (startSlotIdx >= 0 && startSlotIdx < timeSlots.size() &&
                                timeSlots.get(startSlotIdx).getDay() == timeSlots.get(t).getDay()) {

                            if (eventVars != null && startSlotIdx < eventVars.length &&
                                    l < eventVars[startSlotIdx].length && eventVars[startSlotIdx][l] != null) {
                                activeAssignments.add(eventVars[startSlotIdx][l]);
                            }
                        }
                    }
                }
                if (activeAssignments.size() > 1) {
                    model.addAtMostOne(activeAssignments);
                }
            }
        }
    }

    // =================================================================================
    // LES MÉTHODES addRegroupementConstraint, validateRegroupements, createEventKeyMap,
    // findEventsForRegroupement, et isGroupAssignmentValid SONT SUPPRIMÉES.
    // =================================================================================

    private void addSectionGroupNoOverlapConstraint(CpModel model, Map<String, BoolVar[][]> x, List<EventToSchedule> allEvents, List<TimeSlot> timeSlots, List<Local> locals, Map<Section, Set<Groupe>> sectionGroupRelations) {
        Map<Section, List<EventToSchedule>> sectionCourseEvents = new HashMap<>();
        Map<Groupe, List<EventToSchedule>> groupTd_TpEvents = new HashMap<>();
        for (EventToSchedule e : allEvents) {
            String typeSeance = e.getTypeSeance().getLibelle().toUpperCase();
            if ("COURS".equals(typeSeance) && !e.getSections().isEmpty()) { e.getSections().forEach(s -> sectionCourseEvents.computeIfAbsent(s, k -> new ArrayList<>()).add(e)); }
            else if (("TD".equals(typeSeance) || "TP".equals(typeSeance)) && !e.getGroupes().isEmpty()) { e.getGroupes().forEach(g -> groupTd_TpEvents.computeIfAbsent(g, k -> new ArrayList<>()).add(e)); }
        }
        for (Map.Entry<Section, Set<Groupe>> relation : sectionGroupRelations.entrySet()) {
            List<EventToSchedule> courseEvents = sectionCourseEvents.get(relation.getKey());
            if (courseEvents == null || courseEvents.isEmpty()) continue;
            List<EventToSchedule> groupEvents = relation.getValue().stream().map(groupTd_TpEvents::get).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
            if (groupEvents.isEmpty()) continue;
            for (int t = 0; t < timeSlots.size(); t++) {
                List<Literal> overlappingEvents = new ArrayList<>();
                for (EventToSchedule courseEvent : courseEvents) { addOverlappingLiterals(overlappingEvents, courseEvent, t, x, timeSlots, locals); }
                for (EventToSchedule groupEvent : groupEvents) { addOverlappingLiterals(overlappingEvents, groupEvent, t, x, timeSlots, locals); }
                if (overlappingEvents.size() > 1) { model.addAtMostOne(overlappingEvents); }
            }
        }
    }

    private void addOverlappingLiterals(List<Literal> literals, EventToSchedule event, int timeSlotIndex, Map<String, BoolVar[][]> x, List<TimeSlot> timeSlots, List<Local> locals) {
        int durationInSlots = (int) Math.ceil((double) event.getDurationInMinutes() / TimetableServiceV6.SLOT_DURATION_MINUTES);
        BoolVar[][] eventVars = x.get(event.getId());
        for (int i = 0; i < durationInSlots; i++) {
            int startSlotIdx = timeSlotIndex - i;
            if (startSlotIdx >= 0 && startSlotIdx < timeSlots.size() && timeSlots.get(startSlotIdx).getDay() == timeSlots.get(timeSlotIndex).getDay()) {
                if (eventVars != null && startSlotIdx < eventVars.length) {
                    for (int l = 0; l < locals.size(); l++) {
                        if (eventVars[startSlotIdx][l] != null) {
                            literals.add(eventVars[startSlotIdx][l]);
                        }
                    }
                }
            }
        }
    }

    private <R> void addResourceNoOverlapConstraint(CpModel model, Map<String, BoolVar[][]> x, Map<String, EventToSchedule> eventMap, List<TimeSlot> timeSlots, List<Local> locals, String resourceName, java.util.function.Function<EventToSchedule, Set<R>> resourceExtractor) {
        Map<R, List<EventToSchedule>> eventsByResource = new HashMap<>();
        for (EventToSchedule e : eventMap.values()) { for (R resource : resourceExtractor.apply(e)) { eventsByResource.computeIfAbsent(resource, k -> new ArrayList<>()).add(e); } }
        for (Map.Entry<R, List<EventToSchedule>> entry : eventsByResource.entrySet()) {
            if (entry.getValue().size() <= 1) continue;
            for (int t = 0; t < timeSlots.size(); t++) {
                List<Literal> activeAssignments = new ArrayList<>();
                for (EventToSchedule e : entry.getValue()) { addOverlappingLiterals(activeAssignments, e, t, x, timeSlots, locals); }
                if (activeAssignments.size() > 1) { model.addAtMostOne(activeAssignments); }
            }
        }
    }

    private <T> Map<T, Integer> indexList(List<T> list) {
        Map<T, Integer> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
        return map;
    }
}