<template>
  <div class="consultation-page-container">
    <header class="page-header">
      <div class="academic-year-info">
        Année universitaire : {{ store.globalAcademicYear }}
      </div>
    </header>

    <div class="main-title-block">
      Emploi du temps de la session de {{ store.globalSessionName }}
    </div>

    <section class="filters-section">
      <div class="filter-group">
        <label for="filiere-select">Filière:</label>
        <select id="filiere-select" :value="store.selectedFiliere" @change="store.setFiliere(($event.target as HTMLSelectElement)?.value || null)">
          <option :value="null">-- Toutes les filières --</option>
          <option v-for="filiere in store.uniqueFilieres" :key="filiere" :value="filiere">{{ filiere }}</option>
        </select>
      </div>

      <div class="filter-group">
        <label for="semestre-select">Semestre:</label>
        <select
            id="semestre-select"
            :value="store.selectedSemestre"
            @change="store.setSemestre(($event.target as HTMLSelectElement)?.value || null)"
            :disabled="!store.selectedFiliere">
          <option :value="null">-- Tous les semestres --</option>
          <option v-for="semestre in store.uniqueSemestres" :key="semestre" :value="semestre">{{ semestre }}</option>
        </select>
      </div>

      <button @click="store.resetFilters()" class="btn btn-reset">Réinitialiser</button>
      <button
          @click="handleDownloadPdf"
          class="btn btn-download"
          :disabled="store.loading || isPdfButtonDisabled">
        <span v-if="pdfDownloadInitiated">Génération PDF...</span>
        <span v-else>Télécharger PDF (Grille)</span>
      </button>
    </section>

    <section class="content-section">
      <div v-if="store.loading && !pdfDownloadInitiated" class="status-message loading">Chargement des données...</div>
      <div v-if="store.error" class="status-message error">Erreur: {{ store.error }}</div>

      <div v-if="!store.loading && !store.error">
        <div v-if="store.selectedFiliere && hasDataForGrid">
          <div v-for="(semData, semestreName) in store.dataForGridDisplayAndPdf" :key="semestreName" class="semestre-grid-wrapper">
            <div class="semester-details-header">
              <p>Département : {{ semData.departmentDisplay }}</p>
              <p>Filière : {{ semData.filiereDisplay }}</p>
              <p>Semestre : {{ semestreName }}</p>
            </div>

            <table class="timetable-grid" v-if="semData.days && semData.timeSlots && semData.timeSlots.length > 0">
              <thead>
              <tr>
                <th class="corner-cell"></th>
                <!-- Les en-têtes utilisent maintenant les CANONICAL_TIME_SLOTS -->
                <th v-for="timeSlotKey in semData.timeSlots" :key="timeSlotKey" class="time-slot-th">
                  {{ formatTimeSlotForDisplay(timeSlotKey) }}
                </th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="day in semData.days" :key="day">
                <td class="day-td">{{ day }}</td>
                <!-- Les cellules utilisent maintenant les CANONICAL_TIME_SLOTS pour l'itération -->
                <td v-for="timeSlotKey in semData.timeSlots" :key="timeSlotKey"
                    :class="{'has-entry': semData.grid?.[day]?.[timeSlotKey]?.length > 0, 'empty-cell': !semData.grid?.[day]?.[timeSlotKey]?.length}">
                  <div v-if="semData.grid?.[day]?.[timeSlotKey]?.length > 0">
                    <div v-for="entry in semData.grid[day][timeSlotKey]" :key="entry.id" class="grid-entry-item">
                      <div><strong>Mod:</strong> {{ entry.module }}{{ entry.type !== 'N/A' ? ' (' + entry.type + ')' : '' }}</div>
                      <div><strong>Prof:</strong> {{ entry.professeur }}</div>
                      <div><strong>Salle:</strong> {{ entry.local }}</div>
                      <!-- Optionnel: afficher les heures exactes de la séance si elles diffèrent du créneau canonique -->
                      <!-- <div v-if="entry.heureDebut + '-' + entry.heureFin !== timeSlotKey" style="font-size: 0.8em; color: grey;">
                        (Séance: {{ entry.heureDebut }}-{{ entry.heureFin }})
                      </div> -->
                    </div>
                  </div>
                  <div v-else class="empty-cell-visual-stripe"></div>
                </td>
              </tr>
              </tbody>
            </table>
            <div v-else class="status-message info">
              Structure de grille (jours/créneaux) non disponible ou vide pour {{semestreName}}.
            </div>
          </div>
        </div>
        <div v-else-if="store.selectedFiliere && (!store.dataForGridDisplayAndPdf || Object.keys(store.dataForGridDisplayAndPdf).length === 0) && !store.loading" class="status-message no-data">
          Aucun emploi du temps trouvé pour la filière et le semestre sélectionnés.
          Vérifiez que les horaires des cours peuvent être mappés aux créneaux standards définis.
        </div>
        <div v-else-if="!store.selectedFiliere && !store.loading" class="status-message info">
          Veuillez sélectionner une filière pour afficher l'emploi du temps.
        </div>
        <div v-else-if="store.allEntries && store.allEntries.length === 0 && !store.loading && !store.error" class="status-message no-data">
          Aucune donnée d'emploi du temps disponible initialement.
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useEmploiStore } from '../../stores/Fix';

