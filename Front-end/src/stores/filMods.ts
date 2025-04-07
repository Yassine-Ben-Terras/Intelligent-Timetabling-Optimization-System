// src/stores/filMods.ts

import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { FilModPayload } from "../types/types.ts";

export const useFilModStore = defineStore("filMods", () => {
    // --- STATE ---
    // On stocke les liaisons existantes dans un Set pour une recherche rapide.
    // La clé sera une chaîne unique comme "filiereId-moduleId".
    const liaisons = ref(new Set<string>());
    const loading = ref(false);
    const error = ref<string | null>(null);

    // --- ACTIONS ---

    /**
     * Charge toutes les liaisons Filière-Module existantes.
     */
    async function fetchFilMods(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<FilModPayload[]>("/fil-mods/all");
            liaisons.value.clear();
            for (const liaison of response.data) {
                liaisons.value.add(`${liaison.filiereId}-${liaison.moduleId}`);
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors de la récupération des liaisons.";
            error.value = message;
            throw new Error(message);
        } finally {
            loading.value = false;
        }
    }

    /**
     * Crée une nouvelle liaison Filière-Module.
     * @param payload L'objet contenant filiereId et moduleId.
     */
    async function addFilMod(payload: FilModPayload): Promise<void> {
        const liaisonKey = `${payload.filiereId}-${payload.moduleId}`;
        // Ajout optimiste à l'UI
        liaisons.value.add(liaisonKey);
        try {
            await apiClient.post("/fil-mods", payload);
        } catch (err: any) {
            // En cas d'erreur, on annule le changement dans l'UI
            liaisons.value.delete(liaisonKey);
            const backendMsg = err.response?.data?.message || err.response?.data;
            const message = backendMsg ? (typeof backendMsg === 'string' ? backendMsg : JSON.stringify(backendMsg)) : "Erreur lors de l'ajout de la liaison.";
            error.value = message;
            throw new Error(message);
        }
    }

    /**
     * Supprime une liaison Filière-Module.
     * @param payload L'objet contenant filiereId et moduleId.
     */
    async function deleteFilMod(payload: FilModPayload): Promise<void> {
        const liaisonKey = `${payload.filiereId}-${payload.moduleId}`;
        // Suppression optimiste de l'UI
        liaisons.value.delete(liaisonKey);
        try {
            await apiClient.delete(`/fil-mods/filieres/${payload.filiereId}/modules/${payload.moduleId}`);
        } catch (err) {
            // En cas d'erreur, on annule le changement dans l'UI
            liaisons.value.add(liaisonKey);
            const message = err instanceof Error ? err.message : "Erreur lors de la suppression de la liaison.";
            error.value = message;
            throw new Error(message);
        }
    }

    return {
        liaisons,
        loading,
        error,
        fetchFilMods,
        addFilMod,
        deleteFilMod,
    };
});