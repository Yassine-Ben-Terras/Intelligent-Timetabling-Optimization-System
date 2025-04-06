// src/stores/secParams.ts

import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { SecParamPayload } from "../types/types.ts";

export const useSecParamStore = defineStore("secParams", () => {
    // --- STATE ---
    // On stocke les liaisons existantes dans un Set pour une recherche rapide.
    // La clé sera une chaîne unique comme "filiereId-sectionId-semestreId".
    const liaisons = ref(new Set<string>());
    const loading = ref(false);
    const error = ref<string | null>(null);

    // --- ACTIONS ---

    /**
     * Charge toutes les liaisons SecParam existantes.
     */
    async function fetchSecParams(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<SecParamPayload[]>("/sec-params");
            liaisons.value.clear();
            for (const liaison of response.data) {
                const key = `${liaison.filiereId}-${liaison.sectionId}-${liaison.semestreId}`;
                liaisons.value.add(key);
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors de la récupération des paramétrages de section.";
            error.value = message;
            throw new Error(message);
        } finally {
            loading.value = false;
        }
    }

    /**
     * Crée une nouvelle liaison SecParam.
     * @param payload L'objet contenant les 4 IDs.
     */
    async function addSecParam(payload: SecParamPayload): Promise<void> {
        const key = `${payload.filiereId}-${payload.sectionId}-${payload.semestreId}`;
        if (liaisons.value.has(key)) {
            throw new Error("Cette liaison existe déjà.");
        }
        liaisons.value.add(key); // Ajout optimiste
        try {
            await apiClient.post("/sec-params", payload);
        } catch (err: any) {
            liaisons.value.delete(key); // Annulation en cas d'erreur
            const backendMsg = err.response?.data?.message || err.response?.data;
            const message = backendMsg ? (typeof backendMsg === 'string' ? backendMsg : JSON.stringify(backendMsg)) : "Erreur lors de l'ajout du paramétrage.";
            error.value = message;
            throw new Error(message);
        }
    }

    /**
     * Supprime une liaison SecParam.
     * @param payload L'objet contenant les 4 IDs.
     */
    async function deleteSecParam(payload: SecParamPayload): Promise<void> {
        const key = `${payload.filiereId}-${payload.sectionId}-${payload.semestreId}`;
        liaisons.value.delete(key); // Suppression optimiste
        try {
            const { filiereId, sectionId, semestreId } = payload;
            await apiClient.delete(`/sec-params/filieres/${filiereId}/sections/${sectionId}/semestres/${semestreId}`);
        } catch (err) {
            liaisons.value.add(key); // Annulation en cas d'erreur
            const message = err instanceof Error ? err.message : "Erreur lors de la suppression du paramétrage.";
            error.value = message;
            throw new Error(message);
        }
    }

    return {
        liaisons,
        loading,
        error,
        fetchSecParams,
        addSecParam,
        deleteSecParam,
    };
});