import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { ProfParamPayload } from "../types/types.ts";

export const useProfParamStore = defineStore("profParams", () => {
    // --- STATE ---
    const liaisons = ref(new Set<string>());
    const loading = ref(false);
    const error = ref<string | null>(null);

    // --- ACTIONS ---

    function createLiaisonKey(payload: ProfParamPayload): string {
        return `${payload.professeurId}-${payload.moduleId}-${payload.typeSeanceId}`;
    }

    async function fetchProfParams(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<ProfParamPayload[]>("/prof-params");
            liaisons.value.clear();
            for (const liaison of response.data) {
                // MODIFIÉ : La clé est maintenant composée de 3 parties
                liaisons.value.add(createLiaisonKey(liaison));
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors de la récupération des affectations.";
            error.value = message;
            throw new Error(message);
        } finally {
            loading.value = false;
        }
    }

    async function addProfParam(payload: ProfParamPayload): Promise<void> {
        const liaisonKey = createLiaisonKey(payload);
        liaisons.value.add(liaisonKey); // Ajout optimiste
        try {
            await apiClient.post("/prof-params", payload);
        } catch (err) {
            liaisons.value.delete(liaisonKey); // Annulation
            const message = err instanceof Error ? err.message : "Erreur lors de l'affectation du module.";
            error.value = message;
            throw new Error(message);
        }
    }

    async function deleteProfParam(payload: ProfParamPayload): Promise<void> {
        const liaisonKey = createLiaisonKey(payload);
        liaisons.value.delete(liaisonKey); // Suppression optimiste
        try {
            await apiClient.delete(`/prof-params/professeurs/${payload.professeurId}/modules/${payload.moduleId}/types-seance/${payload.typeSeanceId}`);
        } catch (err) {
            liaisons.value.add(liaisonKey); // Annulation
            const message = err instanceof Error ? err.message : "Erreur lors de la suppression de l'affectation.";
            error.value = message;
            throw new Error(message);
        }
    }

    return {
        liaisons,
        loading,
        error,
        fetchProfParams,
        addProfParam,
        deleteProfParam,
    };
});