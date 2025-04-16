<template>
  <div class="consultation-page">
    <!-- En-tête de la page -->
    <header class="page-header">
      <h1>Consultation des Emplois du Temps</h1>
      <button class="btn btn-availability" @click="showAvailabilityDialog = true" :disabled="store.loading || !store.allEntries.length">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
        Salles disponibles
      </button>
    </header>

    <!-- Bannière de statut (INCHANGÉ) -->
    <div v-if="store.pdfGenerationMessage || store.pdfArchivingMessage" class="status-banner" :class="{ 'is-archiving': !!store.pdfArchivingMessage }">
      <svg class="spinner" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
      <span>{{ store.pdfGenerationMessage || store.pdfArchivingMessage }}</span>
    </div>

    <!-- ============================================================= -->
    <!-- Panneau de contrôles : Filtres + Actions                      -->
    <!-- ============================================================= -->
    <section class="controls-container">

      <!-- ── Ligne des filtres ── -->
      <div class="filters-row">
        <div class="filter-group">
          <label for="filiere-select">Filière</label>
          <select id="filiere-select" :value="store.filters.filiere" @change="store.setFiliere(($event.target as HTMLSelectElement).value || null)" :disabled="store.loading">
            <option :value="null">-- Sélectionner une filière --</option>
            <option v-for="filiere in store.uniqueFilieres" :key="filiere" :value="filiere">{{ filiere }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label for="semestre-select">Semestre</label>
          <select id="semestre-select" :value="store.filters.semestre" @change="store.setSemestre(($event.target as HTMLSelectElement).value || null)" :disabled="store.loading || !store.filters.filiere">
            <option :value="null">-- Tous --</option>
            <option v-for="semestre in store.uniqueSemestres" :key="semestre" :value="semestre">{{ semestre }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label for="groupe-select">Groupe</label>
          <select id="groupe-select" :value="store.filters.groupe" @change="store.setGroupe(($event.target as HTMLSelectElement).value || null)" :disabled="store.loading || !store.filters.filiere">
            <option :value="null">-- Tous --</option>
            <option v-for="groupe in store.uniqueGroupes" :key="groupe" :value="groupe">{{ groupe }}</option>
          </select>
        </div>
        <div class="or-divider">OU</div>
        <div class="filter-group">
          <label for="prof-select">Professeur</label>
          <select id="prof-select" :value="store.filters.professeur" @change="store.setProfesseur(($event.target as HTMLSelectElement).value || null)" :disabled="store.loading">
            <option :value="null">-- Sélectionner un professeur --</option>
            <option v-for="prof in store.uniqueProfesseurs" :key="prof" :value="prof">{{ prof }}</option>
          </select>
        </div>
        <button @click="store.resetFilters()" class="btn-reset" :disabled="store.loading" title="Réinitialiser les filtres">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>
        </button>
      </div>

      <!-- ── Barre d'actions ── -->
      <div class="actions-toolbar">

        <!-- Groupe 1 : Export contextuel (filière sélectionnée) -->
        <div class="action-cluster">
          <span class="cluster-label">Export filière</span>
          <button @click="downloadFiliereGroup" class="btn btn-action-filiere" :disabled="!store.filters.filiere || store.loading">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><polyline points="9 15 12 18 15 15"/></svg>
            PDF {{ store.filters.filiere || 'Filière' }}
          </button>
        </div>

        <span class="toolbar-separator"></span>

        <!-- Groupe 2 : Exports globaux -->
        <div class="action-cluster">
          <span class="cluster-label">Exports globaux</span>
          <div class="cluster-buttons">
            <button @click="store.generatePdfAllFiliereSemestres()" class="btn btn-action-all" :disabled="store.loading">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 3H8l-2 4h12l-2-4z"/></svg>
              Toutes les Filières
            </button>
            <button @click="store.generatePdfAllProfessors()" class="btn btn-action-prof" :disabled="store.loading">
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Tous les Professeurs
            </button>
          </div>
        </div>

        <span class="toolbar-separator"></span>

        <!-- Groupe 3 : Archivage -->
        <div class="action-cluster">
          <span class="cluster-label">Archivage</span>
          <button @click="store.archiveAllFiliereSemestresPdf()" class="btn btn-archive" :disabled="store.loading">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
            Archiver tout
          </button>
        </div>

      </div>
    </section>

    <!-- ── Période de l'emploi du temps ── -->
    <section class="period-section">
      <div class="period-toggle">
        <label class="switch">
          <input type="checkbox" v-model="periodEnabled">
          <span class="slider"></span>
        </label>
        <span class="period-label">Afficher la période sur l'emploi du temps</span>
      </div>
      <div v-if="periodEnabled" class="period-inputs">
        <div class="period-field">
          <label>Du</label>
          <input type="date" v-model="periodStart" class="period-date">
        </div>
        <span class="period-arrow">→</span>
        <div class="period-field">
          <label>Au</label>
          <input type="date" v-model="periodEnd" class="period-date">
        </div>
        <span v-if="periodText" class="period-preview">{{ periodText }}</span>
      </div>
    </section>

    <!-- Section principale du contenu (Logique INCHANGÉE) -->
    <main class="content-section">
      <div v-if="store.loading" class="status-message loading">
        <svg class="spinner" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
        Chargement des données...
      </div>
      <div v-else-if="store.error" class="status-message error">
        <h3>Une erreur est survenue</h3>
        <p>{{ store.error }}</p>
      </div>
      <div v-else-if="!store.filters.filiere && !store.filters.professeur" class="status-message info-initial">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0h18M-4.5 12h22.5" /></svg>
        <h2>Consultez les emplois du temps</h2>
        <p>Pour commencer, veuillez sélectionner une filière ou un professeur en utilisant les filtres ci-dessus.</p>
      </div>
      <div v-else-if="!store.hasResults" class="status-message no-data">
        <h3>Aucun résultat</h3>
        <p>Il n'y a aucun cours correspondant à votre sélection.</p>
      </div>
      
      <div v-else>
        <PdfPreview 
          v-for="group in store.entriesByGroup"
          :key="group.title"
          :title="group.title"
          :entries="group.entries"
          :period-text="periodText"
        />
      </div>
    </main>

    <!-- ── Dialog : Recherche de Salles Disponibles ── -->
    <div v-if="showAvailabilityDialog" class="avail-overlay" @click.self="showAvailabilityDialog = false">
      <div class="avail-dialog">
        <div class="avail-header">
          <h2>
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            Recherche de salles disponibles
          </h2>
          <button class="avail-close" @click="showAvailabilityDialog = false">&times;</button>
        </div>

        <!-- Sélection : Jour + Plage horaire -->
        <div class="avail-search">
          <!-- Onglets jour -->
          <div class="search-row">
            <span class="search-label">Jour</span>
            <div class="avail-tabs">
              <button
                v-for="day in availableDays"
                :key="day"
                class="avail-tab"
                :class="{ active: selectedDay === day }"
                @click="onDaySelected(day)"
              >{{ day }}</button>
            </div>
          </div>
          <!-- Heure début / fin -->
          <div class="search-row">
            <span class="search-label">Plage horaire</span>
            <div class="time-pickers">
              <div class="time-field">
                <label for="time-start">De</label>
                <select id="time-start" v-model="searchStartTime">
                  <option value="">-- Début --</option>
                  <option v-for="t in availableStartTimes" :key="t" :value="t">{{ t }}</option>
                </select>
              </div>
              <span class="time-arrow">→</span>
              <div class="time-field">
                <label for="time-end">À</label>
                <select id="time-end" v-model="searchEndTime">
                  <option value="">-- Fin --</option>
                  <option v-for="t in availableEndTimes" :key="t" :value="t">{{ t }}</option>
                </select>
              </div>
              <button class="btn-search" @click="runSearch" :disabled="!canSearch">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                Rechercher
              </button>
            </div>
          </div>
        </div>

        <!-- Résultats -->
        <div class="avail-body">
          <!-- État initial -->
          <div v-if="!hasSearched" class="avail-empty">
            <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom:0.75rem;color:#10b981"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <p>Sélectionnez un <strong>jour</strong> et une <strong>plage horaire</strong>, puis cliquez sur <strong>Rechercher</strong>.</p>
          </div>
          <!-- Résultats trouvés -->
          <div v-else>
            <div class="results-summary">
              <span class="results-badge">{{ searchResults.length }}</span>
              salle(s) disponible(s) sur {{ totalRooms }} &mdash;
              <strong>{{ selectedDay }}, {{ searchStartTime }} → {{ searchEndTime }}</strong>
            </div>
            <div v-if="searchResults.length === 0" class="avail-empty">
              <p style="color:#ef4444;font-weight:600">Aucune salle n’est libre sur cette plage horaire.</p>
            </div>
            <div v-else class="results-grid">
              <div v-for="room in searchResults" :key="room.libelle" class="room-card">
                <div class="room-name">{{ room.libelle }}</div>
                <div class="room-meta">
                  <span class="room-capacity">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                    {{ room.capacite }} places
                  </span>
                  <span v-if="room.type" class="room-type">{{ room.type }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useConsultationStore } from '../../stores/consultation';
import { useLocalStore } from '../../stores/salle';
import { useCreneauStore } from '../../stores/creneaux';
import PdfPreview from '../../components/PdfPreview.vue';

const store = useConsultationStore();
const localStore = useLocalStore();
const creneauStore = useCreneauStore();

// UI state
const showAvailabilityDialog = ref(false);
const selectedDay = ref('');
const searchStartTime = ref('');
const searchEndTime = ref('');
const hasSearched = ref(false);
const searchResults = ref<{ libelle: string; capacite: number; type: string }[]>([]);

// Period state
const periodEnabled = ref(false);
const periodStart = ref('');
const periodEnd = ref('');

const periodText = computed(() => {
  if (!periodEnabled.value || !periodStart.value || !periodEnd.value) return '';
  const fmt = (d: string) => {
    const [y, m, day] = d.split('-');
    return `${day}/${m}/${y}`;
  };
  return `Période : ${fmt(periodStart.value)} au ${fmt(periodEnd.value)}`;
});

const DAY_ORDER = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];

const totalRooms = computed(() => localStore.locaux.length);

// All unique days from creneaux, sorted
const availableDays = computed(() => {
  const days = [...new Set(creneauStore.creneaux.map(c => c.jour.libelle))];
  return days.sort((a, b) => DAY_ORDER.indexOf(a) - DAY_ORDER.indexOf(b));
});

// Generate time options (every 30 min from 08:00 to 19:00)
const allTimeOptions = computed(() => {
  const times: string[] = [];
  for (let h = 8; h <= 19; h++) {
    times.push(`${h.toString().padStart(2, '0')}:00`);
    if (h < 19) times.push(`${h.toString().padStart(2, '0')}:30`);
  }
  return times;
});

// Filter end times to only show times after the selected start
const availableStartTimes = computed(() => allTimeOptions.value);
const availableEndTimes = computed(() => {
  if (!searchStartTime.value) return allTimeOptions.value;
  return allTimeOptions.value.filter(t => t > searchStartTime.value);
});

const canSearch = computed(() => {
  return selectedDay.value && searchStartTime.value && searchEndTime.value && searchStartTime.value < searchEndTime.value;
});

// Reset search state when day changes
function onDaySelected(day: string) {
  selectedDay.value = day;
  hasSearched.value = false;
  searchResults.value = [];
}

// Auto-select first day when dialog opens
import { watch } from 'vue';

// Sync periodText into the store so that global PDF export buttons also include the period subtitle
watch(periodText, (val) => {
  store.periodSubtitle = val;
}, { immediate: true });
watch(showAvailabilityDialog, (open) => {
  if (open) {
    hasSearched.value = false;
    searchResults.value = [];
    searchStartTime.value = '';
    searchEndTime.value = '';
    if (availableDays.value.length > 0) {
      selectedDay.value = availableDays.value[0];
    }
  }
});

// Time overlap helper: does [aStart, aEnd) overlap with [bStart, bEnd)?
function timesOverlap(aStart: string, aEnd: string, bStart: string, bEnd: string): boolean {
  return aStart < bEnd && bStart < aEnd;
}

// Run search: find rooms with NO overlapping entries in the selected window
function runSearch() {
  if (!canSearch.value) return;

  // Find all rooms occupied (even partially) during the selected window
  const occupiedRoomLabels = new Set(
    store.allEntries
      .filter(e =>
        e.jourLibelle === selectedDay.value &&
        timesOverlap(e.heureDebut, e.heureFin, searchStartTime.value, searchEndTime.value)
      )
      .map(e => e.localLibelle)
  );

  // Available = all rooms minus occupied
  searchResults.value = localStore.locaux
    .filter(r => !occupiedRoomLabels.has(r.libelle))
    .map(r => ({
      libelle: r.libelle,
      capacite: r.capacite,
      type: r.type?.libelle || ''
    }))
    .sort((a, b) => a.libelle.localeCompare(b.libelle));

  hasSearched.value = true;
}

onMounted(async () => {
  store.fetchAllEmplois();
  // Also load rooms and time slots for the availability feature
  await Promise.all([
    localStore.fetchLocaux(),
    creneauStore.fetchCreneaux()
  ]);
});

const downloadFiliereGroup = () => {
  if (store.filters.filiere) {
    store.generatePdfForFiliereGroup(store.filters.filiere);
  }
};
</script>

<style scoped>
/* ============================================================= */
/* START MODIFICATION CSS: Nouveaux styles et réorganisation */
/* ============================================================= */
:root {
  --font-family: 'Inter', system-ui, sans-serif;
  --bg-main: #f4f7fa;
  --bg-controls: #ffffff;
  --bg-content: #ffffff;
  --text-primary: #1a202c;
  --text-secondary: #4a5568;
  --text-muted: #a0aec0;
  --border-color: #e2e8f0;
  --shadow: 0 4px 6px -1px rgb(0 0 0 / 0.05), 0 2px 4px -2px rgb(0 0 0 / 0.05);
  --radius: 0.75rem;
}

.consultation-page { 
  font-family: var(--font-family); 
  padding: 1.5rem 2rem; 
  background-color: var(--bg-main); 
  color: var(--text-primary); 
}

.page-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 1.5rem; 
}

