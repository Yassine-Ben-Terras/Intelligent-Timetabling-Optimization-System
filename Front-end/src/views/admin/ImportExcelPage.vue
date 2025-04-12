<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="10" lg="8" xl="6">
        <v-card elevation="2">
          <v-toolbar color="primary" dark density="compact">
            <v-toolbar-title class="font-weight-medium">
              <v-icon start>mdi-file-excel-outline</v-icon>
              Importation de Données (Excel)
            </v-toolbar-title>
          </v-toolbar>

          <v-card-text class="pa-5">
            <p class="text-body-1 mb-6">
              Veuillez sélectionner un fichier Excel (.xlsx ou .xls) à importer.
              Le système analysera le fichier et tentera d'intégrer les données
              conformément à la structure attendue.
            </p>

            <v-file-input
                v-model="selectedFile"
                label="Choisir un fichier Excel"
                outlined
                dense
                show-size
                accept=".xlsx, .xls"
                prepend-inner-icon="mdi-paperclip"
                :loading="importStore.isLoading"
                :disabled="importStore.isLoading"
                @update:model-value="handleFileSelectionChange"
                class="mb-4"
                clearable
                @click:clear="clearFileAndMessages"
            ></v-file-input>

            <v-btn
                color="primary"
                block
                x-large
                :loading="importStore.isLoading"
                :disabled="!canUpload || importStore.isLoading"
                @click="processFileUpload"
                class="text-none"
                height="50"
            >
              <v-icon left size="large">mdi-upload-outline</v-icon>
              Lancer l'Importation
            </v-btn>

            <v-expand-transition>
              <div v-if="importStore.message" class="mt-6">
                <v-alert
                    :type="importStore.messageType"
                    border="start"
                    elevation="2"
                    prominent
                    closable
                    density="compact"
                    @click:close="importStore.clearMessage()"
                >
                  <div class="text-subtitle-1 font-weight-medium mb-1">
                    {{ importStore.messageType === 'success' ? 'Succès de l\'importation' : (importStore.messageType === 'error' ? 'Erreur d\'importation' : 'Information') }}
                  </div>
                  <div v-html="formattedResponseMessage" class="alert-message-content"></div>
                </v-alert>
              </div>
            </v-expand-transition>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTitle } from '@vueuse/core'
import { useImportStore } from '../../stores/importStore'

useTitle('Importer des Données Excel')

const importStore = useImportStore()
const selectedFile = ref<File | null>(null)

const canUpload = computed(() => !!selectedFile.value)

const formattedResponseMessage = computed(() =>
    importStore.message ? importStore.message.replace(/\n/g, '<br>') : ''
)

watch(selectedFile, (newFile) => {
  console.log("[DEBUG] selectedFile:", newFile?.name || "Aucun fichier")
  if (!newFile) importStore.clearMessage()
}, { deep: true })

watch(() => importStore.isLoading, (newIsLoading) => {
  console.log("[DEBUG] isLoading:", newIsLoading)
})
/*
function handleFileSelectionChange(file: File | null) {
  console.log("[DEBUG] handleFileSelectionChange:", file)
  if (!file) importStore.clearMessage()
}
*/
function handleFileSelectionChange(file: File | File[] | null | undefined) {
  console.log("[DEBUG] handleFileSelectionChange:", file)

  if (!file || Array.isArray(file)) {
    importStore.clearMessage()
    return
  }

  // Ici, tu es sûr d’avoir un File
  console.log("[DEBUG] Selected file:", file.name)
}

function clearFileAndMessages() {
  console.log("[DEBUG] clearFileAndMessages")
  selectedFile.value = null
  importStore.clearMessage()
}

async function processFileUpload() {
  console.log("[DEBUG] processFileUpload triggered")

  if (!canUpload.value || importStore.isLoading) {
    console.warn("[DEBUG] Upload blocked - Reason:", !canUpload.value ? "No file selected." : "Upload in progress.")
    if (!canUpload.value) {
      importStore.setMessage({
        text: "Veuillez d'abord sélectionner un fichier Excel à importer.",
        type: "warning",
      })
    }
    return
  }

  const fileToUpload = selectedFile.value
  if (fileToUpload) {
    console.log("[DEBUG] Uploading file:", fileToUpload.name)
    await importStore.uploadExcelFile(fileToUpload)
  } else {
    console.error("[DEBUG] Inconsistency detected: canUpload is true, but selectedFile is null.")
  }
}
</script>

<style scoped>
.v-card {
  border-radius: 8px;
}
.v-toolbar-title {
  font-size: 1.1rem;
}
.alert-message-content {
  font-size: 0.95rem;
  line-height: 1.6;
}
.debug-info {
  font-size: 0.8rem;
  color: grey;
  border: 1px dashed lightgrey;
  padding: 5px;
}
</style>