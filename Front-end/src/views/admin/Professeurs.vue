<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";

// Import des stores et types nécessaires
import { useProfesseurStore } from "../../stores/professeurs.ts";
import { useDepartementsStore } from "../../stores/departements";
import { ProfesseurPayload, ProfesseurResponse } from "../../types/types.ts";  
// --- STORE INITIALIZATION ---
const professeurStore = useProfesseurStore();
const departementsStore = useDepartementsStore();

// Données principales (Professeurs)
const { professeurs, loading, error } = storeToRefs(professeurStore);
// Données pour la liste déroulante (Départements)
const { departements } = storeToRefs(departementsStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem = {
  id: 0,
  nom: '',
  prenom: '',
  statut: '',
  departementId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "nom", title: "Nom" },
  { key: "prenom", title: "Prénom" },
 // { key: "statut", title: "Statut" },
  { key: "departement.libelle", title: "Département" }, // Accès à la propriété imbriquée
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "nom", label: "Nom", type: "text", required: true },
  { key: "prenom", label: "Prénom", type: "text", required: true },
  { key: "statut", label: "Statut", type: "text", required: true },
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

const formTitle = computed(() => isEditing.value ? "Modifier le professeur" : "Ajouter un professeur");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  Promise.all([
    professeurStore.fetchProfesseurs(),
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

function openEditForm(item: ProfesseurResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    nom: item.nom,
    prenom: item.prenom,
    statut: item.statut,
    departementId: item.departement.id,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: ProfesseurResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
async function saveForm() {
  if (!editedItem.value.departementId) return;

  const payload: ProfesseurPayload = {
    nom: editedItem.value.nom,
    prenom: editedItem.value.prenom,
    statut: editedItem.value.statut,
    departementId: editedItem.value.departementId,
  };

  try {
    if (isEditing.value) {
      await professeurStore.updateProfesseur(editedItem.value.id, payload);
    } else {
      await professeurStore.addProfesseur(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await professeurStore.deleteProfesseur(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Professeurs</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des professeurs"
        :headers="headers"
        :items="professeurs"
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
        title="Supprimer le professeur"
        text="Êtes-vous sûr de vouloir supprimer ce professeur ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>