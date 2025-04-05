// src/stores/dispoProfs.ts (RESTE IDENTIQUE)

import { defineStore } from "pinia";
import { ref } from "vue";
import apiClient from '../services/api';
import type { DispoProfPayload } from "../types/types.ts";

export const useDispoProfStore = defineStore("dispoProfs", () => {
    const disponibilités = ref(new Set<string>());
    const loading = ref(false);
    const error = ref<string | null>(null);

    async function fetchDispoProfs(): Promise<void> {
        loading.value = true;
        error.value = null;
        try {
            const response = await apiClient.get<DispoProfPayload[]>("/dispo-profs/all");
            disponibilités.value.clear();
            for (const dispo of response.data) {
                disponibilités.value.add(`${dispo.profId}-${dispo.jourId}-${dispo.periode}`);
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors du chargement des disponibilités.";
            error.value = message;
            throw new Error(message);
        } finally {
            loading.value = false;
        }
    }

    async function updateDisponibilites(toAdd: DispoProfPayload[], toDelete: DispoProfPayload[]): Promise<void> {
        toAdd.forEach(p => disponibilités.value.add(`${p.profId}-${p.jourId}-${p.periode}`));
        toDelete.forEach(p => disponibilités.value.delete(`${p.profId}-${p.jourId}-${p.periode}`));

        try {
            const addPromises = toAdd.map(payload => apiClient.post("/dispo-profs", payload));
            const deletePromises = toDelete.map(payload =>
                apiClient.delete(`/dispo-profs/professeurs/${payload.profId}/jours/${payload.jourId}/periodes/${payload.periode}`)
            );
            await Promise.all([...addPromises, ...deletePromises]);
        } catch (err) {
            const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour des disponibilités.";
            error.value = message;
            toAdd.forEach(p => disponibilités.value.delete(`${p.profId}-${p.jourId}-${p.periode}`));
            toDelete.forEach(p => disponibilités.value.add(`${p.profId}-${p.jourId}-${p.periode}`));
            throw new Error(message);
        }
    }

    return {
        disponibilités,
        loading,
        error,
        fetchDispoProfs,
        updateDisponibilites,
    };
});