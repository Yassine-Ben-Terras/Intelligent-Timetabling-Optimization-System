<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, ModuleResponse, GroupeResponse, GroupePayload } from "../../types/types.ts";

// Import des stores
import { useGroupeStore } from "../../stores/groupes.ts";
import { useModuleStore } from "../../stores/modules";

// --- INITIALISATION DES STORES ---
const groupeStore = useGroupeStore();
const moduleStore = useModuleStore();

// --- STATE REACTIF ---
const { groupes, loading, error } = storeToRefs(groupeStore);
const { modules } = storeToRefs(moduleStore);

// --- STATE DE L'INTERFACE ---
const showManageDialog = ref(false);
const showConfirmDialog = ref(false);
const itemToDeleteId = ref<number | null>(null);
const selectedGroupe = ref<GroupeResponse | null>(null);
const searchGroupe = ref(''); // ✅ RECHERCHE : Le state est déjà là
const searchModule = ref('');
const editedModuleIds = ref<number[]>([]);

// --- NOUVEAU : State pour le dialogue d'ajout ---
const showAddDialog = ref(false);
const newGroupeItem = ref<Omit<GroupePayload, 'nbrEtudiants'> & { nbrEtudiants: number | string }>({
  libelle: '',
  nbrEtudiants: '',
  moduleIds: [],
});
const isFormValid = ref(false); // Pour la validation du formulaire d'ajout

// --- PROPRIÉTÉS CALCULÉES ---
const tableLoading = computed(() => loading.value || moduleStore.loading);

const modulesLies = computed<ModuleResponse[]>(() => {
  if (!selectedGroupe.value) return [];
  return modules.value.filter(module => editedModuleIds.value.includes(module.id));
});

const modulesNonLies = computed<ModuleResponse[]>(() => {
  if (!selectedGroupe.value) return [];
  let resultat = modules.value.filter(module => !editedModuleIds.value.includes(module.id));
  if (searchModule.value) {
    resultat = resultat.filter(module =>
      module.libelle.toLowerCase().includes(searchModule.value.toLowerCase())
    );
  }
  return resultat;
});

// --- CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Groupe" },
  { key: "nbrEtudiants", title: "Nombre d'étudiants" },
  { key: "modules", title: "Modules Associés", sortable: false },
  { key: "actions", title: "Actions", sortable: false, align: 'end', width: '280px' },
];
// Règles de validation simples pour le formulaire d'ajout
const rules = {
  required: (v: any) => !!v || 'Ce champ est requis',
};

// --- CYCLE DE VIE ---
onMounted(() => {
  groupeStore.fetchGroupes();
  moduleStore.fetchModules();
});

// --- GESTIONNAIRES ---
function openManageDialog(groupe: GroupeResponse) {
  selectedGroupe.value = groupe;
  editedModuleIds.value = groupe.modules.map(m => m.id);
  searchModule.value = '';
  showManageDialog.value = true;
}

function closeManageDialog() {
  showManageDialog.value = false;
  selectedGroupe.value = null;
  editedModuleIds.value = [];
}

function openDeleteConfirm(item: GroupeResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeConfirmDialog() {
  showConfirmDialog.value = false;
  itemToDeleteId.value = null;
}

function lierModule(moduleId: number) { editedModuleIds.value.push(moduleId); }
function delierModule(moduleId: number) { editedModuleIds.value = editedModuleIds.value.filter(id => id !== moduleId); }

// --- NOUVEAU : Logique pour le dialogue d'ajout ---
function openAddDialog() {
  newGroupeItem.value = { libelle: '', nbrEtudiants: '', moduleIds: [] };
  showAddDialog.value = true;
}
function closeAddDialog() {
  showAddDialog.value = false;
}

// --- ACTIONS API ---
async function enregistrerChangements() {
  if (!selectedGroupe.value) return;
  const payload: GroupePayload = {
    libelle: selectedGroupe.value.libelle,
    nbrEtudiants: selectedGroupe.value.nbrEtudiants,
    moduleIds: editedModuleIds.value,
  };
  await groupeStore.updateGroupe(selectedGroupe.value.id, payload);
  closeManageDialog();
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    await groupeStore.deleteGroupe(itemToDeleteId.value);
    closeConfirmDialog();
  }
}

