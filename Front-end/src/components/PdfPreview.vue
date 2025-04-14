<template>
  <div class="pdf-preview-wrapper">
    <h3 class="preview-title">{{ title }}</h3>
    <div class="pdf-viewer-container">
      <iframe v-if="pdfPreviewUrl" :src="pdfPreviewUrl" class="pdf-iframe" :title="`Aperçu de l'emploi du temps pour ${title}`"></iframe>
      <div v-else class="status-message">
        <svg class="spinner" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
        <span>Génération de l'aperçu...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue';
import jsPDF from 'jspdf';
import { useConsultationStore } from '../stores/consultation'; // Ajustez le chemin
import type { TimetableEntryDTO } from '../types/types'; // Ajustez le chemin

const props = defineProps<{
  title: string;
  entries: TimetableEntryDTO[];
  periodText?: string;
}>();

const store = useConsultationStore();
const pdfPreviewUrl = ref<string | null>(null);

// Surveiller les changements dans les entrées pour (re)générer le PDF
watch(() => [props.entries, props.periodText], () => {
  // Nettoyer l'URL précédente pour libérer la mémoire
  if (pdfPreviewUrl.value) {
    URL.revokeObjectURL(pdfPreviewUrl.value);
    pdfPreviewUrl.value = null;
  }

  if (!props.entries || props.entries.length === 0) {
    return;
  }

  // Générer le PDF en mémoire
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
  
  // On utilise la fonction de dessin centralisée du store
  store.drawModernTimetableOnPdf(doc, props.title, props.entries, true, props.periodText || '');

  // Créer un "Blob" (fichier binaire) à partir du PDF
  const blob = doc.output('blob');

  // Créer une URL locale pour ce Blob et la stocker
  pdfPreviewUrl.value = URL.createObjectURL(blob);

}, { immediate: true, deep: true }); // `immediate` pour un chargement initial

// Nettoyer l'URL quand le composant est détruit pour éviter les fuites de mémoire
onUnmounted(() => {
  if (pdfPreviewUrl.value) {
    URL.revokeObjectURL(pdfPreviewUrl.value);
  }
});
</script>

<style scoped>
.pdf-preview-wrapper {
  margin-bottom: 2.5rem; /* Espace entre chaque aperçu de semestre */
}

.preview-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--text-primary);
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--border-color);
}

/* COULEUR DE FOND AJOUTÉE et PADDING */
.pdf-viewer-container {
  width: 100%;
  height: 75vh;
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border-color);
  background-color: #eef2f7; /* Couleur de fond distincte */
  padding: 0.5rem; /* Ajoute un cadre interne */
  box-shadow: inset 0 2px 4px 0 rgb(0 0 0 / 0.05);
}

.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 0.5rem; /* Coins arrondis pour l'iframe lui-même */
}

.status-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
}
.spinner { animation: rotate 2s linear infinite; width: 32px; height: 32px; margin-bottom: 0.5rem; }
.spinner .path { stroke: currentColor; stroke-linecap: round; animation: dash 1.5s ease-in-out infinite; }
@keyframes rotate { 100% { transform: rotate(360deg); } }
@keyframes dash { 0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; } 50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; } 100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; } }
</style>