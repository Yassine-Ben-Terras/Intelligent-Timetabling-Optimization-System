// src/stores/filieres.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { FiliereResponse, FilierePayload } from "../types/types.ts"; // Ajustez le chemin

export const useFiliereStore = defineStore("filieres", {
  state: () => ({
    filieres: [] as FiliereResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère toutes les filières depuis l'API.
     */
    async fetchFilieres(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<FiliereResponse[]>("/filieres/all");
        this.filieres = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des filières.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute une nouvelle filière.
     * @param payload Les données de la filière à créer.
     */
    async addFiliere(payload: FilierePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<FiliereResponse>("/filieres", payload);
        this.filieres.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout de la filière.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour une filière existante.
     * @param filiereId L'ID de la filière à mettre à jour.
     * @param payload Les nouvelles données de la filière.
     */
    async updateFiliere(filiereId: number, payload: FilierePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<FiliereResponse>(`/filieres/${filiereId}`, payload);
        const index = this.filieres.findIndex(f => f.id === filiereId);
        if (index !== -1) {
          this.filieres[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour de la filière.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime une filière par son ID.
     * @param filiereId L'ID de la filière à supprimer.
     */
    async deleteFiliere(filiereId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/filieres/${filiereId}`);
        this.filieres = this.filieres.filter(f => f.id !== filiereId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression de la filière.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});