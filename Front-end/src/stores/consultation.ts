import { defineStore } from "pinia";
import { ref, computed } from "vue";
import jsPDF from "jspdf";
import apiClient from "../services/api";
import type { TimetableEntryDTO, TimetableFilters } from "../types/types";
import { useAnneeUniversitaireStore } from "./annees";
import { useSessionStore } from "./sessions";

// =============================================================================
// CHANGEMENTS PAR RAPPORT À LA VERSION PRÉCÉDENTE
// =============================================================================
// 1. GRILLE PDF : pas de 30 min → pas de 15 min.
//    La fonction buildTimeSlots() génère maintenant des créneaux de 15 min
//    pour correspondre aux heures fines renvoyées par le backend (ex. 11:45).
//
// 2. PAUSE DÉJEUNER CONFIGURABLE dans le PDF :
//    La constante PDF_LUNCH_START / PDF_LUNCH_END permet d'afficher la plage
//    de pause déjeuner avec un fond visuel distinct dans la grille PDF.
//    Ces valeurs doivent correspondre à timetable.solver.lunch.start/end.
//
// 3. PAUSE INTER-SÉANCES VISIBLE dans le PDF :
//    Les créneaux de 15 min entre deux séances (la pause obligatoire) sont
//    maintenant des colonnes à part entière dans la grille. Le fond de ces
//    colonnes est légèrement grisé pour les rendre lisibles.
//
// 4. LARGEUR DES COLONNES adaptée :
//    Avec 4x plus de colonnes (15 min au lieu de 30 min sur une plage 8h-18h),
//    TIME_SLOT_WIDTH est recalculé dynamiquement. Les labels d'heure n'affichent
//    que les heures rondes et les demi-heures pour éviter la surcharge visuelle.
// =============================================================================

// -----------------------------------------------------------------------------
// CONFIGURATION PDF — à synchroniser avec application.properties backend
// -----------------------------------------------------------------------------

/** Début de la pause déjeuner (doit correspondre à timetable.solver.lunch.start) */
const PDF_LUNCH_START = "11:45";

/** Fin de la pause déjeuner (doit correspondre à timetable.solver.lunch.end) */
const PDF_LUNCH_END = "13:00";

/**
 * Granularité de la grille PDF en minutes.
 * DOIT correspondre à TimetableServiceV6.SLOT_DURATION_MINUTES (= 15).
 */
const PDF_SLOT_MINUTES = 15;

// --- Helpers de Couleur (INCHANGÉS) ---
function hslToRgb(h: number, s: number, l: number): [number, number, number] {
    s /= 100; l /= 100;
    const k = (n: number) => (n + h / 30) % 12;
    const a = s * Math.min(l, 1 - l);
    const f = (n: number) => l - a * Math.max(-1, Math.min(k(n) - 3, 9 - k(n), 1));
    return [Math.round(255 * f(0)), Math.round(255 * f(8)), Math.round(255 * f(4))];
}

const generateColorFromString = (str: string, saturation = 70, lightness = 88) => {
  let hash = 0;
  const safeStr = str || 'default';
  for (let i = 0; i < safeStr.length; i++) hash = safeStr.charCodeAt(i) + ((hash << 5) - hash);
  const hue = hash % 360;
  return {
    rgb: hslToRgb(hue, saturation, lightness),
    rgbDark: hslToRgb(hue, saturation - 10, lightness - 20),
  };
};

// =============================================================================
// CHANGEMENT #1 — Constructeur de créneaux horaires à granularité variable
// =============================================================================
/**
 * Génère la liste des créneaux horaires pour la grille PDF.
 *
 * @param minHour  Heure de début (entier, ex. 8)
 * @param maxHour  Heure de fin   (entier, ex. 18)
 * @param stepMin  Pas en minutes (ex. 15)
 * @returns Tableau de chaînes "HH:MM"
 *
 * Exemple avec stepMin=15, minHour=8, maxHour=10 :
 *   ["08:00","08:15","08:30","08:45","09:00","09:15","09:30","09:45","10:00"]
 *   Le dernier créneau "10:00" représente le début du créneau 10:00-10:15,
 *   mais on supprime le tout-dernier élément car il correspond à la borne de fin.
 */
