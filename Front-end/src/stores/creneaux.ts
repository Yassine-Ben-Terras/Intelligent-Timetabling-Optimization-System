// src/stores/creneaux.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { CreneauResponse, CreneauPayload } from "../types/types.ts"; // Ajustez le chemin

export const useCreneauStore = defineStore("creneaux", {
  state: () => ({
    creneaux: [] as CreneauResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère tous les créneaux depuis l'API.
     */
    async fetchCreneaux(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<CreneauResponse[]>("/creneaux/all");
        this.creneaux = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des créneaux.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute un nouveau créneau.
     * @param payload Les données du créneau à créer.
     */
    async addCreneau(payload: CreneauPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<CreneauResponse>("/creneaux", payload);
        this.creneaux.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du créneau.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour un créneau existant.
     * @param creneauId L'ID du créneau à mettre à jour.
     * @param payload Les nouvelles données du créneau.
     */
    async updateCreneau(creneauId: number, payload: CreneauPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<CreneauResponse>(`/creneaux/${creneauId}`, payload);
        const index = this.creneaux.findIndex(c => c.id === creneauId);
        if (index !== -1) {
          this.creneaux[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du créneau.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime un créneau par son ID.
     * @param creneauId L'ID du créneau à supprimer.
     */
    async deleteCreneau(creneauId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/creneaux/${creneauId}`);
        this.creneaux = this.creneaux.filter(c => c.id !== creneauId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression du créneau.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});