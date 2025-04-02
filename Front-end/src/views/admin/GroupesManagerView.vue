<script setup lang="ts">
import { ref, onMounted, computed, reactive } from "vue";
import { storeToRefs } from "pinia";

// --- IMPORTS OBLIGATOIRES ---
// 1. Importer les types nécessaires
import type { DataTableHeader, FormField, GroupeResponse, GroupePayload, GrpParamDto, GrpParamPayload } from "../../types/types.ts";

// 2. Importer les composants enfants utilisés dans le template
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";

// 3. Importer tous les stores Pinia nécessaires
import { useGroupeStore } from "../../stores/groupes.ts";
import { useGrpParamStore } from "../../stores/grpParams.ts";
import { useModuleStore } from "../../stores/modules";
import { useSectionStore } from "../../stores/sections";
import { useSemestreStore } from "../../stores/semestres";

// --- INTERFACE POUR LE FORMULAIRE ---
// Définit une structure claire pour les données du formulaire d'édition/création de groupe
interface GroupeFormData {
  id?: number;
  libelle: string;
  nbrEtudiants: number;
  moduleIds: number[];
}

// --- INITIALISATION DES STORES ---
const groupeStore = useGroupeStore();
const grpParamStore = useGrpParamStore();
const moduleStore = useModuleStore();
const sectionStore = useSectionStore();
const semestreStore = useSemestreStore();

// --- ETAT REACTIF (STATE) ---
// Données pour la vue "maître" (liste des groupes)
const { groupes, loading: loadingGroupes, error: errorGroupes } = storeToRefs(groupeStore);
const { modules } = storeToRefs(moduleStore);

// Données pour la vue "détail" (listes pour les formulaires de paramétrage)
const { liaisons, loading: loadingParams, error: errorParams } = storeToRefs(grpParamStore);
const { sections } = storeToRefs(sectionStore);
const { semestres } = storeToRefs(semestreStore);

// Etat pour gérer les boîtes de dialogue et les formulaires
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);
const editedItem = ref<GroupeFormData>({ libelle: '', nbrEtudiants: 0, moduleIds: [] });
const itemToDeleteId = ref<number | null>(null);

// Stocke les données des formulaires de paramétrage (un formulaire par groupe)
const newParams = reactive<Record<number, Partial<GrpParamPayload>>>({});

// --- CONFIGURATION DU COMPOSANT ---
const headers: DataTableHeader[] = [
  { key: 'data-table-expand', title: '' }, // Colonne pour l'icône "développer"
  { key: "libelle", title: "Groupe" },
  { key: "nbrEtudiants", title: "Nbr. Étudiants" },
  { key: "modules", title: "Modules" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "libelle", label: "Nom du groupe", type: "text", required: true },
  { key: "nbrEtudiants", label: "Nombre d'étudiants", type: "number", required: true },
  {
    key: "moduleIds", label: "Modules", type: "select", required: true,
    items: modules.value, itemTitle: 'libelle', itemValue: 'id', props: { multiple: true }
  },
]);

const formTitle = computed(() => isEditing.value ? "Modifier le groupe" : "Ajouter un groupe");

// --- CYCLE DE VIE ---
onMounted(() => {
  // Charge toutes les données nécessaires au démarrage du composant
  Promise.all([
    groupeStore.fetchGroupes(),
    grpParamStore.fetchGrpParams(),
    moduleStore.fetchModules(),
    sectionStore.fetchSections(),
    semestreStore.fetchSemestres(),
  ]).catch(err => {
    console.error("Erreur lors du chargement des données initiales:", err);
  });
});

// --- METHODES POUR LES GROUPES (CRUD principal) ---
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { libelle: '', nbrEtudiants: 0, moduleIds: [] };
  showFormDialog.value = true;
}

