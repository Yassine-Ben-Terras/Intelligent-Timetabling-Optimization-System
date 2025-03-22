<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";

// Import des stores et types nécessaires
import { useFiliereStore  } from "../../stores/filieres";
import { useDepartementsStore } from "../../stores/departements";
import { FilierePayload, FiliereResponse } from "../../types/types.ts";
// --- STORE INITIALIZATION ---
const filiereStore = useFiliereStore();
const departementsStore = useDepartementsStore();

// Données principales (Filières)
const { filieres, loading, error } = storeToRefs(filiereStore);
// Données pour la liste déroulante (Départements)
const { departements } = storeToRefs(departementsStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem = {
  id: 0,
  libelle: '',
  codeFiliere: '',
  departementId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---
const headers: DataTableHeader[] = [
  //{ key: "codeFiliere", title: "Code" },
  { key: "libelle", title: "Libellé de la Filière" },
  { key: "departement.libelle", title: "Département" }, // Accès à la propriété imbriquée
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "codeFiliere", label: "Code de la filière", type: "text", required: true },
  { key: "libelle", label: "Libellé de la filière", type: "text", required: true },
  {
    key: "departementId",
    label: "Département",
    type: "select",
    required: true,
    items: departements.value, // La liste vient du store des départements
    itemTitle: 'libelle',
    itemValue: 'id'
  },
]);

const formTitle = computed(() => isEditing.value ? "Modifier la filière" : "Ajouter une filière");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  Promise.all([
    filiereStore.fetchFilieres(),
    departementsStore.fetchDepartements(),
  ]).catch(err => {
    console.error("Une erreur est survenue lors du chargement des données:", err);
  });
});

// --- UI HANDLERS ---
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: FiliereResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    libelle: item.libelle,
    codeFiliere: item.codeFiliere,
    departementId: item.departement.id,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: FiliereResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
async function saveForm() {
  if (!editedItem.value.departementId) return;

  const payload: FilierePayload = {
    libelle: editedItem.value.libelle,
    codeFiliere: editedItem.value.codeFiliere,
    departementId: editedItem.value.departementId,
  };

  try {
    if (isEditing.value) {
      await filiereStore.updateFiliere(editedItem.value.id, payload);
    } else {
      await filiereStore.addFiliere(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await filiereStore.deleteFiliere(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Filières</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des filières"
        :headers="headers"
        :items="filieres"
        :loading="loading"
        @add="openAddForm"
        @edit="openEditForm"
        @delete="openDeleteConfirm"
    />

    <v-dialog v-model="showFormDialog" max-width="600px" persistent>
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
        title="Supprimer la filière"
        text="Êtes-vous sûr de vouloir supprimer cette filière ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>