// src/stores/consultationStore.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import apiClient from '../services/api';
import type { TimetableEntryDTO, TimetableFilters } from '../types/types';
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

export const useConsultationStore = defineStore("consultation", () => {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const allEntries = ref<TimetableEntryDTO[]>([]);
  const filters = ref<TimetableFilters>({
    filiere: null,
    semestre: null,
    groupe: null,
    professeur: null
  });
  const globalAcademicYear = ref("2024-2025");
  const globalSessionName = ref("Automne");
  const pdfGenerationMessage = ref<string | null>(null);

  // --- FILTRES & DONNÉES DÉRIVÉES ---
  const filteredEntries = computed(() => {
    return allEntries.value.filter(e => {
      const matchFiliere = !filters.value.filiere || e.filiereLibelle === filters.value.filiere;
      const matchSemestre = !filters.value.semestre || e.semestreLibelle === filters.value.semestre;
      const matchProf = !filters.value.professeur || `${e.profPrenom} ${e.profNom}` === filters.value.professeur;
      return matchFiliere && matchSemestre && matchProf;
    });
  });

  const uniqueFilieres = computed(() =>
    [...new Set(allEntries.value.map(e => e.filiereLibelle))].sort()
  );

  const uniqueSemestres = computed(() => {
    const entriesToScan = filters.value.filiere ? filteredEntries.value : allEntries.value;
    return [...new Set(entriesToScan.map(e => e.semestreLibelle))].sort();
  });

  const uniqueProfesseurs = computed(() =>
    [...new Set(allEntries.value.map(e => `${e.profPrenom} ${e.profNom}`))].sort()
  );

  const hasResults = computed(() => filteredEntries.value.length > 0);

  // --- REQUÊTES ---
  async function fetchAllEmplois() {
    loading.value = true;
    error.value = null;
    try {
      const { data } = await apiClient.get<TimetableEntryDTO[]>("/emploi/all");
      allEntries.value = data;
    } catch (err: any) {
      error.value =
        err.response?.data?.message || "Erreur lors de la récupération des emplois.";
      allEntries.value = [];
    } finally {
      loading.value = false;
    }
  }

  // --- GESTION FILTRES ---
  function setFiliere(v: string | null) {
    if (filters.value.filiere !== v) filters.value.semestre = null;
    filters.value.filiere = v;
  }
  function setSemestre(v: string | null) {
    filters.value.semestre = v;
  }
  function setProfesseur(v: string | null) {
    filters.value.professeur = v;
  }
  function resetFilters() {
    filters.value = { filiere: null, semestre: null, groupe: null, professeur: null };
  }

  // --- ARCHIVAGE ---
  async function archivePdf(file: Blob, fileName: string) {
    try {
      const formData = new FormData();
      formData.append("file", file, fileName);
      await apiClient.post("/archives/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });
      alert("PDF archivé avec succès !");
    } catch (err) {
      console.error("Erreur archivage :", err);
      alert("Erreur lors de l'archivage");
    }
  }

  // --- OUTIL POUR LES TABLES ---
  const createTableBody = (entries: TimetableEntryDTO[]) => {
    return entries
      .sort((a, b) => a.heureDebut.localeCompare(b.heureDebut))
      .map(e => [
        e.jourLibelle,
        `${e.heureDebut} - ${e.heureFin}`,
                e.moduleLibelle,
        e.typeSeance,
        `${e.profPrenom} ${e.profNom}`,
        e.localLibelle,
        e.groupeLibelle || e.sectionLibelle
      ]);
  };

  // --- GÉNÉRATION + ARCHIVAGE ---
  function generatePdfCurrentSelection() {
    if (!hasResults.value) return alert("Rien à exporter.");
    pdfGenerationMessage.value = "Génération du PDF...";
    try {
      const doc = new jsPDF({ orientation: "landscape" });
      const head = [['Jour', 'Heure', 'Module', 'Type', 'Professeur', 'Salle', 'Public']];
      const body = createTableBody(filteredEntries.value);

      doc.text(`Emploi du temps - ${filters.value.filiere || filters.value.professeur || 'Sélection'}`, 14, 10);
      autoTable(doc, { head, body, startY: 15 });

      const fileName = `emploi_selection.pdf`;
      const pdfBlob = doc.output("blob");

      doc.save(fileName); // Sauvegarde locale
      archivePdf(pdfBlob, fileName); // Archivage
    } finally {
      pdfGenerationMessage.value = null;
    }
  }

  function generatePdfAllFiliereSemestres() {
    if (!allEntries.value.length) return;
    pdfGenerationMessage.value = "Génération du PDF complet...";
    try {
      const doc = new jsPDF({ orientation: "landscape" });
      const head = [['Jour', 'Heure', 'Module', 'Type', 'Professeur', 'Salle', 'Public']];

      uniqueFilieres.value.forEach((filiere, iF) => {
        const filiereEntries = allEntries.value.filter(e => e.filiereLibelle === filiere);
        const semestres = [...new Set(filiereEntries.map(e => e.semestreLibelle))].sort();
        semestres.forEach((semestre, iS) => {
          const semestreEntries = filiereEntries.filter(e => e.semestreLibelle === semestre);
          if (semestreEntries.length) {
            doc.text(`Filière: ${filiere} - Semestre: ${semestre}`, 14, 10);
            autoTable(doc, { head, body: createTableBody(semestreEntries), startY: 15 });
            if (!(iF === uniqueFilieres.value.length - 1 && iS === semestres.length - 1)) doc.addPage();
          }
        });
      });

      const fileName = "emploi_complet.pdf";
      const pdfBlob = doc.output("blob");
      doc.save(fileName);
      archivePdf(pdfBlob, fileName);
    } finally {
      pdfGenerationMessage.value = null;
    }
  }

  function generatePdfForFiliereGroup(filiere: string) {
    if (!filiere) return;
    pdfGenerationMessage.value = `Génération PDF pour ${filiere}...`;
    try {
      const doc = new jsPDF({ orientation: "landscape" });
      const head = [['Jour', 'Heure', 'Module', 'Type', 'Professeur', 'Salle', 'Public']];
      const filiereEntries = allEntries.value.filter(e => e.filiereLibelle === filiere);
      const semestres = [...new Set(filiereEntries.map(e => e.semestreLibelle))].sort();

      semestres.forEach((semestre, idx) => {
        const semestreEntries = filiereEntries.filter(e => e.semestreLibelle === semestre);
        if (semestreEntries.length) {
          doc.text(`Filière: ${filiere} - Semestre: ${semestre}`, 14, 10);
          autoTable(doc, { head, body: createTableBody(semestreEntries), startY: 15 });
          if (idx < semestres.length - 1) doc.addPage();
        }
      });

      const fileName = `emploi_${filiere}.pdf`;
      const pdfBlob = doc.output("blob");
      doc.save(fileName);
      archivePdf(pdfBlob, fileName);
    } finally {
      pdfGenerationMessage.value = null;
    }
  }

  function generatePdfAllProfessors() {
    if (!allEntries.value.length) return;
    pdfGenerationMessage.value = "Génération PDF tous professeurs...";
    try {
      const doc = new jsPDF({ orientation: "landscape" });
      const head = [['Jour', 'Heure', 'Module', 'Type', 'Filière', 'Salle', 'Public']];

      uniqueProfesseurs.value.forEach((prof, idx) => {
        const profEntries = allEntries.value.filter(e => `${e.profPrenom} ${e.profNom}` === prof);
        if (profEntries.length) {
          doc.text(`Professeur: ${prof}`, 14, 10);
          autoTable(doc, {
            head,
            body: profEntries.map(e => [
              e.jourLibelle,
              `${e.heureDebut} - ${e.heureFin}`,
              e.moduleLibelle,
              e.typeSeance,
              e.filiereLibelle,
              e.localLibelle,
              e.groupeLibelle || e.sectionLibelle
            ]),
            startY: 15
          });
          if (idx < uniqueProfesseurs.value.length - 1) doc.addPage();
        }
      });

      const fileName = "emploi_tous_professeurs.pdf";
      const pdfBlob = doc.output("blob");
      doc.save(fileName);
      archivePdf(pdfBlob, fileName);
    } finally {
      pdfGenerationMessage.value = null;
    }
  }

  return {
    loading,
    error,
    allEntries,
    filters,
    globalAcademicYear,
    globalSessionName,
    pdfGenerationMessage,
    filteredEntries,
    uniqueFilieres,
    uniqueSemestres,
    uniqueProfesseurs,
    hasResults,
    fetchAllEmplois,
    setFiliere,
    setSemestre,
    setProfesseur,
    resetFilters,
    generatePdfCurrentSelection,
    generatePdfAllFiliereSemestres,
    generatePdfForFiliereGroup,
    generatePdfAllProfessors
  };
});
