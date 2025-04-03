// src/stores/sessions.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
// On importe les types depuis le fichier centralisé
import type { SessionResponse, SessionPayload } from "../types/types.ts";

// Le nom du store est "sessions"
export const useSessionStore = defineStore("sessions", {
  state: () => ({
    // La liste s'appelle "sessions"
    sessions: [] as SessionResponse[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère toutes les sessions depuis l'API.
     */
    async fetchSessions(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<SessionResponse[]>("/sessions/all");
        this.sessions = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des sessions.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute une nouvelle session.
     * @param payload Les données de la session à créer.
     */
    async addSession(payload: SessionPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<SessionResponse>("/sessions", payload);
        this.sessions.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout de la session.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour une session existante.
     * @param sessionId L'ID de la session à mettre à jour.
     * @param payload Les nouvelles données de la session.
     */
    async updateSession(sessionId: number, payload: SessionPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.put<SessionResponse>(`/sessions/${sessionId}`, payload);
        const index = this.sessions.findIndex(s => s.id === sessionId);
        if (index !== -1) {
          this.sessions[index] = response.data;
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour de la session.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime une session par son ID.
     * @param sessionId L'ID de la session à supprimer.
     */
    async deleteSession(sessionId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/sessions/${sessionId}`);
        this.sessions = this.sessions.filter(s => s.id !== sessionId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression de la session.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});