// src/stores/typeSeance.ts

import { ref } from 'vue';
import { defineStore } from 'pinia';
import apiClient from '../services/api'; // <-- ÉTAPE 1 : Importer le client API
import type { TypeSeanceResponse } from '../types/types';

/**
 * Store Pinia pour la gestion des types de séance (ex: COURS, TD, TP).
 */
export const useTypeSeanceStore = defineStore('typeSeance', () => {

  // --- STATE ---
  const typesSeance = ref<TypeSeanceResponse[]>([]);
  const loading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // --- ACTIONS ---
  /**
   * Récupère la VRAIE liste des types de séance depuis l'API Spring Boot.
   */
  async function fetchTypesSeance() {
    loading.value = true;
    error.value = null;
    try {
      // ÉTAPE 2 : FAIRE UN VRAI APPEL API
      // Remplacez '/typeseances' par l'URL exacte de votre endpoint si elle est différente
      const response = await apiClient.get<TypeSeanceResponse[]>('/types-seance');
      
      // On utilise les données de la base de données, pas les données simulées
      typesSeance.value = response.data;

    } catch (e: any) {
      const errorMessage = "Impossible de charger les types de séance depuis l'API.";
      error.value = `${errorMessage}`;
      console.error(errorMessage, e);
    } finally {
      loading.value = false;
    }
  }

  // --- EXPORT ---
  return {
    typesSeance,
    loading,
    error,
    fetchTypesSeance,
  };
});