<!-- vue.txt -->
<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";

// Import des types
import type { DataTableHeader, FormField } from "../../types/types.ts";
import type { PlageTravailEnrichie, PlageTravailPayload } from "../../types/types.ts";

// Import des stores
import { usePlageTravailStore } from "../../stores/plagesTravail";
import { useJourStore } from "../../stores/jours";
import { useSessionStore } from "../../stores/sessions";

// --- INITIALISATION DES STORES ---
const plageTravailStore = usePlageTravailStore();
const jourStore = useJourStore();
const sessionStore = useSessionStore();

// --- DONNÉES REACTIVES ---
const { loading, error } = storeToRefs(plageTravailStore);
const { jours } = storeToRefs(jourStore);
const { sessions } = storeToRefs(sessionStore);

// On utilise le getter qui retourne les données enrichies et prêtes pour l'affichage
const plagesTravailAffichees = computed(() => plageTravailStore.plagesTravailEnrichies);


// --- ÉTAT DE L'UI ---
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

const defaultItem = {
  id: 0,
  heureDebut: '08:00',
  heureFin: '18:30',
  jourId: null as number | null,
  sessionId: null as number | null,
};
const editedItem = ref({ ...defaultItem });
const itemToDeleteId = ref<number | null>(null);

// --- CONFIGURATION DU COMPOSANT ---
const headers: DataTableHeader[] = [
  { key: "jour.libelle", title: "Jour" },
  { key: "heureDebut", title: "Début" },
  { key: "heureFin", title: "Fin" },
  { key: "session.libelle", title: "Session" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

const formFields = computed<FormField[]>(() => [
  { key: "jourId", label: "Jour", type: "select", required: true, items: jours.value, itemTitle: 'libelle', itemValue: 'id' },
  { key: "heureDebut", label: "Heure de début", type: "text", required: true, props: { placeholder: 'HH:MM' } },
  { key: "heureFin", label: "Heure de fin", type: "text", required: true, props: { placeholder: 'HH:MM' } },
  { key: "sessionId", label: "Session", type: "select", required: true, items: sessions.value, itemTitle: 'libelle', itemValue: 'id' },
]);

const formTitle = computed(() => isEditing.value ? "Modifier la plage de travail" : "Ajouter une plage de travail");

// --- CYCLE DE VIE ---
onMounted(() => {
  Promise.all([
    jourStore.fetchJours(),
    sessionStore.fetchSessions(),
  ]).then(() => {
    // On charge les plages de travail seulement après que les dépendances sont là
    plageTravailStore.fetchPlagesTravail();
  }).catch(err => {
    console.error("Une erreur est survenue lors du chargement des données:", err);
  });
});

// --- GESTIONNAIRES D'ÉVÉNEMENTS UI ---
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

// L'item reçu est bien de type `PlageTravailEnrichie`
function openEditForm(item: PlageTravailEnrichie) {
  isEditing.value = true;
  editedItem.value = {
    id: item.id,
    heureDebut: item.heureDebut,
    heureFin: item.heureFin,
    // On accède aux IDs via les objets imbriqués
    jourId: item.jour.id,
    sessionId: item.session.id,
  };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: PlageTravailEnrichie) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() { showFormDialog.value = false; }
function closeConfirmDialog() { showConfirmDialog.value = false; itemToDeleteId.value = null; }

// --- ACTIONS API ---
async function saveForm() {
  if (!editedItem.value.jourId || !editedItem.value.sessionId) return;

  const payload: PlageTravailPayload = {
    heureDebut: editedItem.value.heureDebut,
    heureFin: editedItem.value.heureFin,
    jourId: editedItem.value.jourId,
    sessionId: editedItem.value.sessionId,
  };

  try {
    if (isEditing.value) {
      await plageTravailStore.updatePlageTravail(editedItem.value.id, payload);
    } else {
      await plageTravailStore.addPlageTravail(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await plageTravailStore.deletePlageTravail(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Plages de Travail</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <DataTable
        title="Liste des plages de travail"
        :headers="headers"
        :items="plagesTravailAffichees"
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
        title="Supprimer la plage de travail"
        text="Êtes-vous sûr de vouloir supprimer cette plage ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>