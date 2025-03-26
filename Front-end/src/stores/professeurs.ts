// src/stores/professeurs.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { ProfesseurResponse, ProfesseurPayload } from "../types/types.ts"; // Ajustez le chemin

export const useProfesseurStore = defineStore("professeurs", {
  state: () => ({
    professeurs: [] as ProfesseurResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère tous les professeurs depuis l'API.
     */
    async fetchProfesseurs(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<ProfesseurResponse[]>("/professeurs/all");
        this.professeurs = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des professeurs.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute un nouveau professeur.
     * @param payload Les données du professeur à créer.
     */
    async addProfesseur(payload: ProfesseurPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<ProfesseurResponse>("/professeurs", payload);
        this.professeurs.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du professeur.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour un professeur existant.
     * @param professeurId L'ID du professeur à mettre à jour.
     * @param payload Les nouvelles données du professeur.
     */
    async updateProfesseur(professeurId: number, payload: ProfesseurPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<ProfesseurResponse>(`/professeurs/${professeurId}`, payload);
        const index = this.professeurs.findIndex(p => p.id === professeurId);
        if (index !== -1) {
          this.professeurs[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du professeur.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime un professeur par son ID.
     * @param professeurId L'ID du professeur à supprimer.
     */
    async deleteProfesseur(professeurId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/professeurs/${professeurId}`);
        this.professeurs = this.professeurs.filter(p => p.id !== professeurId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression du professeur.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});