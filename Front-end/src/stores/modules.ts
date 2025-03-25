// src/stores/modules.ts

import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api'; // Ajustez le chemin
import type { ModuleResponse, ModulePayload } from "../types/types.ts"; // Ajustez le chemin

export const useModuleStore = defineStore("modules", () => {
  // --- STATE ---
  const modules = ref<ModuleResponse[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // --- ACTIONS ---

  /**
   * Récupère tous les modules depuis l'API.
   */
  async function fetchModules(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.get<ModuleResponse[]>("/modules/all");
      modules.value = response.data;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la récupération des modules.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Ajoute un nouveau module.
   * @param payload Les données du module à créer.
   */
  async function addModule(payload: ModulePayload): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.post<ModuleResponse>("/modules", payload);
      modules.value.push(response.data);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du module.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Met à jour un module existant.
   * @param moduleId L'ID du module à mettre à jour.
   * @param payload Les nouvelles données du module.
   */
  async function updateModule(moduleId: number, payload: ModulePayload): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.put<ModuleResponse>(`/modules/${moduleId}`, payload);
      const index = modules.value.findIndex(m => m.id === moduleId);
      if (index !== -1) {
        modules.value[index] = response.data;
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du module.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Supprime un module par son ID.
   * @param moduleId L'ID du module à supprimer.
   */
  async function deleteModule(moduleId: number): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      await apiClient.delete(`/modules/${moduleId}`);
      modules.value = modules.value.filter(m => m.id !== moduleId);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la suppression du module.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  return {
    modules,
    loading,
    error,
    fetchModules,
    addModule,
    updateModule,
    deleteModule,
  };
});