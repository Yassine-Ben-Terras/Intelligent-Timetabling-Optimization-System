<!-- src/views/ArchiveList.vue -->
<template>
  <v-container fluid class="pa-4">
    <v-card class="mx-auto pa-6" max-width="900">
      <div class="d-flex align-center mb-6">
        <v-icon color="blue-darken-2" size="x-large" class="mr-3">mdi-folder-multiple-outline</v-icon>
        <v-card-title class="text-h4 font-weight-bold pa-0">PDFs Archivés</v-card-title>
      </div>

      <v-alert
        v-if="loading"
        type="info"
        variant="tonal"
        class="mb-4"
        icon="mdi-information-outline"
        closable
      >
        Chargement des PDFs...
        <v-progress-linear indeterminate color="blue-darken-2" class="mt-2"></v-progress-linear>
      </v-alert>

      <v-alert
        v-if="error"
        type="error"
        variant="outlined"
        class="mb-4"
        icon="mdi-alert-circle-outline"
        closable
      >
        <v-alert-title>Erreur</v-alert-title>
        {{ error }}
      </v-alert>

      <v-card-text v-if="!loading && pdfList.length">
        <v-data-table
          :headers="headers"
          :items="pdfList"
          :items-per-page="5"
          class="elevation-2"
          item-value="id"
          density="comfortable"
        >
          <template v-slot:item.action="{ item }">
            <v-btn
              color="blue-darken-2"
              variant="flat"
              small
              @click="openPdf(item.id)"
              prepend-icon="mdi-eye"
            >
              Afficher
            </v-btn>
          </template>

          <template v-slot:no-data>
            <v-alert :value="true" color="warning" icon="mdi-alert-circle">
              Aucun PDF trouvé.
            </v-alert>
          </template>
        </v-data-table>
      </v-card-text>

      <v-card-text v-else-if="!loading" class="text-center pa-10">
        <v-icon size="60" color="grey-lighten-1" class="mb-4">mdi-file-remove-outline</v-icon>
        <p class="text-h6 text-grey-darken-1">Aucun PDF archivé trouvé pour le moment.</p>
        <p class="text-body-2 text-grey-lighten-1 mt-2">Revenez plus tard ou ajoutez de nouveaux documents.</p>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useArchivePdfStore } from '../../stores/archivePdfStore';

// Définition des headers pour v-data-table
const headers = ref([
  { title: 'Nom du Fichier', key: 'fileName' },
  { title: 'Type', key: 'contentType' },
  { title: 'Action', key: 'action', sortable: false },
]);

const store = useArchivePdfStore();
const { pdfList, loading, error } = storeToRefs(store);
const { fetchAllPdfs, openPdf } = store;

onMounted(() => {
  fetchAllPdfs();
});
</script>

<style scoped>
/* Avec Vuetify, la plupart du style est géré par les props et les classes. */
/* Vous n'aurez généralement pas besoin de beaucoup de CSS scoped ici, sauf pour des ajustements très spécifiques. */
</style>