.page-header h1 { 
  font-size: 1.75rem; 
  font-weight: 700; 
}

.header-info { 
  text-align: right; 
  color: var(--text-secondary); 
  font-size: 0.9rem; 
}

/* NOUVEAU STYLE pour le conteneur des contrôles */
.controls-container {
  background-color: var(--bg-controls);
  padding: 1.5rem;
  border-radius: var(--radius);
  margin-bottom: 2rem;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow);
}

/* Ligne de filtres horizontale */
.filters-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  align-items: flex-end;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 1.25rem;
}

.filter-group { 
  display: flex; 
  flex-direction: column; 
}

.filter-group label { 
  margin-bottom: 0.4rem; 
  font-size: 0.8rem; 
  color: var(--text-secondary); 
  font-weight: 600; 
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.filter-group select { 
  padding: 0.5rem 0.7rem;
  border-radius: 0.5rem; 
  border: 1px solid var(--border-color); 
  min-width: 220px;
  background-color: #ffffff; 
  transition: all 0.2s; 
  font-size: 0.9rem;
}

.filter-group select:focus { 
  border-color: #3b82f6; 
  outline: none; 
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); 
}

.or-divider { 
  align-self: center; 
  color: var(--text-muted); 
  font-weight: 600; 
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background-color: #f1f5f9;
  padding: 0.25rem 0.6rem;
  border-radius: 0.375rem;
  margin-bottom: 0.15rem;
}

