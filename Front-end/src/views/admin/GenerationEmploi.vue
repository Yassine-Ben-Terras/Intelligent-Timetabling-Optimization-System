<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia';
import { useEmploistore  } from  "../../stores/emploiTemps" 
import { useSessionStore } from '../../stores/sessions'; 

const emploiTempsStore = useEmploistore ();
const sessionStore = useSessionStore();

const { sessions, loading: sessionsLoading } = storeToRefs(sessionStore);
const { loading: generationLoading, error: generationError, generationStatusMessage } = storeToRefs(emploiTempsStore);

const formError = ref('');
const operationAttempted = ref(false);

// ✅ Le `v-model` est lié à ces variables qui stockeront les IDs (number)
const params = reactive({
  sessionId: null as number | null,
  lunchStart: '',
  lunchEnd: '',
});

const logMessages = ref<{id: number, type: 'info' | 'success' | 'warning' | 'error', message: string, timestamp: string}[]>([]);

onMounted(() => {
  sessionStore.fetchSessions();
});

watch(generationError, (newError) => {
  if (newError && operationAttempted.value) {
    addLogMessage('error', `Erreur: ${newError}`);
  }
});

watch(generationStatusMessage, (newMessage) => {
  if (newMessage && operationAttempted.value) {
    addLogMessage('success', newMessage);
  }
});

function validateForm(): boolean {
  if (!params.sessionId) { formError.value = 'Session requise.'; return false; }
  formError.value = ''; return true;
}

async function handleGenerateTimetables() {
  logMessages.value = [];
  formError.value = '';
  operationAttempted.value = true;
  emploiTempsStore.error = null;
  emploiTempsStore.generationStatusMessage = null;

  if (!validateForm()) {
    addLogMessage('error', formError.value);
    return;
  }
  
  if (params.sessionId === null) { return; }

  const sessionLibelle = sessions.value.find(s => s.id === params.sessionId)?.libelle || params.sessionId;

  addLogMessage('info', `Tentative de génération des emplois du temps pour : Session ${sessionLibelle}...`);

  // ✅ On envoie les IDs et les params optionnels au backend
  await emploiTempsStore.fetchGenerationStatus(String(params.sessionId), params.lunchStart, params.lunchEnd);
}

function addLogMessage(type: 'info'|'success'|'warning'|'error', msg: string) {
  logMessages.value.push({id:Date.now(), type, message:msg, timestamp:new Date().toLocaleTimeString()});
}
const getLogIcon = (t:string) => ({info:'mdi-information',success:'mdi-check-circle',warning:'mdi-alert',error:'mdi-alert-circle'}[t]||'mdi-information');
const getLogColor = (t:string) => ({info:'info',success:'success',warning:'warning',error:'error'}[t]||'info');

function resetForm() {
  params.sessionId = null;
  params.lunchStart = '';
  params.lunchEnd = '';
  logMessages.value = [];
  formError.value = '';
  operationAttempted.value = false;
  emploiTempsStore.$reset();
}
</script>

<template>
  <v-container fluid>
    <h1 class="text-h4 font-weight-bold mb-6">Génération des emplois du temps</h1>
    
    <v-alert v-if="formError" type="error" density="compact" class="mb-4" closable @click:close="formError = ''" :text="formError" />
    <v-alert v-if="!formError && generationStatusMessage && !generationLoading" type="success" density="compact" class="mb-4" closable @click:close="emploiTempsStore.generationStatusMessage = null" :text="generationStatusMessage" />
    <v-alert v-if="!formError && generationError && !generationLoading" type="error" density="compact" class="mb-4" closable @click:close="emploiTempsStore.error = null" :text="`Erreur: ${generationError}`" />

    <v-row>
      <v-col cols="12" md="8">
        <v-card elevation="2" class="mb-6">
          <v-card-title class="text-subtitle-1 font-weight-medium">Paramètres de Génération</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="handleGenerateTimetables">
              <v-row dense>
                <v-col cols="12" sm="4">
                  <!-- ✅ Ce v-select affiche le 'libelle' mais stocke l''id' dans params.sessionId -->
                  <v-select 
                    v-model="params.sessionId" 
                    :items="sessions" 
                    item-title="libelle" 
                    item-value="id" 
                    label="Session" 
                    variant="outlined" 
                    required 
                    density="compact" 
                    :loading="sessionsLoading"
                    :disabled="generationLoading"
                    no-data-text="Chargement ou aucune donnée"
                  />
                </v-col>
                <v-col cols="12" sm="4">
                  <v-text-field
                    v-model="params.lunchStart"
                    label="Début pause déjeuner"
                    type="time"
                    variant="outlined"
                    density="compact"
                    :disabled="generationLoading"
                    clearable
                  />
                </v-col>
                <v-col cols="12" sm="4">
                  <v-text-field
                    v-model="params.lunchEnd"
                    label="Fin pause déjeuner"
                    type="time"
                    variant="outlined"
                    density="compact"
                    :disabled="generationLoading"
                    clearable
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>
        </v-card>

        <div class="d-flex justify-space-between align-center mb-6">
          <v-btn color="grey-darken-1" variant="text" @click="resetForm" :disabled="generationLoading"><v-icon start>mdi-refresh</v-icon>Réinitialiser</v-btn>
          <v-btn color="primary" size="large" variant="elevated" @click="handleGenerateTimetables" :loading="generationLoading" :disabled="!params.sessionId || generationLoading">
            <v-icon start>mdi-calendar-sync</v-icon>
            Lancer la Génération
          </v-btn>
        </div>
      </v-col>

      <v-col cols="12" md="4">
        <v-card elevation="2" class="generation-log">
          <v-card-title class="d-flex align-center pb-2 text-subtitle-1 font-weight-medium">
            Journal des Opérations <v-spacer />
            <v-chip v-if="generationLoading" color="blue-grey" size="small" label variant="tonal"><v-progress-circular indeterminate size="16" class="mr-2" />En cours...</v-chip>
            <v-chip v-else-if="operationAttempted && generationStatusMessage" color="success" size="small" label variant="tonal"><v-icon start>mdi-check-all</v-icon>Succès</v-chip>
            <v-chip v-else-if="operationAttempted && generationError" color="error" size="small" label variant="tonal"><v-icon start>mdi-alert-circle-outline</v-icon>Erreur</v-chip>
            <v-chip v-else color="grey" size="small" label variant="tonal">En attente</v-chip>
          </v-card-title>
          <v-divider />
          <v-card-text class="log-container pa-0">
            <v-timeline v-if="logMessages.length > 0" density="compact" side="end" truncate-line="both" class="pa-3">
              <v-timeline-item v-for="log in logMessages" :key="log.id" :dot-color="getLogColor(log.type)" :icon="getLogIcon(log.type)" fill-dot size="small">
                <div class="text-caption font-weight-medium"><strong>{{log.timestamp}}</strong></div>
                <div class="text-body-2" style="word-break: break-word;">{{log.message}}</div>
              </v-timeline-item>
            </v-timeline>
            <div v-else class="d-flex flex-column align-center justify-center text-center pa-8" style="min-height: 200px;">
              <v-icon icon="mdi-clipboard-text-clock-outline" size="56" color="grey-lighten-1" />
              <p class="text-body-1 mt-4 text-grey-darken-1">Le journal des opérations s'affichera ici.</p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>