const store = useEmploiStore();
const pdfDownloadInitiated = ref(false);

onMounted(async () => {
  if (!store.allEntries || store.allEntries.length === 0) {
    await store.fetchEmplois();
  }
});

const hasDataForGrid = computed(() => {
  if (!store.dataForGridDisplayAndPdf || Object.keys(store.dataForGridDisplayAndPdf).length === 0) {
    return false;
  }
  for (const semestreName in store.dataForGridDisplayAndPdf) {
    const semData = store.dataForGridDisplayAndPdf[semestreName];
    if (semData && semData.days && semData.days.length > 0 && semData.timeSlots && semData.timeSlots.length > 0) {
      return true;
    }
  }
  return false;
});

const isPdfButtonDisabled = computed(() => {
  return pdfDownloadInitiated.value || !store.dataForGridDisplayAndPdf || Object.keys(store.dataForGridDisplayAndPdf).length === 0;
});

const handleDownloadPdf = async () => {
  if (!store.dataForGridDisplayAndPdf || Object.keys(store.dataForGridDisplayAndPdf).length === 0) {
    alert("Aucune structure de données d'emploi du temps n'est disponible pour générer un PDF.");
    return;
  }

  if (store.loading) {
    alert("Veuillez attendre la fin du chargement des données.");
    return;
  }

  pdfDownloadInitiated.value = true;
  try {
    await new Promise(resolve => setTimeout(resolve, 50));
    store.generatePdfGrid();
  } catch (e) {
    console.error("Error caught in handleDownloadPdf:", e);
    alert("Une erreur s'est produite lors du lancement de la génération PDF.");
  } finally {
    setTimeout(() => { pdfDownloadInitiated.value = false; }, 1500);
  }
};

const formatTimeSlotForDisplay = (timeSlotKey: string): string => {
  if (!timeSlotKey || !timeSlotKey.includes('-')) return timeSlotKey;
  const [start, end] = timeSlotKey.split('-');
  const formatPart = (part: string) => (part || "00:00").replace(':', 'H');
  return `${formatPart(start)} - ${formatPart(end)}`;
};
</script>

<style scoped>
/* Styles restent les mêmes */
.consultation-page-container {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  padding: 20px;
  background-color: #ffffff;
  color: #333;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 3px solid orange;
  padding-bottom: 10px;
  margin-bottom: 15px;
  font-size: 0.85em;
}
.university-info { text-align: right; }
.university-info p { margin: 1px 0; }
.academic-year-info { font-weight: bold; font-size: 1.1em; padding-top: 5px;}

.main-title-block {
  text-align: center;
  font-size: 1.8em;
  font-weight: 600;
  padding: 12px;
  border: 2px solid #4A5568;
  margin-bottom: 25px;
  background-color: #F7FAFC;
  color: #2D3748;
  border-radius: 6px;
  box-shadow: 0 3px 6px rgba(0,0,0,0.05);
}

