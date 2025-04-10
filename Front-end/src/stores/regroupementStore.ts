import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { RegroupementDto, RegroupementPayload } from "../types/types"; // Ajustez le chemin

export const useRegroupementStore = defineStore("regroupements", () => {
  // --- STATE ---
  const regroupements = ref<RegroupementDto[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // --- ACTIONS ---

  /**
   * Récupère tous les regroupements depuis l'API.
   */
  async function fetchRegroupements(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.get<RegroupementDto[]>("/regroupements");
      regroupements.value = response.data;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la récupération des regroupements.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Récupère un regroupement par son ID.
   * @param id L'ID du regroupement à récupérer.
   * @returns Le regroupement ou null si non trouvé.
   */
  async function fetchRegroupementById(id: number): Promise<RegroupementDto | null> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.get<RegroupementDto>(`/regroupements/${id}`);
      return response.data;
    } catch (err) {
      const message = err instanceof Error ? err.message : `Erreur lors de la récupération du regroupement avec l'ID ${id}.`;
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Crée un nouveau regroupement.
   * @param payload Les données du regroupement à créer.
   */
  async function addRegroupement(payload: RegroupementPayload): Promise<RegroupementDto> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.post<RegroupementDto>("/regroupements", payload);
      regroupements.value.push(response.data);
      return response.data;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du regroupement.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Met à jour un regroupement existant.
   * @param regroupementId L'ID du regroupement à mettre à jour.
   * @param payload Les nouvelles données du regroupement.
   */
  async function updateRegroupement(regroupementId: number, payload: RegroupementPayload): Promise<RegroupementDto> {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.put<RegroupementDto>(`/regroupements/${regroupementId}`, payload);
      const index = regroupements.value.findIndex(r => r.id === regroupementId);
      if (index !== -1) {
        regroupements.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du regroupement.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  /**
   * Supprime un regroupement par son ID.
   * @param regroupementId L'ID du regroupement à supprimer.
   */
  async function deleteRegroupement(regroupementId: number): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      await apiClient.delete(`/regroupements/${regroupementId}`);
      regroupements.value = regroupements.value.filter(r => r.id !== regroupementId);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Erreur lors de la suppression du regroupement.";
      error.value = message;
      throw new Error(message);
    } finally {
      loading.value = false;
    }
  }

  return {
    regroupements,
    loading,
    error,
    fetchRegroupements,
    fetchRegroupementById,
    addRegroupement,
    updateRegroupement,
    deleteRegroupement,
  };
});