function buildTimeSlots(minHour: number, maxHour: number, stepMin: number): string[] {
  const slots: string[] = [];
  let totalMinutes = minHour * 60;
  const endMinutes  = maxHour * 60;

  while (totalMinutes < endMinutes) {
    const h = Math.floor(totalMinutes / 60);
    const m = totalMinutes % 60;
    slots.push(`${h.toString().padStart(2, "0")}:${m.toString().padStart(2, "0")}`);
    totalMinutes += stepMin;
  }
  return slots;
}

// =============================================================================
// CHANGEMENT #2 — Helper : un créneau "HH:MM" est-il dans la pause déjeuner ?
// =============================================================================
/**
 * Retourne true si le créneau `slotStart` (inclusif) se situe dans
 * la plage de pause déjeuner [lunchStart, lunchEnd[.
 */
function isLunchSlot(slotStart: string, lunchStart: string, lunchEnd: string): boolean {
  return slotStart >= lunchStart && slotStart < lunchEnd;
}

// =============================================================================
// CHANGEMENT #3 — Helper : label visible pour un créneau horaire
// Avec 15 min de granularité, on affiche seulement les :00 et :30 pour ne
// pas surcharger l'axe des abscisses du PDF.
// =============================================================================
function shouldShowLabel(time: string): boolean {
  return time.endsWith(":00") || time.endsWith(":30");
}

