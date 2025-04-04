// store.txt
import { defineStore } from "pinia";
import apiClient from '../services/api';
// Assurez-vous que les chemins et noms sont corrects
import type { PlageTravailDto, PlageTravailEnrichie, PlageTravailPayload, SessionResponse, Jour } from "../types/types";

import { useJourStore } from "./jours";
import { useSessionStore } from "./sessions";

/**
 * Store Pinia pour la gestion des Plages de Travail.
 */
export const usePlageTravailStore = defineStore("plagesTravail", {
  // 1. L'état contient les données BRUTES de l'API (les DTOs)
  state: () => ({
    plagesTravail: [] as PlageTravailDto[],
    loading: false,
    error: null as string | null,
  }),

  // 2. GETTERS - Transforme les données brutes en données enrichies pour la vue
  getters: {
    /**
     * Getter qui enrichit les plages de travail.
     * PREND : PlageTravailDto[] (depuis le state)
     * RETOURNE : PlageTravailEnrichie[] (pour le composant Vue)
     */
    plagesTravailEnrichies(state): PlageTravailEnrichie[] {
      const jourStore = useJourStore();
      const sessionStore = useSessionStore();

      if (!state.plagesTravail.length || !jourStore.jours.length || !sessionStore.sessions.length) {
        return [];
      }

      return state.plagesTravail.map(plageDto => {
        const jour = jourStore.jours.find(j => j.id === plageDto.jourId);
        const session = sessionStore.sessions.find(s => s.id === plageDto.sessionId);

        const sessionParDefaut: SessionResponse = {
            id: plageDto.sessionId,
            libelle: 'Session inconnue',
            annee: { id: 0, libelle: 'Année inconnue', code: '' } // keep it if SessionResponse still requires it
        } as SessionResponse;

        const jourParDefaut: Jour = { id: plageDto.jourId, libelle: 'Jour inconnu' };

        return {
          id: plageDto.id,
          heureDebut: plageDto.heureDebut,
          heureFin: plageDto.heureFin,
          jour: jour || jourParDefaut,
          session: session || sessionParDefaut,
        };
      });
    }
  },

  // 3. ACTIONS - Celles-ci restent inchangées et sont correctes
  actions: {
    async fetchPlagesTravail(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<PlageTravailDto[]>("/plages-travail");
        this.plagesTravail = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur de récupération des plages de travail.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    async addPlageTravail(payload: PlageTravailPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<PlageTravailDto>("/plages-travail", payload);
        this.plagesTravail.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout de la plage de travail.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    async updatePlageTravail(plageId: number, payload: PlageTravailPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<PlageTravailDto>(`/plages-travail/${plageId}`, payload);
        const index = this.plagesTravail.findIndex(p => p.id === plageId);
        if (index !== -1) {
          this.plagesTravail[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour de la plage de travail.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    async deletePlageTravail(plageId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/plages-travail/${plageId}`);
        this.plagesTravail = this.plagesTravail.filter(p => p.id !== plageId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression de la plage de travail.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});