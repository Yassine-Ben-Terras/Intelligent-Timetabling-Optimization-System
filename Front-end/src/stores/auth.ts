// src/stores/auth.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User } from '../types'; // Assurez-vous que ce chemin est correct
import apiClient from '../services/api'; // Assurez-vous que ce chemin est correct
import type { LoginRequest, RegisterRequest, PasswordUpdateRequest, AdminUserCreateRequest } from '../payloads'; // Assurez-vous que ce chemin est correct
import { isAxiosError } from 'axios';

export const useAuthStore = defineStore('auth', () => {
  // --- STATE ---
  const user = ref<User | null>(null);
  const token = ref<string | null>(localStorage.getItem('token'));

  // [NOUVEAU] State pour suivre si la session a ├®t├® valid├®e au moins une fois
  const isSessionValidated = ref(false);

  // --- INITIALISATION SYNCHRONE DEPUIS LOCALSTORAGE ---
  // Tente de charger l'utilisateur depuis le localStorage si un token existe.
  // C'est une premi├¿re ├®tape rapide, mais qui sera valid├®e par un appel API.
  if (token.value) {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser);
      } catch (e) {
        console.error("AuthStore: Erreur de parsing du user stock├®. Nettoyage.", e);
        user.value = null;
        token.value = null;
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
  }

  // --- GETTERS ---
  const isAuthenticated = computed(() => !!token.value && !!user.value);
  const isAdmin = computed(() => user.value?.roles?.includes('ROLE_ADMIN') || false);
  const userName = computed(() => user.value?.username || user.value?.name || '');
  const userRoles = computed(() => user.value?.roles || []);
  const userFirstName = computed(() => user.value?.firstName || '');
  const userLastName = computed(() => user.value?.lastName || '');
  const userId = computed(() => user.value?.id || null);

  // --- ACTIONS ---

  // [NOUVEAU] Action pour valider le token aupr├¿s du serveur
  async function validateSession(): Promise<void> {
    if (!token.value) {
      console.log("AuthStore: Pas de token, session non valide.");
      isSessionValidated.value = true;
      return;
    }

    try {
      // Endpoint qui retourne les informations de l'utilisateur si le token est valide.
      // Cet appel ├®chouera avec une erreur 401/403 si le token est invalide.
      // Assurez-vous que cet endpoint existe sur votre backend (ex: /api/auth/me)
      const response = await apiClient.get<User>('/auth/me');

      // Met ├á jour les donn├®es de l'utilisateur avec les derni├¿res informations du serveur.
      user.value = response.data;
      console.log("AuthStore: Session valid├®e avec succ├¿s via API.");

    } catch (error) {
      // Le token est probablement expir├® ou invalide.
      console.warn("AuthStore: La validation de la session a ├®chou├®. D├®connexion.", error);
      logout(); // Nettoie l'├®tat et le localStorage.
    } finally {
      // Indique que la tentative de validation a eu lieu, peu importe le r├®sultat.
      isSessionValidated.value = true;
    }
  }

  async function login(credentials: LoginRequest): Promise<boolean> {
    try {
      const response = await apiClient.post<{
        token: string;
        id: number;
        username: string;
        email: string;
        roles: string[];
        firstName?: string;
        lastName?: string;
      }>('/auth/login', credentials);

      const data = response.data;
      token.value = data.token;
      user.value = {
        id: data.id,
        username: data.username,
        email: data.email,
        roles: data.roles,
        name: data.username,
        firstName: data.firstName,
        lastName: data.lastName
      };

      // Apr├¿s une connexion r├®ussie, la session est consid├®r├®e comme valid├®e.
      isSessionValidated.value = true;
      return true;
    } catch (error) {
      console.error('AuthStore: ├ëchec du login:', error);
      // G├®rer et relancer l'erreur pour que le composant puisse l'afficher.
      if (isAxiosError(error) && error.response) {
        throw new Error(error.response.data?.message || 'Identifiants incorrects.');
      }
      throw new Error('Une erreur inattendue est survenue.');
    }
  }

  function logout() {
    console.log("AuthStore: Déconnexion.");
    user.value = null;
    token.value = null;
    isSessionValidated.value = false;
    // Nettoyage explicite du localStorage pour garantir la déconnexion immédiate
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }

  // ... autres actions (register, updatePassword, etc.) restent identiques ...
  async function register(userData: RegisterRequest): Promise<boolean> {
    try {
      // Path is '/auth/register'. Final URL: 'http://localhost:8080/api/auth/register'
      const response = await apiClient.post<{ message: string }>('/auth/register', userData);
      console.log("AuthStore: Registration successful - Message:", response.data.message);
      return true;
    } catch (error) {
      console.error('AuthStore: Registration API call failed:', error);
      if (isAxiosError(error) && error.response) {
        const errorData = error.response.data;
        const message = (typeof errorData === 'string' ? errorData : errorData?.message || errorData?.error) || "Erreur lors de la cr├®ation du compte.";
        throw new Error(message);
      } else if (error instanceof Error) {
        throw new Error(error.message || 'Une erreur inattendue est survenue lors de la cr├®ation du compte.');
      }
      throw new Error('Une erreur inconnue est survenue lors de la cr├®ation du compte.');
    }
  }

  async function updatePassword(payload: PasswordUpdateRequest): Promise<void> {
    if (!isAuthenticated.value) {
      throw new Error("Utilisateur non authentifi├®.");
    }
    try {
      // Path is '/auth/change-password'. Final URL: 'http://localhost:8080/api/auth/change-password'
      await apiClient.post('/auth/change-password', payload);
      console.log("AuthStore: Password update successful.");
    } catch (error) {
      console.error('AuthStore: Password update API call failed:', error);
      if (isAxiosError(error) && error.response) {
        const errorData = error.response.data;
        const message = (typeof errorData === 'string' ? errorData : errorData?.message || errorData?.error) || '├ëchec de la mise ├á jour du mot de passe.';
        throw new Error(message);
      } else if (error instanceof Error) {
        throw new Error(error.message || 'Une erreur inattendue est survenue lors de la mise ├á jour.');
      }
      throw new Error('Une erreur inconnue est survenue lors de la mise ├á jour du mot de passe.');
    }
  }

  async function adminCreateUser(payload: AdminUserCreateRequest): Promise<void> {
    if (!isAdmin.value) {
      throw new Error("Action non autoris├®e. Seuls les administrateurs peuvent cr├®er des utilisateurs.");
    }
    try {
      // Path is '/admin/create-user'. Final URL: 'http://localhost:8080/api/admin/create-user'
      const response = await apiClient.post<{ message: string }>('/auth/create-user', payload);
      console.log("AuthStore: Admin user creation successful - Message:", response.data.message);
    } catch (error) {
      console.error('AuthStore: Admin user creation API call failed:', error);
      if (isAxiosError(error) && error.response) {
        const errorData = error.response.data;
        const message = (typeof errorData === 'string' ? errorData : errorData?.message || errorData?.error) || "Erreur lors de la cr├®ation du compte administrateur.";
        throw new Error(message);
      } else if (error instanceof Error) {
        throw new Error(error.message || 'Une erreur inattendue est survenue lors de la cr├®ation.');
      }
      throw new Error('Une erreur inconnue est survenue lors de la cr├®ation du compte administrateur.');
    }
  }

  return {
    // State
    user,
    token,
    isSessionValidated,

    // Getters
    isAuthenticated,
    isAdmin,
    userName,
    userRoles,
    userFirstName,
    userLastName,
    userId,

    // Actions
    validateSession,
    login,
    logout,
    register,
    updatePassword,
    adminCreateUser,
  };
}, {
  // Configuration de la persistance avec Pinia
  persist: {
    paths: ['user', 'token'], // On ne persiste pas 'isSessionValidated'
    storage: localStorage,
  }
});
