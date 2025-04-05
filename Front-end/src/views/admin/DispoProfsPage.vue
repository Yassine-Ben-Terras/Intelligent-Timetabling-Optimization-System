<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import type { DataTableHeader, CreneauResponse, ProfesseurResponse, DispoProfPayload } from "../../types/types.ts";

import { useDispoProfStore } from "../../stores/dispoProfs";
import { useProfesseurStore } from "../../stores/professeurs";
import { useCreneauStore } from "../../stores/creneaux";
import { useJourStore } from "../../stores/jours";

const dispoProfStore = useDispoProfStore();
const professeurStore = useProfesseurStore();
const creneauStore = useCreneauStore();
const jourStore = useJourStore();

const { disponibilités, loading: dispoLoading, error } = storeToRefs(dispoProfStore);
const { professeurs, loading: profsLoading } = storeToRefs(professeurStore);
const { creneaux } = storeToRefs(creneauStore);
const { jours } = storeToRefs(jourStore);

const showDispoDialog = ref(false);
const selectedProf = ref<ProfesseurResponse | null>(null);
const search = ref('');

const tableLoading = computed(() => profsLoading.value || dispoLoading.value);

const headers: DataTableHeader[] = [
  { key: "nom", title: "Nom" },
  { key: "prenom", title: "Prénom" },
  { key: "departement.libelle", title: "Département" },
  { key: "actions", title: "", sortable: false, align: 'end', width: '120px' },
];

onMounted(() => {
  Promise.all([
    dispoProfStore.fetchDispoProfs(),
    professeurStore.fetchProfesseurs(),
    creneauStore.fetchCreneaux(),
    jourStore.fetchJours(),
  ]).catch(err => {
    console.error("Erreur lors du chargement des données:", err);
  });
});

function openDispoDialog(prof: ProfesseurResponse) {
  selectedProf.value = prof;
  showDispoDialog.value = true;
}

function closeDispoDialog() {
  showDispoDialog.value = false;
  selectedProf.value = null;
}

// --- LOGIQUE DE DISPONIBILITÉ ---
function isPeriodeDisponible(profId: number, jourId: number, periode: 'MATIN' | 'SOIR'): boolean {
  return disponibilités.value.has(`${profId}-${jourId}-${periode}`);
}

async function togglePeriodeDisponibilite(profId: number, jourId: number, periode: 'MATIN' | 'SOIR') {
  const estActuellementDisponible = isPeriodeDisponible(profId, jourId, periode);

  const payload: DispoProfPayload = { profId, jourId, periode };

  if (estActuellementDisponible) {
    await dispoProfStore.updateDisponibilites([], [payload]);
  } else {
    await dispoProfStore.updateDisponibilites([payload], []);
  }
}


</script>

