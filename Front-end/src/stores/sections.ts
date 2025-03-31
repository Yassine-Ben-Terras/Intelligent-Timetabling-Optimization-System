// src/stores/sections.ts

import { defineStore } from "pinia";
import apiClient from '../services/api'; // Ajustez le chemin
import type { Section } from "../types/types.ts";    // Ajustez le chemin

// Interface pour le payload de création/mise à jour
export interface SectionPayload {
  libelle: string;
  nbrEtudiants: number;
}

export const useSectionStore = defineStore("sections", {
  state: () => ({
    sections: [] as Section[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère toutes les sections depuis l'API.
     */
    async fetchSections(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<Section[]>("/sections/all");
        this.sections = response.data;
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la récupération des sections.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute une nouvelle section.
     * @param payload Les données de la section à créer.
     */
    async addSection(payload: SectionPayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.post<Section>("/sections", payload);
        this.sections.push(response.data);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de l'ajout de la section.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour une section existante.
     * @param section L'objet section complet à mettre à jour.
     */
    async updateSection(section: Section): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const payload: SectionPayload = {
          libelle: section.libelle,
          nbrEtudiants: section.nbrEtudiants
        };
        await apiClient.put(`/sections/${section.id}`, payload);

        const index = this.sections.findIndex(s => s.id === section.id);
        if (index !== -1) {
          this.sections[index] = { ...this.sections[index], ...section };
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la mise à jour de la section.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime une section par son ID.
     * @param sectionId L'ID de la section à supprimer.
     */
    async deleteSection(sectionId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/sections/${sectionId}`);
        this.sections = this.sections.filter(s => s.id !== sectionId);
      } catch (err) {
        const message = err instanceof Error ? err.message : "Erreur lors de la suppression de la section.";
        this.error = message;
        throw new Error(message);
      } finally {
        this.loading = false;
      }
    },
  },
});