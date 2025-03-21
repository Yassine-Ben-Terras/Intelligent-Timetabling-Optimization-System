<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";

// Importez vos composants réutilisables
import DataTable from "../../components/DataTable.vue";       // Ajustez le chemin
import EntityForm from "../../components/EntityForm.vue";      // Ajustez le chemin
import ConfirmDialog from "../../components/ConfirmDialog.vue"; // Ajustez le chemin

// Importez les types et le store nécessaires
import type { DataTableHeader, FormField } from "../../types/types.ts"; // Ajustez le chemin
import { useDepartementsStore , type DepartementPayload } from "../../stores/departements"; // Ajustez le chemin
import { Departement } from "../../types/index.ts";
// Initialisation du store
const departementsStore = useDepartementsStore();

// Utilisation de storeToRefs pour conserver la réactivité des propriétés du state
const { departements, loading, error } = storeToRefs(departementsStore);

// State local pour la gestion des dialogues et du formulaire
const showFormDialog = ref(false);
const showConfirmDialog = ref(false);
const isEditing = ref(false);

// Objet pour maintenir l'état du formulaire (création ou édition)
const defaultItem: Departement = { id: 0, libelle: '' };
const editedItem = ref<Departement>({ ...defaultItem });

// ID de l'élément à supprimer
const itemToDeleteId = ref<number | null>(null);

// Configuration des en-têtes pour le DataTable
const headers: DataTableHeader[] = [
  { key: "libelle", title: "Libellé du Département" },
  { key: "actions", title: "Actions", sortable: false, align: 'end' },
];

// Configuration des champs pour le EntityForm
const formFields: FormField[] = [
  { key: "libelle", label: "Libellé", type: "text", required: true },
];

// Titre dynamique pour le formulaire, basé sur le mode (édition ou ajout)
const formTitle = computed(() => {
  return isEditing.value ? "Modifier le département" : "Ajouter un département";
});

// Charger les données initiales au montage du composant
onMounted(() => {
  departementsStore.fetchDepartements().catch(err => {
    // L'erreur est déjà stockée dans le state, mais on peut la logguer ici
    console.error("Erreur attrapée dans le composant au montage:", err.message);
  });
});

// --- Fonctions de gestion de l'interface utilisateur ---

function openAddForm() {
  isEditing.value = false;
  editedItem.value = { ...defaultItem };
  showFormDialog.value = true;
}

function openEditForm(item: Departement) {
  isEditing.value = true;
  // Crée une copie de l'objet pour l'édition afin d'éviter les mutations directes
  editedItem.value = { ...item };
  showFormDialog.value = true;
}

function openDeleteConfirm(item: Departement) {
  itemToDeleteId.value = item.id;
  showConfirmDialog.value = true;
}

function closeFormDialog() {
  showFormDialog.value = false;
}

function closeConfirmDialog() {
  showConfirmDialog.value = false;
  itemToDeleteId.value = null;
}

// --- Fonctions d'interaction avec le store ---

async function saveForm() {
  try {
    if (isEditing.value) {
      await departementsStore.updateDepartement(editedItem.value);
    } else {
      const payload: DepartementPayload = { libelle: editedItem.value.libelle };
      // Débogage : Vérifier le payload juste avant l'envoi
      console.log("Payload envoyé à l'API:", JSON.stringify(payload, null, 2));
      await departementsStore.addDepartement(payload);
    }
    closeFormDialog();
  } catch (err) {
    console.error("Échec de la sauvegarde:", err);
    // On pourrait afficher un message d'erreur plus spécifique ici si nécessaire
  }
}

async function confirmDelete() {
  if (itemToDeleteId.value !== null) {
    try {
      await departementsStore.deleteDepartement(itemToDeleteId.value);
      closeConfirmDialog();
    } catch (err) {
      console.error("Échec de la suppression:", err);
    }
  }
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Gestion des Départements</h1>

    <!-- Affiche une alerte globale si une erreur survient dans le store -->
    <v-alert v-if="error" type="error" closable class="mb-4">
      {{ error }}
    </v-alert>

    <DataTable
        title="Liste des départements"
        :headers="headers"
        :items="departements"
        :loading="loading"
        @add="openAddForm"
        @edit="openEditForm"
        @delete="openDeleteConfirm"
    />

    <!-- Dialogue pour le formulaire d'ajout/modification -->
    <v-dialog v-model="showFormDialog" max-width="600px" persistent>
      <!-- Le v-model est une syntaxe courte pour :model-value et @update:model-value -->
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
        title="Supprimer le département"
        text="Êtes-vous sûr de vouloir supprimer ce département ? Cette action est irréversible."
        confirm-text="Supprimer"
        @confirm="confirmDelete"
        @cancel="closeConfirmDialog"
    />
  </div>
</template>