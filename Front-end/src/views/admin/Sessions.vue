<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";
import { useSessionStore } from "../../stores/sessions.ts";
import { SessionPayload,SessionResponse } from "../../types/types.ts";  
// --- 1. STORES ---
const sessionStore = useSessionStore();

// --- 2. STATE ---
// ON RÉCUPÈRE "sessions" DEPUIS sessionStore
const { sessions, loading, error } = storeToRefs(sessionStore);

// State de l'UI
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// State du formulaire
const defaultItem = { id: 0, libelle: '' };
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- 3. CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Session" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "libelle", label: "Nom de la session", type: "text", required: true },
]);

const formTitle = computed(() => isEditing.value ? "Modifier la session" : "Ajouter une session");

// --- 4. LOGIQUE ---
onMounted(() => {
  sessionStore.fetchSessions();
});




function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: SessionResponse) {
  isEditing.value = true;
  editedItem.value = { id: item.id, libelle: item.libelle };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: SessionResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

async function saveForm() {
  const payload: SessionPayload = { libelle: editedItem.value.libelle };
  await (isEditing.value ? sessionStore.updateSession(editedItem.value.id, payload) : sessionStore.addSession(payload));
  closeFormDialog();
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    await sessionStore.deleteSession(itemToDeleteId.value);
    closeConfirmDialog();
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Sessions</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des sessions"
        :headers="headers"
        :items="sessions"
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
        title="Supprimer la session"
        text="Êtes-vous sûr de vouloir supprimer cette session ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>