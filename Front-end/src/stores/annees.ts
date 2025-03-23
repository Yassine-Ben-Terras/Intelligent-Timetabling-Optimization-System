import { defineStore } from "pinia";
import apiClient from '../services/api'; // Assurez-vous que le chemin est correct

// Interface TypeScript correspondant au DTO Java
export interface AnneeUniversitaire {
  id: number;
  libelle: string;
}

// Interface pour la création, l'ID n'est pas nécessaire
export interface AnneeUniversitairePayload {
  libelle: string;
}

// Définition du store Pinia
export const useAnneeUniversitaireStore = defineStore("anneeUniversitaire", {
  state: () => ({
    // 'annees' est plus approprié pour une liste
    annees: [] as AnneeUniversitaire[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    /**
     * Récupère toutes les années universitaires depuis l'API.
     */
    async fetchAnnees(): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        const response = await apiClient.get<AnneeUniversitaire[]>("/annees/all");
        this.annees = response.data;
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Une erreur est survenue lors de la récupération des années.";
        // Pour que le composant puisse attraper l'erreur
        throw err;
      } finally {
        this.loading = false;
      }
    },

    /**
     * Ajoute une nouvelle année universitaire.
     * @param payload - L'objet contenant les données de la nouvelle année.
     */
    async addAnnee(payload: AnneeUniversitairePayload): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        // L'API renvoie l'objet créé avec son nouvel ID
        const response = await apiClient.post<AnneeUniversitaire>("/annees", payload);
        this.annees.push(response.data);
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Erreur lors de l'ajout de l'année.";
        throw err;
      } finally {
        this.loading = false;
      }
    },

    /**
     * Met à jour une année universitaire existante.
     * @param annee - L'objet année universitaire complet à mettre à jour.
     */
    async updateAnnee(annee: AnneeUniversitaire): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        // Le payload pour la mise à jour n'a besoin que du libellé
        const payload: AnneeUniversitairePayload = { libelle: annee.libelle };
        await apiClient.put(`/annees/${annee.id}`, payload);

        // Mettre à jour la liste dans le state
        const index = this.annees.findIndex(a => a.id === annee.id);
        if (index !== -1) {
          this.annees[index] = annee;
        }
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Erreur lors de la mise à jour de l'année.";
        throw err;
      } finally {
        this.loading = false;
      }
    },

    /**
     * Supprime une année universitaire par son ID.
     * @param anneeId - L'ID de l'année à supprimer.
     */
    async deleteAnnee(anneeId: number): Promise<void> {
      this.loading = true;
      this.error = null;
      try {
        await apiClient.delete(`/annees/${anneeId}`);
        // Filtrer la liste dans le state pour retirer l'élément supprimé
        this.annees = this.annees.filter(a => a.id !== anneeId);
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Erreur lors de la suppression de l'année.";
        throw err;
      } finally {
        this.loading = false;
      }
    },
  },
});