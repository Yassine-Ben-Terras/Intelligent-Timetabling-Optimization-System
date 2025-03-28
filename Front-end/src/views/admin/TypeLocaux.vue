<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";
import { useTypeLocalStore, type TypeLocalPayload } from "../../stores/typesLocaux.ts";
import { TypeLocal } from "../../types/types.ts"; 
// Initialisation du store
const typeLocalStore = useTypeLocalStore();

// Utilisation de storeToRefs pour la réactivité
const { typeLocaux, loading, error } = storeToRefs(typeLocalStore);

// State local pour la gestion de l'UI
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem: TypeLocal = { id: 0, libelle: '' };
const editedItem = ref<TypeLocal>({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// Configuration du tableau
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Type de Local" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

// Configuration des champs du formulaire
const formFields: FormField[] = [
  { key: "libelle", label: "Nom du type de local", type: "text", required: true },
];

// Titre dynamique du formulaire
const formTitle = computed(() => isEditing.value ? "Modifier le type de local" : "Ajouter un type de local");

// Chargement des données au montage
onMounted(() => {
  typeLocalStore.fetchTypeLocaux().catch(err => console.error("Erreur au montage:", err.message));
});

// Fonctions de gestion de l'UI
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: TypeLocal) {
  isEditing.value = true;
  editedItem.value = { ...item };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: TypeLocal) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// Fonctions d'interaction avec le store
async function saveForm() {
  try {
    if (isEditing.value) {
      await typeLocalStore.updateTypeLocal(editedItem.value);
    } else {
      const payload: TypeLocalPayload = { libelle: editedItem.value.libelle };
      await typeLocalStore.addTypeLocal(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await typeLocalStore.deleteTypeLocal(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Types de Locaux</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des types de locaux"
        :headers="headers"
        :items="typeLocaux"
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
        title="Supprimer le type de local"
        text="Êtes-vous sûr de vouloir supprimer ce type de local ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>