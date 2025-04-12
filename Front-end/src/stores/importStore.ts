// stores/importStore.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '../services/api'; // Adjust path if needed


// L'URL de votre API backend pour l'importation
const IMPORT_API_URL = '/import/excel' // Correspond à votre @PostMapping

interface MessageState {
    text: string | null
    type: 'success' | 'error' | 'warning' | 'info'
}

export const useImportStore = defineStore('importManager', () => {
    const isLoading = ref(false)
    const message = ref<string | null>(null)
    const messageType = ref<'success' | 'error' | 'warning' | 'info'>('info')

    function setMessage(payload: MessageState) {
        message.value = payload.text
        messageType.value = payload.type
    }

    function clearMessage() {
        message.value = null
    }

    async function uploadExcelFile(file: File) {
        isLoading.value = true
        clearMessage() // Effacer les messages précédents

        const formData = new FormData()
        formData.append('file', file) // La clé 'file' doit correspondre à @RequestParam("file") dans votre controller
  //        const response = await apiClient.post("/professeurs/all");
        try {
            const response = await apiClient.post(IMPORT_API_URL, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            })

            // Supposons que le backend renvoie un objet avec une propriété 'message'
            // et potentiellement d'autres détails si nécessaire
            setMessage({
                text: response.data.message || 'Fichier importé avec succès !',
                type: 'success',
            })
            // Vous pourriez vouloir retourner des données supplémentaires ici si le backend en envoie
            // et que vous en avez besoin dans le composant.
            // return response.data;
        } catch (error: any) {
            console.error("Erreur API lors de l'importation:", error.response?.data || error.message)
            let errorMessage = 'Une erreur inconnue est survenue lors de l\'envoi du fichier.'
            if (error.response && error.response.data && error.response.data.message) {
                errorMessage = error.response.data.message
            } else if (error.message) {
                errorMessage = error.message
            }
            setMessage({
                text: errorMessage,
                type: 'error',
            })
            // throw error; // Optionnel: relancer l'erreur si le composant a besoin de la gérer plus finement
        } finally {
            isLoading.value = false
        }
    }

    return {
        isLoading,
        message,
        messageType,
        setMessage, // Utile si vous voulez définir un message depuis le composant
        clearMessage,
        uploadExcelFile,
    }
})