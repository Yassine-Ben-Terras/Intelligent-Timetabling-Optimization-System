<!-- FICHIER MODIFIÉ : Votre composant Vue -->
<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
// MODIFIÉ : Import des nouveaux types et stores
import type { DataTableHeader, ProfesseurResponse, ProfParamPayload } from "../../types/types.ts";

import { useProfParamStore } from "../../stores/profParams";
import { useProfesseurStore } from "../../stores/professeurs";
import { useModuleStore } from "../../stores/modules";
import { useTypeSeanceStore } from "../../stores/typeSeance"; // NOUVEAU STORE

// --- STORE INITIALIZATION ---
const profParamStore = useProfParamStore();
const professeurStore = useProfesseurStore();
const moduleStore = useModuleStore();
const typeSeanceStore = useTypeSeanceStore(); // NOUVEAU STORE

// --- STATE ---
const { liaisons, loading: liaisonsLoading, error } = storeToRefs(profParamStore);
const { professeurs, loading: profsLoading } = storeToRefs(professeurStore);
const { modules } = storeToRefs(moduleStore);
const { typesSeance } = storeToRefs(typeSeanceStore); // NOUVEAU STORE

// --- UI STATE ---
const showManageDialog = ref(false);
const selectedProf = ref<ProfesseurResponse | null>(null);
const searchProf = ref('');
// Le searchModule est maintenant géré différemment, on peut le simplifier ou le garder
const searchModule = ref('');

// --- DATA PROCESSING ---
const tableLoading = computed(() => profsLoading.value || liaisonsLoading.value);

// MODIFIÉ : Cette computed property retourne maintenant des objets plus riches
const modulesAssignes = computed(() => {
  if (!selectedProf.value) return [];
  const assignations = [];
  for (const liaison of liaisons.value) {
    const [profId, modId, typeId] = liaison.split('-').map(Number);
    if (profId === selectedProf.value.id) {
      const module = modules.value.find(m => m.id === modId);
      const typeSeance = typesSeance.value.find(ts => ts.id === typeId);
      if (module && typeSeance) {
        assignations.push({ module, typeSeance });
      }
    }
  }
  return assignations.sort((a,b) => a.module.libelle.localeCompare(b.module.libelle));
});

const modulesFiltres = computed(() => {
  if (!searchModule.value) return modules.value;
  return modules.value.filter(module =>
      module.libelle.toLowerCase().includes(searchModule.value.toLowerCase())
  );
});

// --- COMPONENT CONFIGURATION ---
const profsHeaders: DataTableHeader[] = [
  // ... (inchangé)
  { key: "nom", title: "Nom" },
  { key: "prenom", title: "Prénom" },
  { key: "statut", title: "Statut" },
  { key: "actions", title: "Modules Assignés", sortable: false, align: 'end', width: '250px' },
];

// --- LIFECYCLE ---
onMounted(() => {
  Promise.all([
    profParamStore.fetchProfParams(),
    professeurStore.fetchProfesseurs(),
    moduleStore.fetchModules(),
    typeSeanceStore.fetchTypesSeance(), // NOUVEL APPEL API
  ]).catch(err => {
    console.error("Erreur lors du chargement des données:", err);
  });
});

// --- HANDLERS ---
function isAssigned(moduleId: number, typeSeanceId: number): boolean {
  if (!selectedProf.value) return false;
  return liaisons.value.has(`${selectedProf.value.id}-${moduleId}-${typeSeanceId}`);
}

function openManageDialog(prof: ProfesseurResponse) {
  searchModule.value = '';
  selectedProf.value = prof;
  showManageDialog.value = true;
}

function closeManageDialog() {
  showManageDialog.value = false;
  selectedProf.value = null;
}

// --- API ACTIONS ---
// MODIFIÉ : La signature et le payload ont changé
async function assigner(moduleId: number, typeSeanceId: number) {
  if (!selectedProf.value) return;
  const payload: ProfParamPayload = {
    professeurId: selectedProf.value.id,
    moduleId: moduleId,
    typeSeanceId: typeSeanceId,
  };
  await profParamStore.addProfParam(payload);
}

