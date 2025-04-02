import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { GrpParamDto, GrpParamPayload } from "../types/types.ts";

export const useGrpParamStore = defineStore("grpParams", () => {
    // --- STATE ---
    // MODIFIÉ : On stocke les objets DTO complets, pas juste les clés.
    const liaisons = ref<GrpParamDto[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    // --- ACTIONS ---

    /**
     * Charge toutes les liaisons GrpParam existantes.
     */

    async function fetchGrpParams(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            // NOTE : L'API devrait idéalement renvoyer un DTO riche avec les libellés.
            const response = await apiClient.get<GrpParamDto[]>("/grp-params");
            liaisons.value = response.data.map(item => ({
                ...item,
                // On s'assure que chaque item a une clé unique pour la suppression
                key: `${item.sectionId}-${item.groupeId}-${item.semestreId}`
            }));
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors de la récupération des paramétrages.";
            error.value = message;
            console.error(message);
        } finally {
            loading.value = false;
        }
    }

    /**
     * Crée une nouvelle liaison GrpParam.
     * @param payload L'objet contenant les 4 IDs.
     */

    async function addGrpParam(payload: GrpParamPayload): Promise<void> {
        const key = `${payload.sectionId}-${payload.groupeId}-${payload.semestreId}`;
        if (liaisons.value.some(l => l.key === key)) {
            throw new Error("Cette liaison existe déjà.");
        }

        // On ne fait pas d'ajout optimiste ici, car nous attendons l'objet complet du backend.
        try {
            const response = await apiClient.post<GrpParamDto>("/grp-params", payload);
            liaisons.value.push({
                ...response.data,
                key: `${response.data.sectionId}-${response.data.groupeId}-${response.data.semestreId}`
            });
        } catch (err: any) {
            const backendMsg = err.response?.data?.message || err.response?.data;
            const message = backendMsg ? (typeof backendMsg === 'string' ? backendMsg : JSON.stringify(backendMsg)) : "Erreur lors de l'ajout du paramétrage.";
            error.value = message;
            throw new Error(message); // Propage l'erreur pour que le composant puisse la gérer
        }
    }

    /**
     * Supprime une liaison GrpParam.
     * @param payload L'objet contenant les 4 IDs.
     */

    async function deleteGrpParam(payload: GrpParamPayload): Promise<void> {
        const key = `${payload.sectionId}-${payload.groupeId}-${payload.semestreId}`;
        const index = liaisons.value.findIndex(l => l.key === key);
        if (index === -1) return;

        const [removed] = liaisons.value.splice(index, 1); // Suppression optimiste
        try {
            const { sectionId, groupeId, semestreId } = payload;
            await apiClient.delete(`/grp-params/sections/${sectionId}/groupes/${groupeId}/semestres/${semestreId}`);
        } catch (err) {
            liaisons.value.splice(index, 0, removed); // Annulation en cas d'erreur
            const message = err instanceof Error ? err.message : "Erreur lors de la suppression.";
            error.value = message;
            throw new Error(message);
        }
    }

    return {
        liaisons,
        loading,
        error,
        fetchGrpParams,
        addGrpParam,
        deleteGrpParam,
    };
});