// --- NOUVEAU : Action pour sauvegarder un nouveau groupe ---
async function saveNewGroupe() {
  if (!isFormValid.value) return; // Ne soumet pas si le formulaire n'est pas valide
  const payload: GroupePayload = {
    ...newGroupeItem.value,
    nbrEtudiants: Number(newGroupeItem.value.nbrEtudiants),
  };
  await groupeStore.addGroupe(payload);
  closeAddDialog();
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Groupes</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <v-card variant="outlined">
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">Liste des groupes</span>
        <v-spacer></v-spacer>
        <!-- ✅ RECHERCHE : Le champ de recherche est bien là -->
        <v-text-field
            v-model="searchGroupe"
            label="Rechercher un groupe..."
            variant="outlined" density="compact" prepend-inner-icon="mdi-magnify"
            hide-details single-line style="max-width: 300px;" class="mr-2"
        ></v-text-field>
        <!-- ✅ AJOUTER : Le bouton pour ouvrir le dialogue d'ajout -->
        <v-btn color="primary" @click="openAddDialog">
          <v-icon start>mdi-plus</v-icon>
          Ajouter
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>

      <!-- ✅ RECHERCHE : La prop :search est liée à notre ref -->
      <v-data-table
        :headers="headers"
        :items="groupes"
        :loading="tableLoading"
        :search="searchGroupe"
        hover density="compact"
      >
        <template v-slot:item.modules="{ item }">
          <div class="d-flex flex-wrap ga-2 py-2" style="max-width: 450px;">
            <v-chip v-if="item.modules.length === 0" size="small" label color="grey">Aucun module</v-chip>
            <v-chip v-else v-for="module in item.modules" :key="module.id" size="small" label color="primary" variant="tonal">
              {{ module.libelle }}
            </v-chip>
          </div>
        </template>

        <template v-slot:item.actions="{ item }">
          <v-btn color="primary" variant="tonal" size="small" class="mr-2" @click="openManageDialog(item)">
            <v-icon start>mdi-link-variant</v-icon>
            Gérer les modules
          </v-btn>
          <v-btn icon="mdi-delete-outline" variant="text" color="error" size="small" @click="openDeleteConfirm(item)" title="Supprimer ce groupe"/>
        </template>
      </v-data-table>
    </v-card>

    <!-- Dialogue de GESTION des modules (inchangé) -->
    <v-dialog v-model="showManageDialog" max-width="900px" persistent scrollable>
       <!-- ... le code de ce dialogue reste exactement le même ... -->
       <v-card v-if="selectedGroupe">
        <v-card-title class="d-flex align-center pa-4">
          <v-icon start color="primary">mdi-layers-triple-outline</v-icon>
          <span class="text-h6">Modules du groupe : {{ selectedGroupe.libelle }}</span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Modules disponibles</h3>
              <v-text-field v-model="searchModule" label="Rechercher un module..." variant="outlined" density="compact" prepend-inner-icon="mdi-magnify" hide-details clearable class="mb-3"/>
              <v-list density="compact" class="border rounded-lg" style="max-height: 340px; overflow-y: auto;">
                <v-list-item v-for="module in modulesNonLies" :key="`unlinked-${module.id}`">
                  <v-list-item-title>{{ module.libelle }}</v-list-item-title>
                  <template v-slot:append>
                    <v-btn icon="mdi-plus-circle-outline" variant="text" color="success" size="small" @click="lierModule(module.id)" title="Lier ce module"/>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesNonLies.length === 0"><v-list-item-title class="text-center text-medium-emphasis py-4">Aucun module disponible.</v-list-item-title></v-list-item>
              </v-list>
            </v-col>
            <v-col cols="12" md="6">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Modules déjà liés</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <v-list-item v-for="module in modulesLies" :key="`linked-${module.id}`">
                  <v-list-item-title>{{ module.libelle }}</v-list-item-title>
                  <template v-slot:append>
                    <v-btn icon="mdi-minus-circle-outline" variant="text" color="error" size="small" @click="delierModule(module.id)" title="Délier ce module"/>
                  </template>
                </v-list-item>
                <v-list-item v-if="modulesLies.length === 0"><v-list-item-title class="text-center text-medium-emphasis py-4">Aucun module lié.</v-list-item-title></v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn @click="closeManageDialog">Annuler</v-btn>
          <v-btn color="primary" variant="elevated" @click="enregistrerChangements" :loading="loading">Enregistrer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- ✅ NOUVEAU : Dialogue pour AJOUTER un groupe -->
    <v-dialog v-model="showAddDialog" max-width="600px" persistent>
      <v-card>
        <v-form v-model="isFormValid" @submit.prevent="saveNewGroupe">
          <v-card-title>
            <span class="text-h5">Ajouter un nouveau groupe</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="newGroupeItem.libelle"
                    label="Nom du groupe *"
                    :rules="[rules.required]"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="newGroupeItem.nbrEtudiants"
                    label="Nombre d'étudiants *"
                    type="number"
                    :rules="[rules.required]"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                   <v-autocomplete
                      v-model="newGroupeItem.moduleIds"
                      :items="modules"
                      item-title="libelle"
                      item-value="id"
                      label="Modules initiaux (optionnel)"
                      multiple
                      chips
                      closable-chips
                    ></v-autocomplete>
                </v-col>
              </v-row>
            </v-container>
             <small>* champ requis</small>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue-darken-1" variant="text" @click="closeAddDialog">Annuler</v-btn>
            <v-btn color="blue-darken-1" variant="text" type="submit" :disabled="!isFormValid">Sauvegarder</v-btn>
          </v-card-actions>
        </v-form>
      </v-card>
    </v-dialog>

    <!-- Dialogue de confirmation de suppression (inchangé) -->
    <ConfirmDialog
        v-model="showConfirmDialog"
        title="Supprimer le groupe"
        text="Êtes-vous sûr de vouloir supprimer ce groupe ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>