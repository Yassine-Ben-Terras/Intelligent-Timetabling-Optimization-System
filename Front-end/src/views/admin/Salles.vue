<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";

// Import des stores et types nécessaires
import { useLocalStore } from "../../stores/salle.ts";
import {  LocalResponse,  LocalPayload } from "../../types/types.ts";  
import { useTypeLocalStore } from "../../stores/typesLocaux.ts";

// --- STORE INITIALIZATION ---
const localStore = useLocalStore();
const typeLocalStore = useTypeLocalStore();

// Données principales (Locaux)
const { locaux, loading, error } = storeToRefs(localStore);
// Données pour la liste déroulante (Types de Locaux)
const { typeLocaux } = storeToRefs(typeLocalStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem = {
  id: 0,
  libelle: '',
  capacite: 0,
  typeId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Nom du Local" },
  { key: "capacite", title: "Capacité" },
  { key: "type.libelle", title: "Type" }, // Accès à la propriété imbriquée
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "libelle", label: "Nom du local", type: "text", required: true },
  { key: "capacite", label: "Capacité", type: "number", required: true },
  {
    key: "typeId",
    label: "Type de Local",
    type: "select",
    required: false, // Type de local n'est plus obligatoire
    items: typeLocaux.value, // La liste vient du store des types de locaux
    itemTitle: 'libelle',
    itemValue: 'id'
  },
]);

const formTitle = computed(() => isEditing.value ? "Modifier le local" : "Ajouter un local");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  // On charge les deux listes de données en parallèle
  Promise.all([
    localStore.fetchLocaux(),
    typeLocalStore.fetchTypeLocaux(),
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

function openEditForm(item: LocalResponse) {
  isEditing.value = true;
  // On mappe l'objet reçu à notre structure de formulaire
  editedItem.value = {
    id: item.id,
    libelle: item.libelle,
    capacite: item.capacite,
    typeId: item.type?.id || null, // Utiliser null si type est undefined
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: LocalResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
async function saveForm() {
  const payload: LocalPayload = {
    libelle: editedItem.value.libelle,
    capacite: Number(editedItem.value.capacite),
    typeId: editedItem.value.typeId, // typeId peut être null
  };

  try {
    if (isEditing.value) {
      await localStore.updateLocal(editedItem.value.id, payload);
    } else {
      await localStore.addLocal(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await localStore.deleteLocal(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des salles</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des locaux (salles, amphis...)"
        :headers="headers"
        :items="locaux"
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
        title="Supprimer le local"
        text="Êtes-vous sûr de vouloir supprimer ce local ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>