// MODIFIÉ : La signature et le payload ont changé
async function desassigner(moduleId: number, typeSeanceId: number) {
  if (!selectedProf.value) return;
  const payload: ProfParamPayload = {
    professeurId: selectedProf.value.id,
    moduleId: moduleId,
    typeSeanceId: typeSeanceId,
  };
  await profParamStore.deleteProfParam(payload);
}
</script>

<template>
  <div>
    <!-- Le tableau principal des professeurs reste inchangé -->
    <h1 class="text-h4 font-weight-bold mb-6">Affectation des Modules aux Professeurs</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>
    <v-card class="mb-6" variant="outlined">
      <!-- ... (inchangé) ... -->
      <v-data-table :headers="profsHeaders" :items="professeurs" :loading="tableLoading" :search="searchProf" hover density="compact">
        <template v-slot:item.actions="{ item }">
          <v-btn color="primary" variant="tonal" size="small" @click="openManageDialog(item)">
            <v-icon start>mdi-book-open-page-variant-outline</v-icon>
            Gérer les affectations
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- MODIFIÉ : Le contenu du dialogue est entièrement revu pour plus de clarté -->
    <v-dialog v-model="showManageDialog" max-width="900px" persistent scrollable>
      <v-card v-if="selectedProf">
        <v-card-title class="d-flex align-center pa-4">
          <v-icon start color="primary">mdi-account-school-outline</v-icon>
          <span class="text-h6">Affectations de : {{ selectedProf.prenom }} {{ selectedProf.nom }}</span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text>
          <v-row>
            <!-- Colonne des modules et des actions d'assignation -->
            <v-col cols="12" md="7">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Modules et types de séance à assigner</h3>
              <v-text-field
                  v-model="searchModule"
                  label="Rechercher un module..."
                  variant="outlined" density="compact" prepend-inner-icon="mdi-magnify" hide-details clearable class="mb-3"
              ></v-text-field>
              <v-list density="compact" class="border rounded-lg" style="max-height: 340px; overflow-y: auto;">
                <v-list-item
                    v-for="module in modulesFiltres"
                    :key="`module-${module.id}`"
                    :title="module.libelle"
                >
                  <template v-slot:append>
                    <div class="d-flex ga-1">
                      <v-chip
                          v-for="type in typesSeance"
                          :key="`type-${type.id}`"
                          :color="isAssigned(module.id, type.id) ? 'success' : 'default'"
                          :variant="isAssigned(module.id, type.id) ? 'elevated' : 'outlined'"
                          label
                          size="small"
                          class="cursor-pointer"
                          @click="isAssigned(module.id, type.id) ? desassigner(module.id, type.id) : assigner(module.id, type.id)"
                      >
                        <v-icon start :icon="isAssigned(module.id, type.id) ? 'mdi-check' : 'mdi-plus'"></v-icon>
                        {{ type.libelle }}
                      </v-chip>
                    </div>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesFiltres.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucun module trouvé.
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>

            <!-- Colonne des modules déjà assignés (résumé) -->
            <v-col cols="12" md="5">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Résumé des affectations</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <v-list-item
                    v-for="item in modulesAssignes"
                    :key="`linked-${item.module.id}-${item.typeSeance.id}`"
                >
                  <v-list-item-title>
                    {{ item.module.libelle }}
                    <v-chip color="primary" variant="flat" size="x-small" label class="ml-2">{{ item.typeSeance.libelle }}</v-chip>
                  </v-list-item-title>
                  <template v-slot:append>
                    <v-btn
                        icon="mdi-minus-circle-outline" variant="text" color="error" size="small"
                        @click="desassigner(item.module.id, item.typeSeance.id)"
                        title="Désassigner cette affectation"
                    ></v-btn>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesAssignes.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucune affectation.
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="elevated" @click="closeManageDialog">Fermer</v-btn>
        </v-card-actions>

      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>