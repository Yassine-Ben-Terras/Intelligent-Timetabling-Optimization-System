<script setup lang="ts">
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from './layouts/AppLayout.vue'

const route = useRoute()

// Mise à jour du titre de la page à chaque changement de route  
watch(() => route.meta.title, (title) => {
  if (title) {
    document.title = `Emplois du Temps | ${title}`
  } else {
    document.title = 'Emplois du Temps'
  }
}, { immediate: true })
</script>

<template>
  <v-app>
    <!-- Avec Keycloak, l'utilisateur est toujours authentifié quand l'app est montée -->
    <AppLayout />
  </v-app>
</template>

<style>
/* Importe la police depuis Google Fonts pour toute l'application */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+Arabic:wght@400;600&display=swap');

/* Applique la police aux éléments qui en ont besoin */
body, .consultation-page-container, .timetable-grid {
  font-family: 'Noto Sans Arabic', 'Segoe UI', Tahoma, sans-serif;
}
</style>