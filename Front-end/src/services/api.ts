import axios from 'axios';
import { useAuthStore } from '../stores/auth';
import router from '../router';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json', // This is the default, but can be overridden
    },
});

// Intercepteur de requ├¬te : Ajoute le token ├á chaque requ├¬te sortante.
apiClient.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        const token = authStore.token;

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Intercepteur de r├®ponse : G├¿re les erreurs, notamment les 401 pour d├®connecter.
apiClient.interceptors.response.use(
    response => response,
    async (error) => {
        const originalRequest = error.config;
        const authStore = useAuthStore();

        if (axios.isAxiosError(error) && error.response) {
            if (error.response.status === 401 && !originalRequest.url?.includes('/auth/login')) {
                console.error(
                    `API Interceptor: Erreur 401 sur ${originalRequest.url}. Token invalide ou expir├®. D├®connexion...`
                );
                await authStore.logout();
                router.replace({ name: 'Login' }).catch(navigationError => {
                    console.error("Erreur de navigation vers Login apr├¿s d├®connexion:", navigationError);
                });
                return new Promise(() => {});
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;
