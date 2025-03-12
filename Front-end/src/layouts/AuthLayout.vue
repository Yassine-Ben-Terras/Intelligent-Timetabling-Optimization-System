<script setup lang="ts">
// Auth layout for login and registration pages
</script>

<template>
  <div class="auth-layout-wrapper">
  
    <v-main class="auth-background d-flex align-center justify-center">
      <router-view v-slot="{ Component }">
      
        <v-fade-transition mode="out-in">
          <component :is="Component" />
        </v-fade-transition>
      </router-view>
    </v-main>
  </div>
</template>

<style scoped>
.auth-layout-wrapper {
  
  min-height: 100vh;
  display: flex; /* This helps ensure v-main inside it can expand */
  flex-direction: column;
}

.auth-background {
  background: linear-gradient(135deg, #1976D2 0%, #ffffff 100%);
  /* min-height: 100vh; /* v-main with d-flex align-center justify-center should handle this */
  width: 100%; /* Ensure v-main takes full width */
  flex-grow: 1; /* Allow v-main to grow if .auth-layout-wrapper is flex container */
}
</style>