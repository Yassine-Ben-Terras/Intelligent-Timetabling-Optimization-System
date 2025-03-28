// src/stores/typeLocaux.ts

import { defineStore } from "pinia";
import apiClient from '../services/api';
import type { TypeLocal } from "../types/types.ts";

// Interface pour le payload de création/mise à jour
export interface TypeLocalPayload {
    libelle: string;
}

export const useTypeLocalStore = defineStore("typeLocaux", {
    state: () => ({
        typeLocaux: [] as TypeLocal[],
        loading: false,
        error: null as string | null,
    }),

    actions: {
        /**
         * Récupère tous les types de locaux depuis l'API.
         */
        async fetchTypeLocaux(): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                // L'endpoint GET pour lister tout est /api/type-locaux (d'après le controller)
                const response = await apiClient.get<TypeLocal[]>("/type-locaux");
                this.typeLocaux = response.data;
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la récupération des types de locaux.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Ajoute un nouveau type de local.
         * @param payload Les données du type de local à créer.
         */
        async addTypeLocal(payload: TypeLocalPayload): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.post<TypeLocal>("/type-locaux", payload);
                this.typeLocaux.push(response.data);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de l'ajout du type de local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Met à jour un type de local existant.
         * @param typeLocal L'objet complet à mettre à jour.
         */
        async updateTypeLocal(typeLocal: TypeLocal): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                const payload: TypeLocalPayload = { libelle: typeLocal.libelle };
                await apiClient.put(`/type-locaux/${typeLocal.id}`, payload);

                const index = this.typeLocaux.findIndex(t => t.id === typeLocal.id);
                if (index !== -1) {
                    this.typeLocaux[index] = { ...this.typeLocaux[index], ...typeLocal };
                }
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour du type de local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },

        /**
         * Supprime un type de local par son ID.
         * @param typeLocalId L'ID à supprimer.
         */
        async deleteTypeLocal(typeLocalId: number): Promise<void> {
            this.loading = true;
            this.error = null;
            try {
                await apiClient.delete(`/type-locaux/${typeLocalId}`);
                this.typeLocaux = this.typeLocaux.filter(t => t.id !== typeLocalId);
            } catch (err) {
                const message = err instanceof Error ? err.message : "Erreur lors de la suppression du type de local.";
                this.error = message;
                throw new Error(message);
            } finally {
                this.loading = false;
            }
        },
    },
});