<template>
  <div class="dispo-page">
    <div class="dispo-header">
      <div>
        <h1 class="dispo-title">Disponibilités des Professeurs</h1>
        <p class="dispo-subtitle">Gérez les créneaux de disponibilité pour chaque professeur</p>
      </div>
    </div>

    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <v-card class="mb-6" variant="outlined" rounded="lg">
      <v-card-title class="d-flex align-center pa-4">
        <v-icon start color="primary">mdi-account-group</v-icon>
        <span class="text-h6">Liste des professeurs</span>
        <v-spacer></v-spacer>
        <v-text-field
            v-model="search"
            label="Rechercher..."
            variant="outlined"
            density="compact"
            prepend-inner-icon="mdi-magnify"
            hide-details
            single-line
            style="max-width: 280px;"
        ></v-text-field>
      </v-card-title>
      <v-divider></v-divider>

      <v-data-table
          :headers="headers"
          :items="professeurs"
          :loading="tableLoading"
          :search="search"
          hover
          density="comfortable"
          items-per-page="15"
      >
        <!-- Action button -->
        <template v-slot:item.actions="{ item }">
          <v-btn
              color="primary"
              variant="tonal"
              size="small"
              @click="openDispoDialog(item)"
          >
            <v-icon start>mdi-calendar-clock</v-icon>
            Gérer
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Dialog de disponibilité amélioré -->
    <v-dialog v-model="showDispoDialog" max-width="750px" persistent>
      <v-card v-if="selectedProf" rounded="lg">
        <div class="dialog-header">
          <div class="dialog-header-info">
            <v-avatar color="primary" size="42">
              <span class="text-h6 text-white">{{ selectedProf.prenom[0] }}{{ selectedProf.nom[0] }}</span>
            </v-avatar>
            <div>
              <h2 class="dialog-prof-name">{{ selectedProf.prenom }} {{ selectedProf.nom }}</h2>
              <span class="dialog-prof-dept">{{ selectedProf.departement?.libelle }}</span>
            </div>
          </div>
          <v-btn icon="mdi-close" variant="text" size="small" @click="closeDispoDialog"></v-btn>
        </div>

        <v-divider></v-divider>

        <v-card-text class="pa-5">
          <div class="legend-row">
            <span class="legend-item">
              <span class="legend-dot disponible"></span> Disponible
            </span>
            <span class="legend-item">
              <span class="legend-dot indisponible"></span> Indisponible
            </span>
            <span class="legend-hint">Cliquez pour basculer</span>
          </div>

          <!-- Grille visuelle -->
          <div class="dispo-grid">
            <!-- Header row -->
            <div class="grid-header">
              <div class="grid-cell grid-label"></div>
              <div class="grid-cell grid-col-header">
                <v-icon size="16" class="mr-1">mdi-weather-sunny</v-icon>
                Matin
              </div>
              <div class="grid-cell grid-col-header">
                <v-icon size="16" class="mr-1">mdi-weather-sunset</v-icon>
                Après-midi
              </div>
            </div>

            <!-- Day rows -->
            <div
              v-for="jour in jours"
              :key="jour.id"
              class="grid-row"
            >
              <div class="grid-cell grid-label">
                <span class="day-name">{{ jour.libelle }}</span>
              </div>
              <div
                class="grid-cell grid-toggle"
                :class="{ active: isPeriodeDisponible(selectedProf.id, jour.id, 'MATIN') }"
                @click="togglePeriodeDisponibilite(selectedProf.id, jour.id, 'MATIN')"
              >
                <v-icon size="18">
                  {{ isPeriodeDisponible(selectedProf.id, jour.id, 'MATIN') ? 'mdi-check-circle' : 'mdi-close-circle-outline' }}
                </v-icon>
                <span>{{ isPeriodeDisponible(selectedProf.id, jour.id, 'MATIN') ? 'Disponible' : 'Indisponible' }}</span>
              </div>
              <div
                class="grid-cell grid-toggle"
                :class="{ active: isPeriodeDisponible(selectedProf.id, jour.id, 'SOIR') }"
                @click="togglePeriodeDisponibilite(selectedProf.id, jour.id, 'SOIR')"
              >
                <v-icon size="18">
                  {{ isPeriodeDisponible(selectedProf.id, jour.id, 'SOIR') ? 'mdi-check-circle' : 'mdi-close-circle-outline' }}
                </v-icon>
                <span>{{ isPeriodeDisponible(selectedProf.id, jour.id, 'SOIR') ? 'Disponible' : 'Indisponible' }}</span>
              </div>
            </div>
          </div>
        </v-card-text>

        <v-divider></v-divider>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="elevated" @click="closeDispoDialog">
            <v-icon start>mdi-check</v-icon>
            Terminé
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.dispo-page {
  padding: 0.5rem;
}

.dispo-header {
  margin-bottom: 1.5rem;
}

.dispo-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.dispo-subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.25rem 0 0 0;
}


/* Dialog header */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #eff6ff, #f0f9ff);
}

.dialog-header-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dialog-prof-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.dialog-prof-dept {
  font-size: 0.8rem;
  color: #64748b;
}

/* Légende */
.legend-row {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  background: #f8fafc;
  border-radius: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #475569;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.disponible {
  background: #10b981;
}

.legend-dot.indisponible {
  background: #e2e8f0;
  border: 1px solid #cbd5e1;
}

.legend-hint {
  margin-left: auto;
  font-size: 0.75rem;
  color: #94a3b8;
  font-style: italic;
}

/* Grille visuelle */
.dispo-grid {
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  overflow: hidden;
}

.grid-header {
  display: grid;
  grid-template-columns: 140px 1fr 1fr;
  background: #f1f5f9;
  border-bottom: 2px solid #e2e8f0;
}

.grid-row {
  display: grid;
  grid-template-columns: 140px 1fr 1fr;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.grid-row:last-child {
  border-bottom: none;
}

.grid-row:hover {
  background: #fafbfd;
}

.grid-cell {
  padding: 0.6rem 0.75rem;
  display: flex;
  align-items: center;
}

.grid-col-header {
  font-size: 0.8rem;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  justify-content: center;
}

.grid-label {
  border-right: 1px solid #e2e8f0;
}

.day-name {
  font-weight: 600;
  font-size: 0.85rem;
  color: #334155;
}

/* Toggle cells */
.grid-toggle {
  justify-content: center;
  gap: 0.4rem;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  color: #94a3b8;
  border-left: 1px solid #f1f5f9;
  transition: all 0.2s;
  user-select: none;
}

.grid-toggle:hover {
  background: #f8fafc;
}

.grid-toggle.active {
  background: #ecfdf5;
  color: #059669;
}

.grid-toggle.active:hover {
  background: #d1fae5;
}

.grid-toggle .v-icon {
  color: inherit;
}
</style>