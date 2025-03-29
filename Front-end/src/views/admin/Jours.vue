<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField, Jour } from "../../types/types.ts";
import { useJourStore, type JourPayload } from "../../stores/jours";
const jourStore = useJourStore();
const { jours, loading, error } = storeToRefs(jourStore);

const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

const defaultItem: Jour = { id: 0, libelle: '' };
const editedItem = ref<Jour>({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

const headers: DataTableHeader[] = [
  { key: "libelle", title: "Jour" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields: FormField[] = [
  { key: "libelle", label: "Nom du jour", type: "text", required: true },
];

const formTitle = computed(() => isEditing.value ? "Modifier le jour" : "Ajouter un jour");

onMounted(() => {
  jourStore.fetchJours().catch(err => console.error("Erreur au montage:", err.message));
});

function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: Jour) {
  isEditing.value = true;
  editedItem.value = { ...item };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: Jour) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

async function saveForm() {
  try {
    if (isEditing.value) {
      await jourStore.updateJour(editedItem.value);
    } else {
      const payload: JourPayload = { libelle: editedItem.value.libelle };
      await jourStore.addJour(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await jourStore.deleteJour(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Jours</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des jours"
        :headers="headers"
        :items="jours"
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
        title="Supprimer le jour"
        text="Êtes-vous sûr de vouloir supprimer ce jour ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>