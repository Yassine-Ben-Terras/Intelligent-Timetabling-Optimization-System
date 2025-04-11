// src/stores/emploiTemps.ts
import { defineStore } from 'pinia';
import apiClient from '../services/api';

// Backend interfaces removed (were unused after BackendLigneEmploiTemps cleanup)

// --- Flat structure for Vue components ---
// Cette interface EmploiTemps n'est plus directement utilisée par la vue de génération
export interface EmploiTemps {
  id: string;
  id_groupe: string; nom_groupe?: string;
  id_locale: string; nom_locale?: string;
  id_module: string; nom_module?: string;
  id_prof: string; nom_prof?: string;
  id_section: string; nom_section?: string;
  filiere_id: string; filiere_nom?: string;
  semestre_id: string; semestre_nom?: string;
  jour: string;
  heure_debut: string;
  heure_fin: string;
  type?: string;
}

export const useEmploistore = defineStore('emploiStore', {
  state: () => ({
    emploiTemps: [] as EmploiTemps[], // Ne sera pas rempli par l'endpoint /generate
    loading: false,
    error: null as string | null,
    generationStatusMessage: null as string | null, // Pour les messages de statut de la génération
  }),

  actions: {
    // Cette action appelle l'endpoint de génération qui retourne un message
    async fetchGenerationStatus(sessionId: string, lunchStart?: string, lunchEnd?: string): Promise<void> {
      this.loading = true;
      this.error = null;
      this.generationStatusMessage = null;
      this.emploiTemps = []; // S'assurer que c'est vide

      try {
        // L'API /generate retourne un String (message de succès) ou une erreur avec un message
        let url = `/timetable/v6/generate?session=${sessionId}`;
        if (lunchStart) url += `&lunchStart=${lunchStart}`;
        if (lunchEnd) url += `&lunchEnd=${lunchEnd}`;

        const response = await apiClient.get<string>(url);

        // Si la requête réussit (status 2xx) et que response.data est une chaîne
        if (typeof response.data === 'string') {
          this.generationStatusMessage = response.data;
          console.info("[Store] Message de génération reçu:", response.data);
        } else {
          // Cas inattendu si le backend retourne autre chose qu'une chaîne pour un succès 2xx
          console.warn(`[Store] L'API /generate a retourné un type de données inattendu pour un succès:`, response.data);
          this.error = "Réponse inattendue du serveur après la génération.";
        }

      } catch (err: any) {
        if (err.response) {
          // err.response.data devrait être le message d'erreur textuel du backend
          let backendErrorMessage = err.response.data?.message || err.response.data;
          // Si err.response.data est un objet (ex: erreur de validation Spring Boot), essayez de le formater
          if (typeof backendErrorMessage !== 'string' && backendErrorMessage !== null && backendErrorMessage !== undefined) {
            backendErrorMessage = JSON.stringify(backendErrorMessage);
          }
          this.error = `Erreur API (${err.response.status}): ${backendErrorMessage || err.message || 'Erreur serveur.'}`;
          console.error("[Store] Erreur de réponse API:", err.response);
        } else if (err.request) {
          this.error = "Aucune réponse du serveur. Vérifiez connexion/URL.";
          console.error("[Store] Pas de réponse de l'API:", err.request);
        } else {
          this.error = `Erreur de requête: ${err.message}`;
          console.error("[Store] Erreur de configuration de la requête:", err.message);
        }
      } finally {
        this.loading = false;
      }
    },
  },
});