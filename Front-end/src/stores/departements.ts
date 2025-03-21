// src/stores/departements.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin vers votre api.ts
import type { Departement } from "../types"; // Ajustez le chemin vers votre types/index.ts

// Interface pour le payload de création/mise à jour.
// L'ID est géré par le backend et n'est pas nécessaire ici.
export interface DepartementPayload {
    libelle: string;
}

export const useDepartementsStore = defineStore("departements", {
    state: () => ({
        departements: [] as Departement[],
        loading: false,
        error: null as string | null,
    }),

    actions: {
        /**
         * Récupère tous les départements depuis l'API.
         */
        async fetchDepartements(): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.get<Departement[]>("/departements/all");
                this.departements = response.data;
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la récupération des départements.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Ajoute un nouveau département.
         * @param payload - Les données du département à créer.
         */
        async addDepartement(payload: DepartementPayload): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.post<Departement>("/departements", payload);
                this.departements.push(response.data);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du département.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Met à jour un département existant.
         * @param departement - L'objet département complet à mettre à jour.
         */
        async updateDepartement(departement: Departement): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const payload: DepartementPayload = { libelle: departement.libelle };
                // Utilise l'endpoint PUT standard: /api/departements/{id}
                await apiClient.put(`/departements/${departement.id}`, payload);

                const index = this.departements.findIndex(d => d.id === departement.id);
                if (index !== -1) {
                    // Met à jour la réactivité en remplaçant l'objet
                    this.departements[index] = { ...this.departements[index], ...departement };
                }
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du département.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Supprime un département par son ID.
         * @param departementId - L'ID du département à supprimer.
         */
        async deleteDepartement(departementId: number): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                // Utilise l'endpoint DELETE standard: /api/departements/{id}
                await apiClient.delete(`/departements/${departementId}`);
                this.departements = this.departements.filter(d => d.id !== departementId);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la suppression du département.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },
    },
});