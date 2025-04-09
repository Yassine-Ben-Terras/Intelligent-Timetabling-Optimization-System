import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { AffectationResponseDto, AffectationPayload } from "../types/types";

export const useAffectationStore = defineStore("affectations", () => {
    const affectations = ref<AffectationResponseDto[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    async function fetchAffectations(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<AffectationResponseDto[]>("/affectations");
            affectations.value = response.data;
        } catch (err: any) {
            error.value = err.response?.data || "Erreur lors de la récupération des affectations.";
        } finally {
            loading.value = false;
        }
    }

    async function createAffectation(payload: AffectationPayload): Promise<boolean> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.post<AffectationResponseDto>("/affectations", payload);
            affectations.value.push(response.data);
            return true;
        } catch (err: any) {
            error.value = err.response?.data || "Erreur lors de la création.";
            return false;
        } finally {
            loading.value = false;
        }
    }

    async function deleteAffectation(id: number): Promise<void> {
        const index = affectations.value.findIndex(a => a.id === id);
        if (index === -1) return;
        const affectationSupprimee = affectations.value.splice(index, 1)[0];
        try {
            await apiClient.delete(`/affectations/${id}`);
        } catch (err: any) {
            affectations.value.splice(index, 0, affectationSupprimee);
            error.value = err.response?.data || "Erreur lors de la suppression.";
        }
    }

    return { affectations, loading, error, fetchAffectations, createAffectation, deleteAffectation };
});