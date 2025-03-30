<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader, FormField } from "../../types/types.ts";

// Import des stores et types nécessaires
import { useCreneauStore } from "../../stores/creneaux.ts";
import { useJourStore } from "../../stores/jours";
import { useSessionStore } from "../../stores/sessions";
import {  CreneauResponse,  CreneauPayload } from "../../types/types.ts";

// --- STORE INITIALIZATION ---
const creneauStore = useCreneauStore();
const jourStore = useJourStore();
const sessionStore = useSessionStore();
//
// Données principales et des dépendances
const { creneaux, loading, error } = storeToRefs(creneauStore);
const { jours } = storeToRefs(jourStore);
const { sessions } = storeToRefs(sessionStore);

// --- UI STATE ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour le formulaire
const defaultItem = {
  id: 0,
  heureDebut: '',
  heureFin: '',
  nbrHeures: 1.5,
  jourId: null as number | null,
  sessionId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- COMPONENT CONFIGURATION ---
const headers: DataTableHeader[] = [
  { key: "heureDebut", title: "Début" },
  { key: "heureFin", title: "Fin" },
  //{ key: "nbrHeures", title: "Durée (h)" },
  { key: "jour.libelle", title: "Jour" },
  { key: "session.libelle", title: "Session" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "heureDebut", label: "Heure de début", type: "text", required: true, props: { placeholder: 'HH:MM' } },
  { key: "heureFin", label: "Heure de fin", type: "text", required: true, props: { placeholder: 'HH:MM' } },
  { key: "nbrHeures", label: "Nombre d'heures", type: "number", required: true, props: { step: 0.5 } },
  { key: "jourId", label: "Jour", type: "select", required: true, items: jours.value, itemTitle: 'libelle', itemValue: 'id' },
  { key: "sessionId", label: "Session", type: "select", required: true, items: sessions.value, itemTitle: 'libelle', itemValue: 'id' },
]);

const formTitle = computed(() => isEditing.value ? "Modifier le créneau" : "Ajouter un créneau");

// --- LIFECYCLE HOOKS ---
onMounted(() => {
  Promise.all([
    creneauStore.fetchCreneaux(),
    jourStore.fetchJours(),
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

function openEditForm(item: CreneauResponse) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    heureDebut: item.heureDebut,
    heureFin: item.heureFin,
    nbrHeures: item.nbrHeures,
    jourId: item.jour.id,
    sessionId: item.session.id,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: CreneauResponse) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- API ACTIONS ---
async function saveForm() {
  if (!editedItem.value.jourId || !editedItem.value.sessionId) return;

  const payload: CreneauPayload = {
    heureDebut: editedItem.value.heureDebut,
    heureFin: editedItem.value.heureFin,
    nbrHeures: Number(editedItem.value.nbrHeures),
    jourId: editedItem.value.jourId,
    sessionId: editedItem.value.sessionId,
  };

  try {
    if (isEditing.value) {
      await creneauStore.updateCreneau(editedItem.value.id, payload);
    } else {
      await creneauStore.addCreneau(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await creneauStore.deleteCreneau(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Créneaux</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des créneaux"
        :headers="headers"
        :items="creneaux"
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
        title="Supprimer le créneau"
        text="Êtes-vous sûr de vouloir supprimer ce créneau ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>