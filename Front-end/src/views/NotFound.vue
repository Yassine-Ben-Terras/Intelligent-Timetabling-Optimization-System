<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  router.go(-1)
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="not-found-container">
    <v-card class="not-found-card" elevation="2" rounded="lg">
      <v-card-text class="text-center py-8">
        <v-icon icon="mdi-alert-circle" size="100" color="error"></v-icon>
        <h1 class="text-h3 font-weight-bold mt-4">404</h1>
        <h2 class="text-h5 mb-6">Page non trouvée</h2>
        <p class="text-body-1 mb-8">
          La page que vous recherchez n'existe pas ou a été déplacée.
        </p>
        
        <div class="d-flex justify-center gap-4">
          <v-btn
            color="primary"
            variant="outlined"
            @click="goBack"
            prepend-icon="mdi-arrow-left"
          >
            Retour
          </v-btn>
          
          <v-btn
            color="primary"
            @click="goHome"
            prepend-icon="mdi-home"
          >
            Accueil
          </v-btn>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.not-found-card {
  max-width: 500px;
  width: 90%;
}
</style>