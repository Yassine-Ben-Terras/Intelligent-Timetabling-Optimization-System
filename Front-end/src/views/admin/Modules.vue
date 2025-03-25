<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField, ModuleResponse, ModulePayload } from "../../types/types.ts";

// --- STORE INITIALIZATION ---
import { useModuleStore } from "../../stores/modules.ts";
import { useSemestreStore } from "../../stores/semestres";
// NOUVEAU : Import du store pour les types de locaux
import { useTypeLocalStore } from "../../stores/typesLocaux.ts";

const moduleStore = useModuleStore();
const semestreStore = useSemestreStore();
// NOUVEAU : Initialisation du store
const typeLocalStore = useTypeLocalStore();

// Données principales (Modules) et des dépendances
const { modules, loading, error } = storeToRefs(moduleStore);
const { semestres } = storeToRefs(semestreStore);
// NOUVEAU : Récupération des données du store avec le nom correct de la state `typeLocaux`
const { typeLocaux } = storeToRefs(typeLocalStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// MODIFIÉ : Objet par défaut pour le formulaire avec les nouveaux champs
const defaultItem = {
  id: 0,
  libelle: '',
  heuresCours: 0,
  heuresTD: 0,
  heuresTP: 0,
  semDemTP: '',
  semDemTD: '',
  semestreId: null as number | null,
  // NOUVEAU : Initialisation des nouveaux champs à null
  typeLocalRequisCoursId: null as number | null,
  typeLocalRequisTDId: null as number | null,
  typeLocalRequisTPId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---

// MODIFIÉ : Configuration des en-têtes du tableau de données pour inclure les locaux
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Module" },
  { key: "heuresCours", title: "Durée Cours(min)" },
  { key: "heuresTD", title: "Durée TD(min)" },
  { key: "heuresTP", title: "Durée TP(min)" },
  // NOUVEAU : Colonnes pour les locaux requis. Vuetify gère l'accès aux objets imbriqués.
  { key: "typeLocalRequisCours.libelle", title: "Local Cours" },
  { key: "typeLocalRequisTD.libelle", title: "Local TD" },
  { key: "typeLocalRequisTP.libelle", title: "Local TP" },
  { key: "semestre.libelle", title: "Semestre" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

// MODIFIÉ : Configuration des champs du formulaire, transformée en `computed`
// pour que la liste des `items` (typeLocaux) soit réactive après son chargement.
const formFields = computed<FormField[]>(() => [
  { key: "libelle", label: "Nom du module", type: "text", required: true },
  { key: "heuresCours", label: "Durée Cours(min)", type: "number", required: false },
  { key: "heuresTD", label: "Durée TD(min)", type: "number", required: false },
  { key: "heuresTP", label: "Durée TP(min)", type: "number", required: false },
  {
    key: "semestreId",
    label: "Semestre",
    type: "select",
    required: true,
    items: semestres.value,
    itemTitle: 'libelle',
    itemValue: 'id'
  },
  // NOUVEAU : Champs de sélection pour les types de locaux
  {
    key: "typeLocalRequisCoursId",
    label: "Type de local requis (Cours)",
    type: "select",
    required: false, // Ils sont nullable, donc pas obligatoires
    items: typeLocaux.value, // Utilise la valeur réactive du store
    itemTitle: 'libelle',
    itemValue: 'id'
  },
  {
    key: "typeLocalRequisTDId",
    label: "Type de local requis (TD)",
    type: "select",
    required: false,
    items: typeLocaux.value,
    itemTitle: 'libelle',
    itemValue: 'id'
  },
  {
    key: "typeLocalRequisTPId",
    label: "Type de local requis (TP)",
    type: "select",
    required: false,
    items: typeLocaux.value,
    itemTitle: 'libelle',
    itemValue: 'id'
  },
]);

const formTitle = computed(() => isEditing.value ? "Modifier le module" : "Ajouter un module");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  // MODIFIÉ : Chargement de toutes les données nécessaires en parallèle
  Promise.all([
    moduleStore.fetchModules(),
    semestreStore.fetchSemestres(),
    typeLocalStore.fetchTypeLocaux(), // NOUVEAU : Chargement des types de locaux
  ]).catch(err => {
    // Affiche une erreur générique si l'un des appels échoue
    console.error("Une erreur est survenue lors du chargement des données initiales:", err);
  });
});

// --- UI HANDLERS ---
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

// MODIFIÉ : La fonction d'édition remplit les nouveaux champs
function openEditForm(item: ModuleResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    libelle: item.libelle,
    heuresCours: item.heuresCours,
    heuresTD: item.heuresTD,
    heuresTP: item.heuresTP,
    semDemTD: item.semDemTD,
    semDemTP: item.semDemTP,
    semestreId: item.semestre.id,
    // NOUVEAU : Utilise l'optional chaining `?.` pour éviter les erreurs si l'objet est null
    // et l'opérateur de coalescence nulle `??` pour garantir une valeur `null` en cas d'absence.
    typeLocalRequisCoursId: item.typeLocalRequisCours?.id ?? null,
    typeLocalRequisTDId: item.typeLocalRequisTD?.id ?? null,
    typeLocalRequisTPId: item.typeLocalRequisTP?.id ?? null,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: ModuleResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
// MODIFIÉ : La fonction de sauvegarde construit le payload avec les nouveaux champs
async function saveForm() {
  if (!editedItem.value.semestreId) {
    console.error("L'ID du semestre est requis.");
    return;
  }

  const payload: ModulePayload = {
    libelle: editedItem.value.libelle,
    heuresCours: Number(editedItem.value.heuresCours) || 0,
    heuresTD: Number(editedItem.value.heuresTD) || 0,
    heuresTP: Number(editedItem.value.heuresTP) || 0,
    semDemTD: editedItem.value.semDemTD,
    semDemTP: editedItem.value.semDemTP,
    semestreId: editedItem.value.semestreId,
    // NOUVEAU : Ajout des IDs au payload. `|| null` assure que `undefined` ou `0` (souvent retourné par les selects vides) devient `null`.
    typeLocalRequisCoursId: editedItem.value.typeLocalRequisCoursId || null,
    typeLocalRequisTDId: editedItem.value.typeLocalRequisTDId || null,
    typeLocalRequisTPId: editedItem.value.typeLocalRequisTPId || null,
  };

  try {
    if (isEditing.value) {
      await moduleStore.updateModule(editedItem.value.id, payload);
    } else {
      await moduleStore.addModule(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde du module:", err);
    // Optionnel: afficher une notification d'erreur à l'utilisateur
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await moduleStore.deleteModule(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression du module:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Modules</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des modules"
        :headers="headers"
        :items="modules"
        :loading="loading"
        @add="openAddForm"
        @edit="openEditForm"
        @delete="openDeleteConfirm"
    />

    <v-dialog v-model="showFormDialog" max-width="600px" persistent>
      <!-- Le v-model et les props sont maintenant correctement liés aux données réactives -->
      <EntityForm
          v-model="editedItem"
          :fields="formFields"
          :title="formTitle"
          :is-edit="isEditing"
          @submit="saveForm"
          @cancel="closeFormDialog"
      />
    </v-dialog>

    <ConfirmDialog
        v-model="showConfirmDialog"
        title="Supprimer le module"
        text="Êtes-vous sûr de vouloir supprimer ce module ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>