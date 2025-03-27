// src/stores/locaux.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { LocalResponse, LocalPayload } from "../types/types.ts"; // Ajustez le chemin

export const useLocalStore = defineStore("locaux", {
    state: () => ({
        locaux: [] as LocalResponse[],
        loading: false,
        error: null as string | null,
    }),

    actions: {
        /**
         * Récupère tous les locaux depuis l'API.
         */
        async fetchLocaux(): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.get<LocalResponse[]>("/locaux/all");
                this.locaux = response.data;
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la récupération des locaux.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Ajoute un nouveau local.
         * @param payload Les données du local à créer.
         */
        async addLocal(payload: LocalPayload): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.post<LocalResponse>("/locaux", payload);
                this.locaux.push(response.data);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Met à jour un local existant.
         * @param localId L'ID du local à mettre à jour.
         * @param payload Les nouvelles données du local.
         */
        async updateLocal(localId: number, payload: LocalPayload): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.put<LocalResponse>(`/locaux/${localId}`, payload);
                const index = this.locaux.findIndex(l => l.id === localId);
                if (index !== -1) {
                    this.locaux[index] = response.data;
                }
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Supprime un local par son ID.
         * @param localId L'ID du local à supprimer.
         */
        async deleteLocal(localId: number): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                await apiClient.delete(`/locaux/${localId}`);
                this.locaux = this.locaux.filter(l => l.id !== localId);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la suppression du local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },
    },
});