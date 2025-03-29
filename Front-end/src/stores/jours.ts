// src/stores/jours.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { Jour } from "../types/types.ts";    // Ajustez le chemin

// Interface pour le payload de création/mise à jour
export interface JourPayload {
  libelle: string;
}

// Le nom du store est "jours" (cohérent avec le nom du fichier)
export const useJourStore = defineStore("jours", {
  state: () => ({
    // Le state contient une liste de 'jours'
    jours: [] as Jour[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère tous les jours depuis l'API.
     */
    async fetchJours(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        // L'endpoint correct pour l'entité Jour
        const response = await apiClient.get<Jour[]>("/jours/all");
        this.jours = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des jours.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute un nouveau jour.
     * @param payload Les données du jour à créer.
     */
    async addJour(payload: JourPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<Jour>("/jours", payload);
        this.jours.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du jour.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour un jour existant.
     * @param jour L'objet jour complet à mettre à jour.
     */
    async updateJour(jour: Jour): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const payload: JourPayload = { libelle: jour.libelle };
        await apiClient.put(`/jours/${jour.id}`, payload);

        const index = this.jours.findIndex(j => j.id === jour.id);
        if (index !== -1) {
          this.jours[index] = { ...this.jours[index], ...jour };
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du jour.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime un jour par son ID.
     * @param jourId L'ID du jour à supprimer.
     */
    async deleteJour(jourId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/jours/${jourId}`);
        this.jours = this.jours.filter(j => j.id !== jourId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression du jour.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});