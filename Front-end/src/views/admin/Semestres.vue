<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";

// Import des stores et types nécessaires
import { useSemestreStore } from "../../stores/semestres.ts";
import { useSessionStore } from "../../stores/sessions";
import {  SemestreResponse,  SemestrePayload } from "../../types/types.ts";
// --- STORE INITIALIZATION ---
const semestreStore = useSemestreStore();
const sessionStore = useSessionStore();

// Données principales (Semestres)
const { semestres, loading, error } = storeToRefs(semestreStore);
// Données pour la liste déroulante (Sessions)
const { sessions } = storeToRefs(sessionStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem = {
  id: 0,
  libelle: '',
  sessionId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Semestre" },
  { key: "session.libelle", title: "Session" }, // Accès à la propriété imbriquée
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "libelle", label: "Nom du semestre", type: "text", required: true },
  {
    key: "sessionId",
    label: "Session",
    type: "select",
    required: true,
    items: sessions.value, // La liste vient du store des sessions
    itemTitle: 'libelle',
    itemValue: 'id'
  },
]);

const formTitle = computed(() => isEditing.value ? "Modifier le semestre" : "Ajouter un semestre");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  Promise.all([
    semestreStore.fetchSemestres(),
    sessionStore.fetchSessions(),
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

function openEditForm(item: SemestreResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    libelle: item.libelle,
    sessionId: item.session.id,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: SemestreResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
async function saveForm() {
  if (!editedItem.value.sessionId) return;

  const payload: SemestrePayload = {
    libelle: editedItem.value.libelle,
    sessionId: editedItem.value.sessionId,
  };

  try {
    if (isEditing.value) {
      await semestreStore.updateSemestre(editedItem.value.id, payload);
    } else {
      await semestreStore.addSemestre(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await semestreStore.deleteSemestre(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Semestres</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des semestres"
        :headers="headers"
        :items="semestres"
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
        title="Supprimer le semestre"
        text="Êtes-vous sûr de vouloir supprimer ce semestre ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>