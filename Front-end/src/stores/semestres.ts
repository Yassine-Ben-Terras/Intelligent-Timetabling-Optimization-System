// src/stores/semestres.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { SemestreResponse, SemestrePayload } from "../types/types.ts"; // Ajustez le chemin

export const useSemestreStore = defineStore("semestres", {
  state: () => ({
    semestres: [] as SemestreResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère tous les semestres depuis l'API.
     */
    async fetchSemestres(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<SemestreResponse[]>("/semestres/all");
        this.semestres = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des semestres.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute un nouveau semestre.
     * @param payload Les données du semestre à créer.
     */
    async addSemestre(payload: SemestrePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<SemestreResponse>("/semestres", payload);
        this.semestres.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du semestre.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour un semestre existant.
     * @param semestreId L'ID du semestre à mettre à jour.
     * @param payload Les nouvelles données du semestre.
     */
    async updateSemestre(semestreId: number, payload: SemestrePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<SemestreResponse>(`/semestres/${semestreId}`, payload);
        const index = this.semestres.findIndex(s => s.id === semestreId);
        if (index !== -1) {
          this.semestres[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du semestre.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime un semestre par son ID.
     * @param semestreId L'ID du semestre à supprimer.
     */
    async deleteSemestre(semestreId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/semestres/${semestreId}`);
        this.semestres = this.semestres.filter(s => s.id !== semestreId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression du semestre.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});