/* Bouton de réinitialisation */
.btn-reset {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  cursor: pointer;
  color: var(--text-muted);
  width: 38px; 
  height: 38px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease-in-out;
  margin-bottom: 0.15rem;
}

.btn-reset:hover:not(:disabled) {
  background-color: #fef2f2;
  border-color: #fca5a5;
  color: #ef4444;
}

.btn-reset:disabled {
  color: #e2e8f0;
  cursor: not-allowed;
}

/* ── Barre d'actions ── */
.actions-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 1rem;
}

.action-cluster {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.cluster-label {
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.cluster-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.toolbar-separator {
  align-self: stretch;
  width: 1px;
  background-color: var(--border-color);
  margin: 0 0.5rem;
}

/* ── Boutons d'action ── */
.actions-toolbar .btn { 
  padding: 0.5rem 0.85rem;
  border-radius: 0.5rem; 
  border: none; 
  font-weight: 600; 
  font-size: 0.8rem;
  cursor: pointer; 
  transition: all 0.2s ease; 
  color: white;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  white-space: nowrap;
}

.actions-toolbar .btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.actions-toolbar .btn:active:not(:disabled) {
  transform: translateY(0);
}

.actions-toolbar .btn:disabled { 
  background-color: #e2e8f0 !important; 
  color: #94a3b8 !important; 
  cursor: not-allowed; 
  transform: none !important; 
  box-shadow: none !important;
}

.btn-action-filiere { background-color: #8b5cf6; } 
.btn-action-filiere:hover:not(:disabled) { background-color: #7c3aed; }
.btn-action-all { background-color: #3b82f6; } 
.btn-action-all:hover:not(:disabled) { background-color: #2563eb; }
.btn-action-prof { background-color: #0ea5e9; }
.btn-action-prof:hover:not(:disabled) { background-color: #0284c7; }
.btn-archive { background-color: #f59e0b; } 
.btn-archive:hover:not(:disabled) { background-color: #d97706; }

.content-section { 
  background-color: var(--bg-content); 
  border-radius: var(--radius); 
  box-shadow: var(--shadow); 
  border: 1px solid var(--border-color); 
  padding: 2rem; 
  min-height: 50vh; 
}

.status-message { 
  padding: 3rem 2rem; 
  text-align: center; 
}

.status-message h3, .status-message h2 { 
  font-weight: 600; 
  margin-bottom: 0.5rem; 
}

.status-message p { 
  color: var(--text-secondary); 
}

.info-initial svg { 
  width: 48px; 
  margin: 0 auto 1rem; 
  color: #3b82f6; 
}

.status-banner { 
  display: flex; 
  align-items: center; 
  gap: 0.75rem; 
  padding: 1rem 1.25rem; 
  margin-bottom: 1.5rem; 
  border-radius: 0.5rem; 
  font-weight: 500; 
  background-color: #e0f2fe; 
  color: #0c4a6e; 
}

.status-banner.is-archiving { 
  background-color: #ffedd5; 
  color: #9a3412; 
}

.spinner { 
  animation: rotate 2s linear infinite; 
  width: 24px; 
  height: 24px; 
}

.spinner .path { 
  stroke: currentColor; 
  stroke-linecap: round; 
  animation: dash 1.5s ease-in-out infinite; 
}

@keyframes rotate { 100% { transform: rotate(360deg); } }
@keyframes dash { 0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; } 50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; } 100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; } }

/* ── Bouton Salles disponibles (header) ── */
.btn-availability {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  border: 1px solid #10b981;
  background-color: #ecfdf5;
  color: #065f46;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s ease;
}
.btn-availability:hover:not(:disabled) {
  background-color: #10b981;
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}
.btn-availability:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Dialog Overlay ── */
.avail-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.avail-dialog {
  background: white;
  border-radius: 1rem;
  width: 90vw;
  max-width: 900px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: slideUp 0.25s ease;
  overflow: hidden;
}
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.avail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, #ecfdf5, #f0fdf4);
}
.avail-header h2 {
  font-size: 1.2rem;
  font-weight: 700;
  color: #065f46;
  margin: 0;
}
.avail-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  transition: all 0.15s;
}
.avail-close:hover {
  background: #fee2e2;
  color: #ef4444;
}

/* Onglets jours */
.avail-tabs {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
.avail-tab {
  padding: 0.4rem 0.85rem;
  border: 1px solid var(--border-color);
  background: #f8fafc;
  font-weight: 600;
  font-size: 0.8rem;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: 0.375rem;
  transition: all 0.15s;
  white-space: nowrap;
}
.avail-tab:hover { color: var(--text-secondary); background: #f1f5f9; }
.avail-tab.active {
  color: white;
  background: #059669;
  border-color: #059669;
}

/* Zone de recherche */
.avail-search {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: #f9fafb;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}
.search-label {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-muted);
  min-width: 90px;
}
.time-pickers {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.time-field {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}
.time-field label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
}
.time-field select {
  padding: 0.4rem 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: 0.375rem;
  font-size: 0.85rem;
  background: white;
  min-width: 100px;
}
.time-field select:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}
.time-arrow {
  color: var(--text-muted);
  font-size: 1.1rem;
  font-weight: 600;
}
.btn-search {
  padding: 0.45rem 1rem;
  border: none;
  border-radius: 0.5rem;
  background: #10b981;
  color: white;
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  transition: all 0.2s;
}
.btn-search:hover:not(:disabled) {
  background: #059669;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}
.btn-search:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Corps */
.avail-body {
  padding: 1.25rem 1.5rem;
  overflow-y: auto;
  flex: 1;
}
.avail-empty {
  text-align: center;
  padding: 2.5rem 1rem;
  color: var(--text-muted);
}
.avail-empty p { margin: 0; }

/* Résultats */
.results-summary {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #f0fdf4;
  border: 1px solid #a7f3d0;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
}
.results-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  background: #10b981;
  color: white;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.85rem;
  padding: 0 0.5rem;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem;
}

/* Carte salle */
.room-card {
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  padding: 0.75rem 1rem;
  background: white;
  transition: all 0.15s;
}
.room-card:hover {
  border-color: #10b981;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.12);
}
.room-name {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--text-primary);
  margin-bottom: 0.35rem;
}
.room-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}
.room-capacity {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}
.room-type {
  background: #f1f5f9;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--text-secondary);
}

/* ── Période ── */
.period-section {
  background: var(--surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  padding: 0.75rem 1.25rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.period-toggle {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.period-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
}

/* Toggle switch */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;
}
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #cbd5e1;
  border-radius: 22px;
  transition: 0.25s;
}
.slider::before {
  content: "";
  position: absolute;
  height: 16px;
  width: 16px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  border-radius: 50%;
  transition: 0.25s;
}
input:checked + .slider {
  background-color: #3b82f6;
}
input:checked + .slider::before {
  transform: translateX(18px);
}

.period-inputs {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  animation: fadeIn 0.2s ease;
}

.period-field {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.period-field label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.period-date {
  padding: 0.35rem 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: 0.4rem;
  font-size: 0.85rem;
  color: var(--text-primary);
  background: white;
  outline: none;
  transition: border-color 0.15s;
}
.period-date:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

.period-arrow {
  font-size: 1.1rem;
  color: var(--text-muted);
  font-weight: 600;
}

.period-preview {
  font-size: 0.8rem;
  font-weight: 600;
  color: #3b82f6;
  background: #eff6ff;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid #bfdbfe;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>