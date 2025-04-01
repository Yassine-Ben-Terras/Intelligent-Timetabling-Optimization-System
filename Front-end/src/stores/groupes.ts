// src/stores/groupes.ts

import { defineStore } from "pinia";
import apiClient from '../services/api';
// CHANGEMENT ICI : Importation des types mis à jour
import type { GroupeResponse, GroupePayload } from "../types/types.ts";

export const useGroupeStore = defineStore("groupes", {
  state: () => ({
    groupes: [] as GroupeResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère tous les groupes depuis l'API.
     */
    async fetchGroupes(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<GroupeResponse[]>("/groupes/all");
        this.groupes = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des groupes.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute un nouveau groupe. Le payload est maintenant de type GroupePayload mis à jour.
     * @param payload Les données du groupe à créer.
     */
    async addGroupe(payload: GroupePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<GroupeResponse>("/groupes", payload);
        this.groupes.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du groupe.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour un groupe existant. Le payload est maintenant de type GroupePayload mis à jour.
     * @param groupeId L'ID du groupe à mettre à jour.
     * @param payload Les nouvelles données du groupe.
     */
    async updateGroupe(groupeId: number, payload: GroupePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<GroupeResponse>(`/groupes/${groupeId}`, payload);
        const index = this.groupes.findIndex(g => g.id === groupeId);
        if (index !== -1) {
          this.groupes[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du groupe.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime un groupe par son ID. (Inchangé)
     * @param groupeId L'ID du groupe à supprimer.
     */
    async deleteGroupe(groupeId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/groupes/${groupeId}`);
        this.groupes = this.groupes.filter(g => g.id !== groupeId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression du groupe.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});