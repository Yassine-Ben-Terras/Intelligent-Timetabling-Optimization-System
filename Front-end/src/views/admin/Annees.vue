<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import DataTable from "../../components/DataTable.vue";
import EntityForm from "../../components/EntityForm.vue";
import ConfirmDialog from "../../components/ConfirmDialog.vue";
import type { DataTableHeader } from "../../types";

// Importez le store et les types corrigés
import { useAnneeUniversitaireStore, type AnneeUniversitaire, type AnneeUniversitairePayload } from "../../stores/annees.ts";

// Initialisation du store
const anneeStore = useAnneeUniversitaireStore();

// Utilisation de storeToRefs pour conserver la réactivité des propriétés du state
const { annees, loading, error } = storeToRefs(anneeStore);

// State local pour la gestion du formulaire et des dialogues
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour maintenir l'état du formulaire (création ou édition)
const defaultItem: AnneeUniversitaire = { id: 0, libelle: '' };
const editedItem = ref<AnneeUniversitaire>({ ...defaultItem });

// ID de l'élément à supprimer
const itemToDeleteId = ref<number | null>(null);

// Configuration du tableau
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Année Universitaire" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

// Configuration des champs du formulaire
const formFields = [
  { key: "libelle", label: "Année Universitaire", type: "text", required: true },
];

// Titre dynamique pour le formulaire
const formTitle = computed(() => {
  return isEditing.value ? "Modifier l'année universitaire" : "Ajouter une année universitaire";
});

// Charger les données au montage du composant
onMounted(() => {
  anneeStore.fetchAnnees().catch(err => {
    // L'erreur est déjà gérée dans le store, mais on peut ajouter un logging ici si nécessaire
    console.error("Erreur attrapée dans le composant:", err);
  });
});

// Fonctions CRUD
function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: AnneeUniversitaire) {
  isEditing.value = true;
  editedItem.value = { ...item };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: AnneeUniversitaire) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

async function saveForm() {
  // On peut ajouter une validation ici avec Vuelidate si besoin
  if (isEditing.value) {
    await anneeStore.updateAnnee(editedItem.value);
  } else {
    const payload: AnneeUniversitairePayload = { libelle: editedItem.value.libelle };
    await anneeStore.addAnnee(payload);
  }
  closeFormDialog();
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    await anneeStore.deleteAnnee(itemToDeleteId.value);
  }
  closeConfirmDialog();
}

function closeFormDialog() {
  showFormDialog.value = false;
}

function closeConfirmDialog() {
  showConfirmDialog.value = false;
  itemToDeleteId.value = null;
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Années Universitaires</h1>

    <!-- Afficher un message d'erreur global si une erreur survient dans le store -->
    <v-alert v-if="error" type="error" closable class="mb-4">
      {{ error }}
    </v-alert>

    <DataTable
        title="Liste des années universitaires"
        :headers="headers"
        :items="annees"
        :loading="loading"
        @add="openAddForm"
        @edit="openEditForm"
        @delete="openDeleteConfirm"
    />

    <!-- Dialogue pour le formulaire d'ajout/modification -->
    <v-dialog v-model="showFormDialog" max-width="600px" persistent>
      <EntityForm
          :model-value="editedItem"
          @update:model-value="newValue => editedItem = newValue"
          :fields="formFields"
          :title="formTitle"
          :is-edit="isEditing"
          @submit="saveForm"
          @cancel="closeFormDialog"
      />
    </v-dialog>

    <!-- Dialogue pour la confirmation de suppression -->
    <ConfirmDialog
        v-model="showConfirmDialog"
        title="Supprimer l'année universitaire"
        text="Êtes-vous sûr de vouloir supprimer cette année universitaire ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>