export const useConsultationStore = defineStore("consultation", () => {
  // --- STORES EXTERNES ---
  const anneeStore   = useAnneeUniversitaireStore();
  const sessionStore = useSessionStore();

  // --- STATE (INCHANGÉ) ---
  const loading              = ref(false);
  const error                = ref<string | null>(null);
  const allEntries           = ref<TimetableEntryDTO[]>([]);
  const filters              = ref<TimetableFilters>({ filiere: null, semestre: null, groupe: null, professeur: null });
  const pdfGenerationMessage = ref<string | null>(null);
  const pdfArchivingMessage  = ref<string | null>(null);

  // --- GETTERS (INCHANGÉS) ---
  const globalAcademicYear = computed(() => anneeStore.annees.find(a => a.id)?.libelle || "Année");
  const globalSessionName  = computed(() => sessionStore.sessions.find(s => s.id)?.libelle || "Session");

  const filteredEntries = computed(() =>
    allEntries.value.filter(e => {
      const f = filters.value;
      return (
        (!f.filiere     || e.filiereLibelle === f.filiere) &&
        (!f.semestre    || e.semestreLibelle === f.semestre) &&
        (!f.professeur  || `${e.profPrenom} ${e.profNom}` === f.professeur)
      );
    })
  );

  const uniqueFilieres   = computed(() => [...new Set(allEntries.value.map(e => e.filiereLibelle))].sort());
  const uniqueSemestres  = computed(() => {
    const entries = filters.value.filiere
      ? allEntries.value.filter(e => e.filiereLibelle === filters.value.filiere)
      : [];
    return [...new Set(entries.map(e => e.semestreLibelle))].sort();
  });
  const uniqueProfesseurs = computed(() =>
    [...new Set(allEntries.value.map(e => `${e.profPrenom} ${e.profNom}`))].sort()
  );
  const hasResults = computed(() => filteredEntries.value.length > 0);

  const entriesByGroup = computed(() => {
    if (!hasResults.value) return [];
    const groups: { title: string; entries: TimetableEntryDTO[] }[] = [];
    const f = filters.value;

    if (f.professeur) {
      groups.push({ title: `Professeur : ${f.professeur}`, entries: filteredEntries.value });
      return groups;
    }

    if (f.filiere) {
      const uniqueGroupKeys = [...new Set(
        filteredEntries.value.map(e => `${e.semestreLibelle}|${e.sectionLibelle || ''}`)
      )].sort();

      uniqueGroupKeys.forEach(key => {
        const [semestre, section] = key.split('|');
        const groupEntries = filteredEntries.value.filter(
          e => e.semestreLibelle === semestre && (e.sectionLibelle || '') === section
        );
        if (groupEntries.length > 0) {
          const sectionTitle = section ? ` - ${section}` : '';
          groups.push({ title: `${f.filiere} - ${semestre}${sectionTitle}`, entries: groupEntries });
        }
      });
      return groups;
    }

    return [];
  });

  // --- FILTRES (INCHANGÉS) ---
  function setFiliere(v: string | null) {
    if (filters.value.filiere !== v) {
      filters.value.semestre  = null;
      filters.value.professeur = null;
    }
    filters.value.filiere = v;
  }
  const setSemestre = (v: string | null) => (filters.value.semestre = v);
  function setProfesseur(v: string | null) {
    if (v) {
      filters.value.filiere  = null;
      filters.value.semestre = null;
    }
    filters.value.professeur = v;
  }
  const resetFilters = () => (filters.value = { filiere: null, semestre: null, groupe: null, professeur: null });

  // --- FETCH (INCHANGÉ) ---
  async function fetchAllEmplois() {
    loading.value = true; error.value = null;
    try {
      await anneeStore.fetchAnnees(); await sessionStore.fetchSessions();
      const { data } = await apiClient.get<TimetableEntryDTO[]>("/emploi/all");
      allEntries.value = data;
    } catch (err: any) {
      error.value = err.response?.data?.message || "Une erreur est survenue.";
    } finally { loading.value = false; }
  }

  // ===========================================================================
  // CHANGEMENT #4 — drawModernTimetableOnPdf : grille 15 min + pause visible
  // ===========================================================================
  /**
   * Dessine l'emploi du temps sur une page jsPDF.
   *
   * Modifications par rapport à la version précédente :
   *   A) buildTimeSlots() remplace la boucle hardcodée à 30 min.
   *   B) La plage de pause déjeuner est colorée en gris clair dans la grille.
   *   C) Les labels horaires n'apparaissent que pour :00 et :30 (lisibilité).
   *   D) Les séances sont positionnées avec une précision de 15 min, ce qui
   *      leur permet d'être placées à des horaires comme 09:45, 11:30, 13:00…
   */
  const drawModernTimetableOnPdf = (
    doc: jsPDF,
    title: string,
    entries: TimetableEntryDTO[]
  ) => {
    if (!entries || entries.length === 0) {
      doc.text("Aucune donnée disponible pour cette sélection.", 14, 20);
      return;
    }

    // ── Construction de la grille ──────────────────────────────────────────
    const dayOrder = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"];
    const days     = [...new Set(entries.map(e => e.jourLibelle))]
      .sort((a, b) => dayOrder.indexOf(a) - dayOrder.indexOf(b));

    // Déterminer l'étendue horaire réelle des données
    const allTimes = entries.flatMap(e => [e.heureDebut, e.heureFin]).sort();
    const minHour  = Math.floor(Math.min(8,  parseInt(allTimes[0].slice(0, 2))));
    const maxHour  = Math.ceil( Math.max(18, parseInt(allTimes[allTimes.length - 1].slice(0, 2))));

    // CHANGEMENT A : granularité 15 min (PDF_SLOT_MINUTES)
    const timeSlots = buildTimeSlots(minHour, maxHour, PDF_SLOT_MINUTES);

    // ── Dimensions de la page ──────────────────────────────────────────────
    const MARGIN           = { top: 30, left: 25, right: 10, bottom: 10 };
    const PAGE_WIDTH       = doc.internal.pageSize.getWidth();
    const GRID_WIDTH       = PAGE_WIDTH - MARGIN.left - MARGIN.right;
    const DAY_HEADER_HEIGHT = 10;
    const DAY_ROW_HEIGHT   = 18;
    // CHANGEMENT D : TIME_SLOT_WIDTH calculé dynamiquement selon le nombre de colonnes
    const TIME_SLOT_WIDTH  = GRID_WIDTH / timeSlots.length;

    // ── Titre ──────────────────────────────────────────────────────────────
    doc.setFontSize(16); doc.setFont("helvetica", "bold");
    doc.text(title, MARGIN.left, 20);
    doc.setFontSize(8); doc.setFont("helvetica", "normal");

    // ── Fond gris pour la pause déjeuner (CHANGEMENT B) ───────────────────
    // On colore les colonnes correspondant à [PDF_LUNCH_START, PDF_LUNCH_END[
    // en gris très clair pour les distinguer visuellement des créneaux de cours.
    timeSlots.forEach((time, index) => {
      if (isLunchSlot(time, PDF_LUNCH_START, PDF_LUNCH_END)) {
        const x = MARGIN.left + index * TIME_SLOT_WIDTH;
        const totalGridHeight = DAY_HEADER_HEIGHT + days.length * DAY_ROW_HEIGHT;
        // Fond gris clair pour toute la colonne de pause déjeuner
        doc.setFillColor(245, 245, 245);
        doc.rect(x, MARGIN.top, TIME_SLOT_WIDTH, totalGridHeight, "F");
      }
    });

    // ── Étiquettes des jours (colonne gauche) ─────────────────────────────
    days.forEach((day, index) => {
      const y = MARGIN.top + DAY_HEADER_HEIGHT + index * DAY_ROW_HEIGHT;
      doc.setFillColor(248, 250, 252);
      doc.rect(MARGIN.left - 15, y, 15, DAY_ROW_HEIGHT, "F");
      doc.setTextColor(74, 85, 104);
      doc.text(day, MARGIN.left - 8, y + DAY_ROW_HEIGHT / 2, {
        align: "center", baseline: "middle",
      });
    });

    // ── Lignes verticales + labels horaires ───────────────────────────────
    timeSlots.forEach((time, index) => {
      const x = MARGIN.left + index * TIME_SLOT_WIDTH;

      // CHANGEMENT C : n'afficher les labels que pour :00 et :30
      if (shouldShowLabel(time)) {
        doc.setTextColor(160, 174, 192);
        doc.text(
          time,
          x,
          MARGIN.top + DAY_HEADER_HEIGHT / 2,
          { align: "center", baseline: "middle" }
        );
      }

      // Ligne verticale de grille
      doc.setDrawColor(226, 232, 240);
      doc.line(
        x, MARGIN.top + DAY_HEADER_HEIGHT,
        x, MARGIN.top + DAY_HEADER_HEIGHT + days.length * DAY_ROW_HEIGHT
      );
    });

    // ── Dessin des séances ─────────────────────────────────────────────────
    // CHANGEMENT D (suite) : les positions x et la largeur sont calculées avec
    // la même précision de 15 min, donc les blocs "8:00–9:30" s'étendent sur
    // 6 colonnes × TIME_SLOT_WIDTH exactement.
    entries.forEach(entry => {
      const dayIndex   = days.indexOf(entry.jourLibelle);
      const startIndex = timeSlots.indexOf(entry.heureDebut);
      let   endIndex   = timeSlots.indexOf(entry.heureFin);
      if (endIndex === -1) endIndex = timeSlots.length;
      if (dayIndex === -1 || startIndex === -1) return;

      const x      = MARGIN.left + startIndex * TIME_SLOT_WIDTH;
      const y      = MARGIN.top + DAY_HEADER_HEIGHT + dayIndex * DAY_ROW_HEIGHT;
      const width  = (endIndex - startIndex) * TIME_SLOT_WIDTH;
      const height = DAY_ROW_HEIGHT - 1;

      const { rgb, rgbDark } = generateColorFromString(entry.moduleLibelle);

      // Fond coloré de la séance
      doc.setFillColor(rgb[0], rgb[1], rgb[2]);
      doc.rect(x + 0.5, y + 0.5, width - 1, height, "F");

      // Barre colorée à gauche
      doc.setDrawColor(rgbDark[0], rgbDark[1], rgbDark[2]);
      doc.setLineWidth(0.5);
      doc.line(x + 0.5, y + 0.5, x + 0.5, y + 0.5 + height);

      // Texte dans le bloc
      const PADDING      = 2;
      const LINE_H7      = 3;
      const LINE_H6      = 2.5;
      let currentY       = y + PADDING + 1.5;

      doc.setTextColor(51, 51, 51);
      doc.setFontSize(7); doc.setFont("helvetica", "bold");
      const moduleText = doc.splitTextToSize(
        `${entry.moduleLibelle} (${entry.typeSeance})`,
        width - PADDING * 2
      );
      doc.text(moduleText, x + PADDING, currentY);
      currentY += moduleText.length * LINE_H7;

      if (currentY < y + height - LINE_H6) {
        doc.setFontSize(6); doc.setFont("helvetica", "normal");
        const profText = doc.splitTextToSize(
          `${entry.profPrenom} ${entry.profNom}`,
          width - PADDING * 2
        );
        doc.text(profText, x + PADDING, currentY + 1);
        currentY += profText.length * LINE_H6 + 1;
      }

      if (currentY < y + height - LINE_H6) {
        const publicName   = (entry.groupeLibelle || entry.sectionLibelle || "").split("_")[0];
        const locationText = doc.splitTextToSize(
          `${entry.localLibelle} | ${publicName}`,
          width - PADDING * 2
        );
        doc.text(locationText, x + PADDING, currentY);
      }
    });

    // ── Label "PAUSE DÉJEUNER" centré sur la zone grisée (CHANGEMENT B suite) ──
    const lunchStartIdx = timeSlots.indexOf(PDF_LUNCH_START);
    const lunchEndIdx   = timeSlots.indexOf(PDF_LUNCH_END);
    if (lunchStartIdx !== -1 && lunchEndIdx !== -1) {
      const lx      = MARGIN.left + lunchStartIdx * TIME_SLOT_WIDTH;
      const lWidth  = (lunchEndIdx - lunchStartIdx) * TIME_SLOT_WIDTH;
      const lMiddle = lx + lWidth / 2;
      const lTop    = MARGIN.top + DAY_HEADER_HEIGHT / 2;
      doc.setFontSize(5.5); doc.setFont("helvetica", "italic");
      doc.setTextColor(180, 180, 180);
      doc.text("Pause déj.", lMiddle, lTop, { align: "center", baseline: "middle" });
    }
  };

  // --- createAndSavePdf (INCHANGÉ) ---
  const createAndSavePdf = (fileName: string, drawFunction: (doc: jsPDF) => void) => {
    pdfGenerationMessage.value = "Génération du PDF en cours...";
    try {
      const doc = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
      drawFunction(doc);
      doc.save(fileName);
    } catch (e) {
      console.error("Erreur de génération PDF", e);
    } finally {
      pdfGenerationMessage.value = null;
    }
  };

  // --- Fonctions de génération PDF (INCHANGÉES — utilisent drawModernTimetableOnPdf) ---

  function generatePdfForFiliereGroup(filiere: string) {
    if (!filiere) return;
    const filiereEntries = allEntries.value.filter(e => e.filiereLibelle === filiere);
    if (!filiereEntries.length) return alert(`Aucune donnée pour la filière ${filiere}.`);

    const fileName = `emploi_${filiere.replace(/\s/g, "_")}.pdf`;
    createAndSavePdf(fileName, (doc) => {
      let isFirstPage = true;
      const uniqueGroupKeys = [
        ...new Set(filiereEntries.map(e => `${e.semestreLibelle}|${e.sectionLibelle || ''}`))
      ].sort();

      uniqueGroupKeys.forEach(key => {
        const [semestre, section] = key.split("|");
        const groupEntries = filiereEntries.filter(
          e => e.semestreLibelle === semestre && (e.sectionLibelle || '') === section
        );
        if (groupEntries.length > 0) {
          if (!isFirstPage) doc.addPage("a4", "landscape");
          const sectionTitle = section ? ` - ${section}` : "";
          drawModernTimetableOnPdf(doc, `${filiere} - ${semestre}${sectionTitle}`, groupEntries);
          isFirstPage = false;
        }
      });
    });
  }

  function generatePdfAllFiliereSemestres() {
    if (!allEntries.value.length) return alert("Aucune donnée à exporter.");
    createAndSavePdf("emploi_toutes_filieres_semestres.pdf", (doc) => {
      let isFirstPage = true;
      uniqueFilieres.value.forEach((filiere) => {
        const filiereEntries = allEntries.value.filter(e => e.filiereLibelle === filiere);
        const uniqueGroupKeys = [
          ...new Set(filiereEntries.map(e => `${e.semestreLibelle}|${e.sectionLibelle || ''}`))
        ].sort();

        uniqueGroupKeys.forEach(key => {
          const [semestre, section] = key.split("|");
          const groupEntries = filiereEntries.filter(
            e => e.semestreLibelle === semestre && (e.sectionLibelle || '') === section
          );
          if (groupEntries.length > 0) {
            if (!isFirstPage) doc.addPage("a4", "landscape");
            const sectionTitle = section ? ` - ${section}` : "";
            drawModernTimetableOnPdf(doc, `${filiere} - ${semestre}${sectionTitle}`, groupEntries);
            isFirstPage = false;
          }
        });
      });
    });
  }

  function generatePdfAllProfessors() {
    if (!allEntries.value.length) return alert("Aucune donnée à exporter.");
    createAndSavePdf("emploi_tous_professeurs.pdf", (doc) => {
      let isFirstPage = true;
      uniqueProfesseurs.value.forEach(prof => {
        const profEntries = allEntries.value.filter(
          e => `${e.profPrenom} ${e.profNom}` === prof
        );
        if (profEntries.length > 0) {
          if (!isFirstPage) doc.addPage("a4", "landscape");
          drawModernTimetableOnPdf(doc, `Professeur: ${prof}`, profEntries);
          isFirstPage = false;
        }
      });
    });
  }

  async function archiveAllFiliereSemestresPdf() {
    if (!allEntries.value.length) return;
    pdfArchivingMessage.value = "Préparation de l'archive...";

    const doc = new jsPDF({ orientation: "landscape", unit: "mm", format: "a4" });
    let isFirstPage = true;

    uniqueFilieres.value.forEach((filiere) => {
      const filiereEntries = allEntries.value.filter(e => e.filiereLibelle === filiere);
      const uniqueGroupKeys = [
        ...new Set(filiereEntries.map(e => `${e.semestreLibelle}|${e.sectionLibelle || ''}`))
      ].sort();

      uniqueGroupKeys.forEach(key => {
        const [semestre, section] = key.split("|");
        const groupEntries = filiereEntries.filter(
          e => e.semestreLibelle === semestre && (e.sectionLibelle || '') === section
        );
        if (groupEntries.length > 0) {
          if (!isFirstPage) doc.addPage("a4", "landscape");
          const sectionTitle = section ? ` - ${section}` : "";
          drawModernTimetableOnPdf(doc, `${filiere} - ${semestre}${sectionTitle}`, groupEntries);
          isFirstPage = false;
        }
      });
    });

    const blob = doc.output("blob");
    const filename = `archive_emplois_${globalAcademicYear.value.replace(/\s/g, "_")}_${globalSessionName.value.replace(/\s/g, "_")}.pdf`;

    pdfArchivingMessage.value = "Archivage du PDF en cours...";
    try {
      const formData = new FormData();
      formData.append("file", blob, filename);
      await apiClient.post("/pdfs/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      pdfArchivingMessage.value = "PDF archivé avec succès !";
    } catch (e: any) {
      pdfArchivingMessage.value = `Erreur d'archivage: ${e.message}`;
    } finally {
      setTimeout(() => (pdfArchivingMessage.value = null), 4000);
    }
  }

  return {
    loading, error, allEntries, filters, pdfGenerationMessage, pdfArchivingMessage,
    filteredEntries, uniqueFilieres, uniqueSemestres, uniqueProfesseurs, hasResults,
    globalAcademicYear, globalSessionName,
    entriesByGroup,
    setFiliere, setSemestre, setProfesseur, resetFilters,
    fetchAllEmplois,
    generatePdfForFiliereGroup, generatePdfAllProfessors, generatePdfAllFiliereSemestres,
    archiveAllFiliereSemestresPdf,
    drawModernTimetableOnPdf,
  };
});