.filters-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 25px;
  padding: 20px;
  background-color: #EDF2F7;
  border-radius: 8px;
  align-items: center;
}

.filter-group { display: flex; flex-direction: column; }
.filter-group label { margin-bottom: 6px; font-size: 0.95em; color: #4A5568; font-weight: 500; }
.filter-group select {
  padding: 9px 12px; border-radius: 6px; border: 1px solid #CBD5E0;
  min-width: 200px; background-color: white; font-size: 0.95em;
}
.filter-group select:disabled { background-color: #e2e8f0; cursor: not-allowed; }

.btn {
  padding: 10px 18px; border: none; border-radius: 6px; cursor: pointer;
  font-weight: 500; font-size: 0.95em; transition: background-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.btn-reset { background-color: #ECC94B; color: #2D3748; }
.btn-reset:hover { background-color: #D69E2E; }
.btn-download { background-color: #48BB78; color: white; margin-left: auto; }
.btn-download:hover:not(:disabled) { background-color: #38A169; }
.btn-download:disabled { background-color: #A0AEC0; cursor: not-allowed; }

.status-message { text-align: center; padding: 25px; font-size: 1.1em; border-radius: 6px; margin-top: 10px; }
.loading { background-color: #EBF8FF; color: #3182CE; }
.error { background-color: #FFF5F5; color: #C53030; border: 1px solid #FC8181; }
.no-data { background-color: #FEFCE8; color: #A16207; border: 1px solid #FDE68A;}
.info { background-color: #E6FFFA; color: #319795; border: 1px solid #81E6D9; }


.semestre-grid-wrapper {
  margin-bottom: 35px; padding: 15px; border: 1px solid #E2E8F0;
  border-radius: 8px; background-color: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.03);
  overflow-x: auto;
}

.semester-details-header { text-align: right; margin-bottom: 12px; font-size: 0.95em; color: #4A5568; }
.semester-details-header p { margin: 3px 0; font-weight: 500; }

.timetable-grid { width: 100%; border-collapse: collapse; border: 1px solid #4A5568; }
.timetable-grid th, .timetable-grid td {
  border: 1px solid #CBD5E0;
  padding: 6px; text-align: center;
  min-width: 100px;
  height: 80px;
  vertical-align: top; font-size: 0.8em;
}

.corner-cell { background-color: #F7FAFC; border-color: #4A5568; }
.time-slot-th {
  background-color: #E2E8F0;
  font-weight: 600; color: #2D3748; font-size: 0.85em;
  white-space: pre-line;
}
.day-td {
  background-color: #4A5568;
  color: white; font-weight: 600;
  text-align: center; vertical-align: middle;
  width: 100px;
  font-size: 0.9em;
  border-color: #4A5568;
}

.grid-entry-item {
  font-size: 0.95em;
  padding: 4px; text-align: left; border-radius: 3px;
  margin-bottom: 3px; background-color: #f0f4f8;
  border-left: 3px solid #63B3ED;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.grid-entry-item:last-child { margin-bottom: 0; }
.grid-entry-item div { margin-bottom: 2px; word-break: break-word; line-height: 1.3; }
.grid-entry-item strong { color: #2C5282; }

.empty-cell .empty-cell-visual-stripe {
  background: repeating-linear-gradient(
      -45deg,
      rgba(237, 242, 247, 0.15),
      rgba(237, 242, 247, 0.15) 5px,
      transparent 5px,
      transparent 10px
  );
  width: 100%; height: 100%;
}
.has-entry { background-color: #fff; }

@media (max-width: 768px) {
  .filters-section { flex-direction: column; align-items: stretch; }
  .filter-group select { width: 100%; }
  .btn-download { margin-left: 0; margin-top: 10px; }
  .page-header { flex-direction: column; align-items: center; text-align: center; }
  .university-info { text-align: center; margin-bottom: 10px; }
  .academic-year-info { text-align: center;}
  .timetable-grid th, .timetable-grid td { min-width: 80px; font-size: 0.75em;}
  .day-td { width: 80px; }
}
</style>