function openEditForm(item: GroupeResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    libelle: item.libelle,
    nbrEtudiants: item.nbrEtudiants,
    moduleIds: item.modules ? item.modules.map(m => m.id) : [],
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: GroupeResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

async function saveGroupe() {
  if (!editedItem.value.libelle || editedItem.value.nbrEtudiants === null || !editedItem.value.moduleIds?.length) return;

  const payload: GroupePayload = {
    libelle: editedItem.value.libelle,
    nbrEtudiants: Number(editedItem.value.nbrEtudiants),
    moduleIds: editedItem.value.moduleIds,
  };

  try {
    if (isEditing.value && editedItem.value.id) {
      await groupeStore.updateGroupe(editedItem.value.id, payload);
    } else {
      await groupeStore.addGroupe(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde du groupe:", err);
  }
}

async function confirmDeleteGroupe() {
  if (itemToDeleteId.value !== null) {
    try {
      await groupeStore.deleteGroupe(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression du groupe:", err);
    }
  }
}

// --- METHODES POUR LES PARAMETRAGES (affectations dans la vue détaillée) ---
const liaisonsParGroupe = computed(() => {
    return (groupeId: number) => liaisons.value.filter(l => l.groupeId === groupeId);
});

function getOrCreateNewParam(groupeId: number): Partial<GrpParamPayload> {
    if (!newParams[groupeId]) {
        newParams[groupeId] = {};
    }
    return newParams[groupeId];
}

async function ajouterParametrage(groupeId: number) {
    const newParam = newParams[groupeId];
    if (newParam && newParam.sectionId && newParam.semestreId) {
        const payload: GrpParamPayload = {
            groupeId: groupeId,
            sectionId: newParam.sectionId,
            semestreId: newParam.semestreId,
        };
        try {
            await grpParamStore.addGrpParam(payload);
            delete newParams[groupeId]; // Réinitialise le formulaire pour ce groupe
        } catch(e) {
            console.error("Erreur lors de l'ajout du paramétrage", e);
        }
    } else {
        console.warn("Veuillez remplir tous les champs pour le paramétrage.");
    }
}

async function supprimerParametrage(item: GrpParamDto) {
    const payload: GrpParamPayload = {
      sectionId: item.sectionId,
      groupeId: item.groupeId,
      semestreId: item.semestreId
    };
    await grpParamStore.deleteGrpParam(payload);
}
</script>


<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Groupes et Paramétrages</h1>
    <v-alert v-if="errorGroupes || errorParams" type="error" closable class="mb-4">
      {{ errorGroupes || errorParams }}
    </v-alert>

    <v-card variant="outlined">
      <v-card-title class="d-flex align-center">
        Liste des groupes
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="openAddForm" prepend-icon="mdi-plus">
          Ajouter un groupe
        </v-btn>
      </v-card-title>

      <v-data-table
          :headers="headers"
          :items="groupes"
          :loading="loadingGroupes"
          item-value="id"
          expand-on-click
      >
        <!-- Actions sur la ligne du groupe (Modifier / Supprimer) -->
        <template v-slot:item.actions="{ item }">
          <v-btn icon="mdi-pencil-outline" variant="text" color="grey-darken-1" size="small" @click.stop="openEditForm(item)"></v-btn>
          <v-btn icon="mdi-delete-outline" variant="text" color="error" size="small" @click.stop="openDeleteConfirm(item)"></v-btn>
        </template>

        <!-- Vue développée qui contient TOUTE la logique d'affectation (votre 2ème vue) -->
        <template v-slot:expanded-row="{ columns, item }">
          <tr>
            <td :colspan="columns.length" class="pa-4 bg-grey-lighten-5">
              <h3 class="text-h6 font-weight-medium mb-4">Affectations pour le groupe "{{ item.libelle }}"</h3>

              <!-- Formulaire pour créer une nouvelle affectation -->
              <v-form @submit.prevent="ajouterParametrage(item.id)">
                <v-row>
                  <v-col cols="12" md="4" sm="6">
                    <v-select v-model="getOrCreateNewParam(item.id).sectionId" :items="sections" item-title="libelle" item-value="id" label="Section" variant="underlined" density="compact" clearable></v-select>
                  </v-col>
                  <v-col cols="12" md="4" sm="6">
                    <v-select v-model="getOrCreateNewParam(item.id).semestreId" :items="semestres" item-title="libelle" item-value="id" label="Semestre" variant="underlined" density="compact" clearable></v-select>
                  </v-col>
                  <v-col cols="12" md="4" sm="12" class="d-flex align-center">
                    <v-btn type="submit" color="primary" variant="tonal" block :loading="loadingParams">
                      <v-icon start>mdi-link-plus</v-icon> Associer
                    </v-btn>
                  </v-col>
                </v-row>
              </v-form>
              <v-divider class="my-4"></v-divider>

              <!-- Liste des affectations existantes -->
              <div v-if="liaisonsParGroupe(item.id).length > 0">
                <v-chip
                    v-for="liaison in liaisonsParGroupe(item.id)"
                    :key="liaison.key"
                    class="ma-1"
                    closable
                    color="grey-lighten-1"
                    variant="flat"
                    @click:close="supprimerParametrage(liaison)"
                >
                  <v-icon start>mdi-tag-outline</v-icon>
                  {{ liaison.sectionLibelle }} / {{ liaison.semestreLibelle }}
                </v-chip>
              </div>
              <p v-else class="text-grey text-center py-2">Aucune affectation pour ce groupe.</p>
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-card>

    <!-- Boîte de dialogue pour Ajouter/Modifier un Groupe -->
    <v-dialog v-model="showFormDialog" max-width="600px" persistent>
      <EntityForm
          v-model="editedItem"
          :fields="formFields"
          :title="formTitle"
          :is-edit="isEditing"
          @submit="saveGroupe"
          @cancel="closeFormDialog"
      />
    </v-dialog>

    <!-- Boîte de dialogue de Confirmation de Suppression -->
    <ConfirmDialog
        v-model="showConfirmDialog"
        title="Supprimer le groupe"
        text="Êtes-vous sûr de vouloir supprimer ce groupe ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDeleteGroupe"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>