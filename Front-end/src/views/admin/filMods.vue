<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import type { DataTableHeader, FiliereResponse, ModuleResponse, FilModPayload } from "../../types/types.ts";

// Import des stores nécessaires
import { useFilModStore } from "../../stores/filMods";
import { useFiliereStore } from "../../stores/filieres";
import { useModuleStore } from "../../stores/modules";

// --- STORE INITIALIZATION ---
const filModStore = useFilModStore();
const filiereStore = useFiliereStore();
const moduleStore = useModuleStore();

// --- STATE ---
const { liaisons, loading: liaisonsLoading, error } = storeToRefs(filModStore);
const { filieres, loading: filieresLoading } = storeToRefs(filiereStore);
const { modules } = storeToRefs(moduleStore);

// --- UI STATE ---
const showManageDialog = ref(false);
const selectedFiliere = ref<FiliereResponse | null>(null);
const searchFiliere = ref(''); // Recherche pour la table principale
const searchModule = ref('');  // <<--- NOUVEAU : Recherche pour les modules disponibles

// --- DATA PROCESSING ---
const tableLoading = computed(() => filieresLoading.value || liaisonsLoading.value);

const modulesLies = computed<ModuleResponse[]>(() => {
  if (!selectedFiliere.value) return [];
  return modules.value.filter(module =>
      liaisons.value.has(`${selectedFiliere.value!.id}-${module.id}`)
  );
});

// <<--- MODIFICATION : On filtre maintenant aussi par le terme de recherche ---
const modulesNonLies = computed<ModuleResponse[]>(() => {
  if (!selectedFiliere.value) return [];

  // 1. On trouve les modules non liés
  let resultat = modules.value.filter(module =>
      !liaisons.value.has(`${selectedFiliere.value!.id}-${module.id}`)
  );

  // 2. Si un terme de recherche est saisi, on filtre cette liste
  if (searchModule.value) {
    resultat = resultat.filter(module =>
        module.libelle.toLowerCase().includes(searchModule.value.toLowerCase())
    );
  }

  return resultat;
});

// --- COMPONENT CONFIGURATION ---
const filieresHeaders: DataTableHeader[] = [
  { key: "libelle", title: "Nom de la Filière" },
  { key: "actions", title: "Gérer les Modules", sortable: false, align: 'end', width: '250px' },
];

// --- LIFECYCLE ---
onMounted(() => {
  Promise.all([
    filModStore.fetchFilMods(),
    filiereStore.fetchFilieres(),
    moduleStore.fetchModules(),
  ]).catch(err => {
    console.error("Erreur lors du chargement des données:", err);
  });
});

// --- HANDLERS ---
function openManageDialog(filiere: FiliereResponse) {
  searchModule.value = ''; // On réinitialise la recherche à chaque ouverture
  selectedFiliere.value = filiere;
  showManageDialog.value = true;
}

function closeManageDialog() {
  showManageDialog.value = false;
  selectedFiliere.value = null;
}

// --- API ACTIONS ---
async function lierModule(moduleId: number) {
  if (!selectedFiliere.value) return;
  const payload: FilModPayload = {
    filiereId: selectedFiliere.value.id,
    moduleId: moduleId,
  };
  await filModStore.addFilMod(payload);
}

async function delierModule(moduleId: number) {
  if (!selectedFiliere.value) return;
  const payload: FilModPayload = {
    filiereId: selectedFiliere.value.id,
    moduleId: moduleId,
  };
  await filModStore.deleteFilMod(payload);
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Liaison des Modules aux Filières</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <v-card class="mb-6" variant="outlined">
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">Sélectionnez une filière pour gérer ses modules</span>
        <v-spacer></v-spacer>
        <v-text-field
            v-model="searchFiliere"
            label="Rechercher une filière..."
            variant="outlined"
            density="compact"
            prepend-inner-icon="mdi-magnify"
            hide-details
            single-line
            style="max-width: 300px;"
        ></v-text-field>
      </v-card-title>
      <v-divider></v-divider>

      <v-data-table
          :headers="filieresHeaders"
          :items="filieres"
          :loading="tableLoading"
          :search="searchFiliere"
          hover
          density="compact"
      >
        <template v-slot:item.actions="{ item }">
          <v-btn
              color="primary"
              variant="tonal"
              size="small"
              @click="openManageDialog(item)"
          >
            <v-icon start>mdi-link-variant</v-icon>
            Gérer les modules
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="showManageDialog" max-width="900px" persistent scrollable>
      <v-card v-if="selectedFiliere">
        <v-card-title class="d-flex align-center pa-4">
          <v-icon start color="primary">mdi-school-outline</v-icon>
          <span class="text-h6">
            Modules de la filière : {{ selectedFiliere.libelle }}
          </span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text>
          <v-row>
            <!-- Colonne des modules disponibles -->
            <v-col cols="12" md="6">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Modules disponibles</h3>

              <!-- <<--- NOUVEAU : Barre de recherche pour les modules --- -->
              <v-text-field
                  v-model="searchModule"
                  label="Rechercher un module..."
                  variant="outlined"
                  density="compact"
                  prepend-inner-icon="mdi-magnify"
                  hide-details
                  clearable
                  class="mb-3"
              ></v-text-field>

              <v-list density="compact" class="border rounded-lg" style="max-height: 340px; overflow-y: auto;">
                <v-list-item
                    v-for="module in modulesNonLies"
                    :key="`unlinked-${module.id}`"
                >
                  <v-list-item-title>{{ module.libelle }}</v-list-item-title>
                  <template v-slot:append>
                    <v-btn
                        icon="mdi-plus-circle-outline"
                        variant="text"
                        color="success"
                        size="small"
                        @click="lierModule(module.id)"
                        title="Lier ce module"
                    ></v-btn>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesNonLies.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucun module disponible trouvé.
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>

            <!-- Colonne des modules déjà liés (inchangée) -->
            <v-col cols="12" md="6">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Modules déjà liés</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <v-list-item
                    v-for="module in modulesLies"
                    :key="`linked-${module.id}`"
                >
                  <v-list-item-title>{{ module.libelle }}</v-list-item-title>
                  <template v-slot:append>
                    <v-btn
                        icon="mdi-minus-circle-outline"
                        variant="text"
                        color="error"
                        size="small"
                        @click="delierModule(module.id)"
                        title="Délier ce module"
                    ></v-btn>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesLies.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucun module lié.
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